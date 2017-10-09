package org.dan.ping.pong.app.tournament;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static ord.dan.ping.pong.jooq.Tables.BID;
import static ord.dan.ping.pong.jooq.Tables.CATEGORY;
import static ord.dan.ping.pong.jooq.Tables.CITY;
import static ord.dan.ping.pong.jooq.Tables.MATCHES;
import static ord.dan.ping.pong.jooq.Tables.PLACE;
import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT;
import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT_ADMIN;
import static org.dan.ping.pong.app.bid.BidDao.TERMINAL_BID_STATES;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Rest;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.tournament.TournamentState.Announce;
import static org.dan.ping.pong.app.tournament.TournamentState.Canceled;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentState.Hidden;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.app.tournament.TournamentState.Replaced;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.db.DbUpdateSql.NON_ZERO_ROWS;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.city.CityLink;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.place.PlaceAddress;
import org.dan.ping.pong.app.place.PlaceLink;
import org.dan.ping.pong.sys.db.DbUpdateSql;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectField;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class TournamentDao {
    public static final String AUTHOR = "author";
    private static final SelectField[] DATED_DIGEST_FIELDS = {
            TOURNAMENT.OPENS_AT,
            TOURNAMENT.TID,
            TOURNAMENT.NAME};
    private static final String ENLISTED = "enlisted";
    private static final String PARTICIPANTS = "participants";
    private static final String GAMES = "games";
    private static final String GAMES_COMPLETE = "gamesComplete";
    private static final int DAYS_TO_SHOW_PAST_TOURNAMENT = 30;
    private static final String CATEGORIES = "categories";

    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public int create(int uid, CreateTournament newTournament) {
        final int tid = justCreate(newTournament, Hidden);
        log.info("User {} created tournament {}", uid, tid);

        jooq.insertInto(TOURNAMENT_ADMIN, TOURNAMENT_ADMIN.TID,
                TOURNAMENT_ADMIN.UID, TOURNAMENT_ADMIN.TYPE)
                .values(tid, uid, AUTHOR)
                .execute();
        return tid;
    }

    private Integer justCreate(CreateTournament newTournament, TournamentState hidden) {
        return jooq.insertInto(TOURNAMENT, TOURNAMENT.STATE,
                TOURNAMENT.OPENS_AT,
                TOURNAMENT.PID,
                TOURNAMENT.PREVIOUS_TID,
                TOURNAMENT.RULES,
                TOURNAMENT.TICKET_PRICE,
                TOURNAMENT.NAME)
                .values(hidden,
                        newTournament.getOpensAt(),
                        newTournament.getPlaceId(),
                        newTournament.getPreviousTid(),
                        newTournament.getRules(),
                        newTournament.getTicketPrice(),
                        newTournament.getName())
                .returning(TOURNAMENT.TID)
                .fetchOne()
                .getTid();
    }

    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public List<TournamentDigest> findWritableForAdmin(int uid, Instant after) {
        return jooq.select(TOURNAMENT.NAME, TOURNAMENT.OPENS_AT, TOURNAMENT.TID, TOURNAMENT.STATE)
                .from(TOURNAMENT)
                .innerJoin(TOURNAMENT_ADMIN)
                .on(TOURNAMENT.TID.eq(TOURNAMENT_ADMIN.TID))
                .where(TOURNAMENT_ADMIN.UID.eq(uid),
                        TOURNAMENT.STATE.in(Hidden, Announce, Draft, Open)
                                .or(TOURNAMENT.OPENS_AT.ge(after)))
                .fetch()
                .map(r -> TournamentDigest.builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
                        .state(r.get(TOURNAMENT.STATE))
                        .build());
    }

    private <T extends Record> DatedTournamentDigest mapToDatedDigest(T r) {
        return DatedTournamentDigest.builder()
                .name(r.get(TOURNAMENT.NAME))
                .tid(r.get(TOURNAMENT.TID))
                .opensAt(r.get(TOURNAMENT.OPENS_AT))
                .build();
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<TournamentDigest> findEnlistedIn(int uid, Instant before) {
        return jooq.select(TOURNAMENT.NAME, TOURNAMENT.OPENS_AT, TOURNAMENT.TID, TOURNAMENT.STATE)
                .from(TOURNAMENT)
                .innerJoin(BID)
                .on(TOURNAMENT.TID.eq(BID.TID))
                .where(BID.UID.eq(uid), BID.STATE.ne(Quit),
                        TOURNAMENT.STATE.in(Announce, Draft, Open)
                                .or(TOURNAMENT.OPENS_AT.ge(before)))
                .fetch()
                .map(r -> TournamentDigest.builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
                        .state(r.get(TOURNAMENT.STATE))
                        .build());
    }

    private SelectJoinStep<Record> selectDatedDigest() {
        return jooq.select(DATED_DIGEST_FIELDS)
                .from(TOURNAMENT);
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<DatedTournamentDigest> findByState(TournamentState state) {
        return selectDatedDigest()
                .where(TOURNAMENT.STATE.eq(state))
                .fetch()
                .map(this::mapToDatedDigest);
    }

    public void setState(OpenTournamentMemState tournament, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .logBefore(() -> log.info("Switch tournament {} into {} state.",
                        tournament.getTid(), tournament.getState()))
                .mustAffectRows(NON_ZERO_ROWS)
                .onFailure(u -> badRequest("Stale state"))
                .query(jooq.update(TOURNAMENT)
                        .set(TOURNAMENT.STATE, tournament.getState())
                        .where(TOURNAMENT.TID.eq(tournament.getTid())))
                .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public boolean isAdminOf(int uid, int tid) {
        return jooq.select(TOURNAMENT.TID)
                .from(TOURNAMENT).innerJoin(TOURNAMENT_ADMIN)
                .on(TOURNAMENT.TID.eq(TOURNAMENT_ADMIN.TID))
                .where(TOURNAMENT.TID.eq(tid),
                        TOURNAMENT_ADMIN.UID.eq(uid))
                .fetchOne() != null;
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<DraftingTournamentInfo> getDraftingTournament(int tid,
            Optional<Integer> participantId) {
        return ofNullable(jooq.select(TOURNAMENT.NAME, TOURNAMENT.OPENS_AT,
                BID.CID, TOURNAMENT_ADMIN.UID, TOURNAMENT.TICKET_PRICE,
                TOURNAMENT.PREVIOUS_TID, PLACE.PID, PLACE.POST_ADDRESS,
                PLACE.CITY_ID, CITY.NAME, TOURNAMENT.RULES,
                PLACE.NAME, PLACE.PHONE, TOURNAMENT.STATE, BID.STATE)
                .from(TOURNAMENT)
                .innerJoin(PLACE).on(TOURNAMENT.PID.eq(PLACE.PID))
                .innerJoin(CITY).on(CITY.CITY_ID.eq(PLACE.CITY_ID))
                .leftJoin(TOURNAMENT_ADMIN)
                .on(TOURNAMENT.TID.eq(TOURNAMENT_ADMIN.TID),
                        TOURNAMENT_ADMIN.UID.eq(participantId.orElse(0)))
                .leftJoin(BID)
                .on(TOURNAMENT.TID.eq(BID.TID),
                        BID.UID.eq(participantId.orElse(0)))
                .where(TOURNAMENT.TID.eq(tid))
                .fetchOne())
                .map(r -> DraftingTournamentInfo.builder()
                        .tid(tid)
                        .rules(r.get(TOURNAMENT.RULES))
                        .name(r.get(TOURNAMENT.NAME))
                        .state(r.get(TOURNAMENT.STATE))
                        .ticketPrice(r.get(TOURNAMENT.TICKET_PRICE))
                        .previousTid(r.get(TOURNAMENT.PREVIOUS_TID))
                        .bidState(ofNullable(r.get(BID.STATE)))
                        .place(PlaceLink.builder()
                                .name(r.get(PLACE.NAME))
                                .address(PlaceAddress.builder()
                                        .address(r.get(PLACE.POST_ADDRESS))
                                        .city(CityLink.builder()
                                                .id(r.get(PLACE.CITY_ID))
                                                .name(r.get(CITY.NAME))
                                                .build())
                                        .phone(r.get(PLACE.PHONE))
                                        .build())
                                .pid(r.get(PLACE.PID)).build())
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
                        .myCategoryId(ofNullable(r.get(BID.CID)))
                        .iAmAdmin(participantId.isPresent()
                                && participantId.equals(ofNullable(r.get(TOURNAMENT_ADMIN.UID))))
                        .build());
    }

    public Optional<MyTournamentInfo> getMyTournamentInfo(int tid) {
        return ofNullable(jooq.select(TOURNAMENT.NAME,
                TOURNAMENT.STATE, TOURNAMENT.PID, PLACE.NAME,
                TOURNAMENT.OPENS_AT, TOURNAMENT.TICKET_PRICE,
                TOURNAMENT.PREVIOUS_TID,
                DSL.selectCount().from(CATEGORY)
                        .where(CATEGORY.TID.eq(tid))
                        .asField(CATEGORIES),
                DSL.selectCount()
                        .from(BID)
                        .where(BID.TID.eq(tid), BID.STATE.ne(Quit))
                        .asField(ENLISTED))
                .from(TOURNAMENT)
                .innerJoin(PLACE)
                .on(TOURNAMENT.PID.eq(PLACE.PID))
                .where(TOURNAMENT.TID.eq(tid))
                .fetchOne())
                .map(r -> MyTournamentInfo.builder()
                        .tid(tid)
                        .name(r.get(TOURNAMENT.NAME))
                        .enlisted(r.get(ENLISTED, Integer.class))
                        .categories(r.get(CATEGORIES, Integer.class))
                        .place(PlaceLink.builder()
                                .name(r.get(PLACE.NAME))
                                .pid(r.get(TOURNAMENT.PID))
                                .build())
                        .state(r.get(TOURNAMENT.STATE))
                        .price(r.get(TOURNAMENT.TICKET_PRICE))
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
                        .previousTid(r.get(TOURNAMENT.PREVIOUS_TID))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<OpenTournamentDigest> findRunning(Instant includeClosedAfter) {
        return jooq
                .select(TOURNAMENT.TID, TOURNAMENT.OPENS_AT,
                        TOURNAMENT.NAME, TOURNAMENT.STATE,
                DSL.selectCount().from(BID)
                        .where(BID.TID.eq(TOURNAMENT.TID))
                        .asField(PARTICIPANTS),
                DSL.selectCount().from(MATCHES).where(MATCHES.TID.eq(TOURNAMENT.TID))
                        .asField(GAMES),
                DSL.selectCount().from(MATCHES).where(MATCHES.TID.eq(TOURNAMENT.TID),
                        MATCHES.STATE.eq(MatchState.Over))
                        .asField(GAMES_COMPLETE))
                .from(TOURNAMENT)
                .where(TOURNAMENT.STATE.eq(Open)
                        .or(TOURNAMENT.STATE.eq(Close)
                                .and(TOURNAMENT.OPENS_AT.ge(includeClosedAfter))))
                .orderBy(TOURNAMENT.OPENS_AT)
                .fetch()
                .map(r -> OpenTournamentDigest.builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .startedAt(r.get(TOURNAMENT.OPENS_AT))
                        .state(r.get(TOURNAMENT.STATE))
                        .pariticipants(r.get(PARTICIPANTS, Integer.class))
                        .gamesOverall(r.get(GAMES, Integer.class))
                        .gamesComplete(r.get(GAMES_COMPLETE, Integer.class))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<DatedTournamentDigest> findMyNextTournament(int uid) {
        return ofNullable(jooq.select(TOURNAMENT.TID, TOURNAMENT.NAME, TOURNAMENT.OPENS_AT)
                .from(BID)
                .innerJoin(TOURNAMENT).on(BID.TID.eq(TOURNAMENT.TID))
                .where(BID.UID.eq(uid),
                        BID.STATE.in(Paid, Here, Want))
                .orderBy(TOURNAMENT.OPENS_AT.asc())
                .limit(1)
                .fetchOne())
                .map(r -> DatedTournamentDigest
                        .builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<CompleteTournamentDigest> findMyPreviousTournament(Instant since, int uid) {
        return ofNullable(jooq.select(TOURNAMENT.TID, TOURNAMENT.NAME, BID.STATE)
                .from(BID)
                .innerJoin(TOURNAMENT).on(BID.TID.eq(TOURNAMENT.TID))
                .where(BID.UID.eq(uid),
                        BID.UPDATED.ge(Optional.of(since)),
                        BID.STATE.in(TERMINAL_BID_STATES))
                .orderBy(BID.UPDATED.desc())
                .limit(1)
                .fetchOne())
                .map(r -> CompleteTournamentDigest
                        .builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .outcome(r.get(BID.STATE))
                        .build());
    }

    private List<DatedTournamentDigest> findCurrentTournaments(int uid) {
        return jooq.select(TOURNAMENT.TID, TOURNAMENT.NAME, TOURNAMENT.OPENS_AT)
                .from(BID)
                .innerJoin(TOURNAMENT).on(BID.TID.eq(TOURNAMENT.TID))
                .where(BID.UID.eq(uid), BID.STATE.in(Play, Rest, Wait))
                .fetch()
                .map(r -> DatedTournamentDigest
                        .builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
                        .build());
    }

    private List<DatedTournamentDigest> findCurrentJudgeTournaments(int uid) {
        return jooq.select(TOURNAMENT.TID, TOURNAMENT.NAME, TOURNAMENT.OPENS_AT)
                .from(TOURNAMENT_ADMIN)
                .innerJoin(TOURNAMENT).on(TOURNAMENT_ADMIN.TID.eq(TOURNAMENT.TID))
                .where(TOURNAMENT_ADMIN.UID.eq(uid), TOURNAMENT.STATE.in(Open))
                .fetch()
                .map(r -> DatedTournamentDigest
                        .builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public MyRecentTournaments findMyRecentTournaments(Instant since, int uid) {
        return MyRecentTournaments.builder()
                .next(findMyNextTournament(uid))
                .current(findCurrentTournaments(uid))
                .previous(findMyPreviousTournament(since, uid))
                .build();
    }


    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<DatedTournamentDigest> findMyNextJudgeTournament(int uid) {
        return ofNullable(jooq.select(TOURNAMENT.TID, TOURNAMENT.NAME, TOURNAMENT.OPENS_AT)
                .from(TOURNAMENT_ADMIN)
                .leftJoin(TOURNAMENT).on(TOURNAMENT_ADMIN.TID.eq(TOURNAMENT.TID))
                .where(TOURNAMENT_ADMIN.UID.eq(uid),
                        TOURNAMENT.STATE.in(Announce, Draft))
                .orderBy(TOURNAMENT.OPENS_AT.asc())
                .limit(1)
                .fetchOne())
                .map(r -> DatedTournamentDigest
                        .builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<DatedTournamentDigest> findMyPreviousJudgeTournament(Instant now, int uid) {
        return ofNullable(jooq.select(TOURNAMENT.TID, TOURNAMENT.NAME, TOURNAMENT.OPENS_AT)
                .from(TOURNAMENT_ADMIN)
                .leftJoin(TOURNAMENT).on(TOURNAMENT_ADMIN.TID.eq(TOURNAMENT.TID))
                .where(TOURNAMENT_ADMIN.UID.eq(uid),
                        TOURNAMENT.STATE.in(Close, Canceled, Replaced),
                        TOURNAMENT.COMPLETE_AT.gt(Optional.of(now.minus(DAYS_TO_SHOW_PAST_TOURNAMENT, DAYS))))
                .orderBy(TOURNAMENT.COMPLETE_AT.desc())
                .limit(1)
                .fetchOne())
                .map(r -> DatedTournamentDigest
                        .builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public MyRecentJudgedTournaments findMyRecentJudgedTournaments(Instant now, int uid) {
        return MyRecentJudgedTournaments.builder()
                .next(findMyNextJudgeTournament(uid))
                .current(findCurrentJudgeTournaments(uid))
                .previous(findMyPreviousJudgeTournament(now, uid))
                .build();
    }

    public void update(TournamentUpdate update, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(TOURNAMENT)
                        .set(TOURNAMENT.TICKET_PRICE, update.getPrice())
                        .set(TOURNAMENT.PID, update.getPlaceId())
                        .set(TOURNAMENT.NAME, update.getName())
                        .set(TOURNAMENT.OPENS_AT, update.getOpensAt())
                        .where(TOURNAMENT.TID.eq(update.getTid())))
                .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<TournamentRules> getTournamentRules(int tid) {
        return ofNullable(jooq
                .select(TOURNAMENT.RULES)
                .from(TOURNAMENT)
                .where(TOURNAMENT.TID.eq(tid))
                .fetchOne())
                .map(r -> r.get(TOURNAMENT.RULES));
    }

    @Transactional(TRANSACTION_MANAGER)
    public void updateParams(int tid, TournamentRules rules, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(TOURNAMENT)
                        .set(TOURNAMENT.RULES, rules)
                        .where(TOURNAMENT.TID.eq(tid)))
                .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<TournamentComplete> completeInfo(int tid) {
        return ofNullable(jooq.select(TOURNAMENT.NAME, TOURNAMENT.STATE)
                .from(TOURNAMENT)
                .where(TOURNAMENT.TID.eq(tid))
                .fetchOne())
                .map(r -> TournamentComplete
                        .builder()
                        .state(r.get(TOURNAMENT.STATE))
                        .name(r.get(TOURNAMENT.NAME))
                        .build());

    }

    @Transactional(TRANSACTION_MANAGER)
    public int copy(CopyTournament copyTournament) {
        final int originTid = copyTournament.getOriginTid();
        final int newTid = copyTournamentRow(copyTournament);
        copyPermissions(originTid, newTid);
        return newTid;
    }

    private int copyTournamentRow(CopyTournament copyTournament) {
        final int originTid = copyTournament.getOriginTid();
        final MyTournamentInfo tinfo = getMyTournamentInfo(originTid)
                .orElseThrow(() -> notFound("Tournament " + originTid + " not found"));
        final TournamentRules rules = getTournamentRules(originTid)
                .orElseThrow(() -> notFound("Tournament " + originTid + " not found"));
        return justCreate(CreateTournament.builder()
                .name(copyTournament.getName())
                .rules(rules)
                .opensAt(copyTournament.getOpensAt())
                .placeId(tinfo.getPlace().getPid())
                .ticketPrice(tinfo.getPrice())
                .previousTid(of(originTid))
                .build(), Draft);
    }

    private void copyPermissions(int originTid, int newTid) {
       jooq.batch(jooq.select(TOURNAMENT_ADMIN.TYPE, TOURNAMENT_ADMIN.UID)
                .from(TOURNAMENT_ADMIN)
                .where(TOURNAMENT_ADMIN.TID.eq(originTid))
                .fetch()
                .map(r ->
                        jooq.insertInto(TOURNAMENT_ADMIN, TOURNAMENT_ADMIN.TID,
                                TOURNAMENT_ADMIN.UID, TOURNAMENT_ADMIN.TYPE)
                                .values(newTid, r.get(TOURNAMENT_ADMIN.UID),
                                        r.get(TOURNAMENT_ADMIN.TYPE)))).execute();
    }

    public void setCompleteAt(int tid, Optional<Instant> now, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(TOURNAMENT)
                        .set(TOURNAMENT.COMPLETE_AT, now)
                        .where(TOURNAMENT.TID.eq(tid)))
                .build());
    }

    public Set<Integer> loadAdmins(Tid tid) {
        return jooq.select(TOURNAMENT_ADMIN.UID)
                .from(TOURNAMENT_ADMIN)
                .where(TOURNAMENT_ADMIN.TID.eq(tid.getTid()))
                .fetch()
                .stream()
                .map(r -> r.get(TOURNAMENT_ADMIN.UID))
                .collect(toSet());
    }

    public Optional<TournamentRow> getRow(Tid tid) {
        return ofNullable(jooq
                .select(TOURNAMENT.PID, TOURNAMENT.RULES,
                        TOURNAMENT.COMPLETE_AT, TOURNAMENT.OPENS_AT,
                        TOURNAMENT.NAME, TOURNAMENT.STATE)
                .from(TOURNAMENT)
                .where(TOURNAMENT.TID.eq(tid.getTid()))
                .fetchOne())
                .map(r -> TournamentRow.builder()
                        .pid(new Pid(r.get(TOURNAMENT.PID)))
                        .endedAt(r.get(TOURNAMENT.COMPLETE_AT))
                        .startedAt(r.get(TOURNAMENT.OPENS_AT))
                        .name(r.get(TOURNAMENT.NAME))
                        .state(r.get(TOURNAMENT.STATE))
                        .rules(r.get(TOURNAMENT.RULES))
                        .tid(tid)
                        .build());
    }
}
