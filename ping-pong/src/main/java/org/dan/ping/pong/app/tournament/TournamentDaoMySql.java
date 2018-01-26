package org.dan.ping.pong.app.tournament;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.Console;
import static org.dan.ping.pong.jooq.Tables.BID;
import static org.dan.ping.pong.jooq.Tables.CATEGORY;
import static org.dan.ping.pong.jooq.Tables.MATCHES;
import static org.dan.ping.pong.jooq.Tables.PLACE;
import static org.dan.ping.pong.jooq.Tables.TOURNAMENT;
import static org.dan.ping.pong.jooq.Tables.TOURNAMENT_ADMIN;
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
import static org.dan.ping.pong.app.tournament.TournamentType.Classic;
import static org.dan.ping.pong.jooq.Tables.TOURNAMENT_RELATION;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.db.DbUpdateSql.NON_ZERO_ROWS;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.place.PlaceLink;
import org.dan.ping.pong.app.tournament.console.TournamentRelationType;
import org.dan.ping.pong.jooq.tables.TournamentRelation;
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
public class TournamentDaoMySql implements TournamentDao {
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
    private static final TournamentRelation CHILD_TID_REL = TOURNAMENT_RELATION.as("child_tid_rel");
    private static final TournamentRelation MASTER_TID_REL = TOURNAMENT_RELATION.as("master_tid_rel");

    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public Tid create(Uid uid, CreateTournament newTournament) {
        final Tid tid = justCreate(newTournament, Hidden);
        log.info("User {} created tournament {}", uid, tid);

        jooq.insertInto(TOURNAMENT_ADMIN, TOURNAMENT_ADMIN.TID,
                TOURNAMENT_ADMIN.UID, TOURNAMENT_ADMIN.TYPE)
                .values(tid, uid, AUTHOR)
                .execute();
        return tid;
    }

    private Tid justCreate(CreateTournament newTournament, TournamentState hidden) {
        return jooq.insertInto(TOURNAMENT, TOURNAMENT.STATE,
                TOURNAMENT.OPENS_AT,
                TOURNAMENT.PID,
                TOURNAMENT.PREVIOUS_TID,
                TOURNAMENT.RULES,
                TOURNAMENT.TICKET_PRICE,
                TOURNAMENT.NAME,
                TOURNAMENT.TYPE,
                TOURNAMENT.SPORT)
                .values(hidden,
                        newTournament.getOpensAt(),
                        newTournament.getPlaceId(),
                        newTournament.getPreviousTid().map(Tid::getTid),
                        newTournament.getRules(),
                        newTournament.getTicketPrice(),
                        newTournament.getName(),
                        newTournament.getType(),
                        newTournament.getSport())
                .returning(TOURNAMENT.TID)
                .fetchOne()
                .getTid();
    }

    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public List<TournamentDigest> findWritableForAdmin(Uid uid, Instant after) {
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
    public List<TournamentDigest> findEnlistedIn(Uid uid, Instant before) {
        return jooq.select(TOURNAMENT.NAME, TOURNAMENT.OPENS_AT,
                TOURNAMENT.TID, TOURNAMENT.STATE)
                .from(TOURNAMENT)
                .innerJoin(BID)
                .on(TOURNAMENT.TID.eq(BID.TID))
                .where(BID.UID.eq(uid), BID.STATE.ne(Quit),
                        TOURNAMENT.TYPE.eq(Classic),
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
                .where(TOURNAMENT.TYPE.eq(Classic),
                        TOURNAMENT.STATE.eq(state))
                .fetch()
                .map(this::mapToDatedDigest);
    }

    public void setState(TournamentMemState tournament, DbUpdater batch) {
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
    public boolean isAdminOf(Uid uid, Tid tid) {
        return jooq.select(TOURNAMENT.TID)
                .from(TOURNAMENT).innerJoin(TOURNAMENT_ADMIN)
                .on(TOURNAMENT.TID.eq(TOURNAMENT_ADMIN.TID))
                .where(TOURNAMENT.TID.eq(tid),
                        TOURNAMENT_ADMIN.UID.eq(uid))
                .fetchOne() != null;
    }

    public Optional<MyTournamentInfo> getMyTournamentInfo(Tid tid) {
        return ofNullable(jooq.select(TOURNAMENT.NAME,
                TOURNAMENT.STATE, TOURNAMENT.PID, PLACE.NAME,
                TOURNAMENT.OPENS_AT, TOURNAMENT.TICKET_PRICE,
                TOURNAMENT.PREVIOUS_TID, TOURNAMENT.TYPE,
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
                        .type(r.get(TOURNAMENT.TYPE))
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
                        .previousTid(r.get(TOURNAMENT.PREVIOUS_TID).map(Tid::new))
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
                .where(TOURNAMENT.TYPE.eq(Classic),
                        TOURNAMENT.STATE.eq(Open)
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
    public Optional<DatedTournamentDigest> findMyNextTournament(Uid uid) {
        return ofNullable(jooq
                .select(TOURNAMENT.TID, TOURNAMENT.NAME, TOURNAMENT.OPENS_AT)
                .from(BID)
                .innerJoin(TOURNAMENT).on(BID.TID.eq(TOURNAMENT.TID))
                .where(BID.UID.eq(uid),
                        BID.STATE.in(Paid, Here, Want),
                        TOURNAMENT.TYPE.eq(Classic))
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
    public Optional<CompleteTournamentDigest> findMyPreviousTournament(Instant since, Uid uid) {
        return ofNullable(jooq.select(TOURNAMENT.TID, TOURNAMENT.NAME, BID.STATE)
                .from(BID)
                .innerJoin(TOURNAMENT).on(BID.TID.eq(TOURNAMENT.TID))
                .where(BID.UID.eq(uid),
                        BID.UPDATED.ge(Optional.of(since)),
                        BID.STATE.in(TERMINAL_BID_STATES),
                        TOURNAMENT.TYPE.eq(Classic))
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

    private List<DatedTournamentDigest> findCurrentTournaments(Uid uid) {
        return jooq.select(TOURNAMENT.TID, TOURNAMENT.NAME, TOURNAMENT.OPENS_AT)
                .from(BID)
                .innerJoin(TOURNAMENT).on(BID.TID.eq(TOURNAMENT.TID))
                .where(BID.UID.eq(uid),
                        BID.STATE.in(Play, Rest, Wait),
                        TOURNAMENT.TYPE.eq(Classic))
                .fetch()
                .map(r -> DatedTournamentDigest
                        .builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
                        .build());
    }

    private List<DatedTournamentDigest> findCurrentJudgeTournaments(Uid uid) {
        return jooq.select(TOURNAMENT.TID, TOURNAMENT.NAME, TOURNAMENT.OPENS_AT)
                .from(TOURNAMENT_ADMIN)
                .innerJoin(TOURNAMENT).on(TOURNAMENT_ADMIN.TID.eq(TOURNAMENT.TID))
                .where(TOURNAMENT_ADMIN.UID.eq(uid),
                        TOURNAMENT.TYPE.eq(Classic),
                        TOURNAMENT.STATE.in(Open))
                .fetch()
                .map(r -> DatedTournamentDigest
                        .builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public MyRecentTournaments findMyRecentTournaments(Instant since, Uid uid) {
        return MyRecentTournaments.builder()
                .next(findMyNextTournament(uid))
                .current(findCurrentTournaments(uid))
                .previous(findMyPreviousTournament(since, uid))
                .build();
    }


    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<DatedTournamentDigest> findMyNextJudgeTournament(Uid uid) {
        return ofNullable(jooq
                .select(TOURNAMENT.TID, TOURNAMENT.NAME, TOURNAMENT.OPENS_AT)
                .from(TOURNAMENT_ADMIN)
                .leftJoin(TOURNAMENT)
                .on(TOURNAMENT_ADMIN.TID.eq(TOURNAMENT.TID))
                .where(TOURNAMENT_ADMIN.UID.eq(uid),
                        TOURNAMENT.STATE.in(Announce, Draft),
                        TOURNAMENT.TYPE.eq(Classic))
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
    public Optional<DatedTournamentDigest> findMyPreviousJudgeTournament(
            Instant now, Uid uid) {
        return ofNullable(jooq
                .select(TOURNAMENT.TID, TOURNAMENT.NAME, TOURNAMENT.OPENS_AT)
                .from(TOURNAMENT_ADMIN)
                .leftJoin(TOURNAMENT).on(TOURNAMENT_ADMIN.TID.eq(TOURNAMENT.TID))
                .where(TOURNAMENT_ADMIN.UID.eq(uid),
                        TOURNAMENT.TYPE.eq(Classic),
                        TOURNAMENT.STATE.in(Close, Canceled, Replaced),
                        TOURNAMENT.COMPLETE_AT.gt(
                                Optional.of(now.minus(DAYS_TO_SHOW_PAST_TOURNAMENT, DAYS))))
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
    public MyRecentJudgedTournaments findMyRecentJudgedTournaments(Instant now, Uid uid) {
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
    public Optional<TournamentRules> getTournamentRules(Tid tid) {
        return ofNullable(jooq
                .select(TOURNAMENT.RULES)
                .from(TOURNAMENT)
                .where(TOURNAMENT.TID.eq(tid))
                .fetchOne())
                .map(r -> r.get(TOURNAMENT.RULES));
    }

    @Transactional(TRANSACTION_MANAGER)
    public void updateParams(Tid tid, TournamentRules rules, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(TOURNAMENT)
                        .set(TOURNAMENT.RULES, rules)
                        .where(TOURNAMENT.TID.eq(tid)))
                .build());
    }

    @Transactional(TRANSACTION_MANAGER)
    public Tid copy(CopyTournament copyTournament) {
        final Tid originTid = copyTournament.getOriginTid();
        final Tid newTid = copyTournamentRow(copyTournament);
        copyPermissions(originTid, newTid);
        return newTid;
    }

    private Tid copyTournamentRow(CopyTournament copyTournament) {
        final Tid originTid = copyTournament.getOriginTid();
        final MyTournamentInfo tInfo = getMyTournamentInfo(originTid)
                .orElseThrow(() -> notFound("Tournament " + originTid + " not found"));
        final TournamentRules rules = getTournamentRules(originTid)
                .orElseThrow(() -> notFound("Tournament " + originTid + " not found"));
        return justCreate(CreateTournament.builder()
                .name(copyTournament.getName())
                .rules(rules)
                .type(tInfo.getType())
                .opensAt(copyTournament.getOpensAt())
                .placeId(tInfo.getPlace().getPid())
                .ticketPrice(tInfo.getPrice())
                .previousTid(of(originTid))
                .build(), Draft);
    }

    private void copyPermissions(Tid originTid, Tid newTid) {
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

    public void setCompleteAt(Tid tid, Optional<Instant> now, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(TOURNAMENT)
                        .set(TOURNAMENT.COMPLETE_AT, now)
                        .where(TOURNAMENT.TID.eq(tid)))
                .build());
    }

    @Override
    public Set<Uid> loadAdmins(Tid tid) {
        return jooq.select(TOURNAMENT_ADMIN.UID)
                .from(TOURNAMENT_ADMIN)
                .where(TOURNAMENT_ADMIN.TID.eq(tid))
                .fetch()
                .stream()
                .map(r -> r.get(TOURNAMENT_ADMIN.UID))
                .collect(toSet());
    }

    public RelatedTids getRelatedTids(Tid tid) {
        return ofNullable(jooq.select(
                CHILD_TID_REL.CHILD_TID,
                MASTER_TID_REL.PARENT_TID)
                .from(TOURNAMENT)
                .leftJoin(CHILD_TID_REL)
                .on(TOURNAMENT.TID.eq(CHILD_TID_REL.PARENT_TID)
                        .and(CHILD_TID_REL.TYPE.eq(Console)))
                .leftJoin(MASTER_TID_REL)
                .on(TOURNAMENT.TID.eq(MASTER_TID_REL.CHILD_TID)
                        .and(MASTER_TID_REL.TYPE.eq(Console)))
                .where(TOURNAMENT.TID.eq(tid))
                .fetchOne())
                .map(r -> RelatedTids.builder()
                        .child(ofNullable(r.get(CHILD_TID_REL.CHILD_TID)))
                        .parent(ofNullable(r.get(MASTER_TID_REL.PARENT_TID)))
                        .build())
                .orElseThrow(() -> notFound("tournament " + tid + " not found"));
    }

    @Override
    public Optional<TournamentRow> getRow(Tid tid) {
        return ofNullable(jooq
                .select(TOURNAMENT.PID, TOURNAMENT.TYPE, TOURNAMENT.SPORT, TOURNAMENT.RULES,
                        TOURNAMENT.COMPLETE_AT, TOURNAMENT.OPENS_AT,
                        TOURNAMENT.NAME, TOURNAMENT.STATE, TOURNAMENT.TICKET_PRICE,
                        TOURNAMENT.PREVIOUS_TID)
                .from(TOURNAMENT)
                .where(TOURNAMENT.TID.eq(tid))
                .fetchOne())
                .map(r -> TournamentRow.builder()
                        .pid(r.get(TOURNAMENT.PID))
                        .sport(r.get(TOURNAMENT.SPORT))
                        .endedAt(r.get(TOURNAMENT.COMPLETE_AT))
                        .startedAt(r.get(TOURNAMENT.OPENS_AT))
                        .name(r.get(TOURNAMENT.NAME))
                        .type(r.get(TOURNAMENT.TYPE))
                        .state(r.get(TOURNAMENT.STATE))
                        .rules(r.get(TOURNAMENT.RULES))
                        .ticketPrice(r.get(TOURNAMENT.TICKET_PRICE))
                        .previousTid(r.get(TOURNAMENT.PREVIOUS_TID).map(Tid::new))
                        .tid(tid)
                        .build());
    }

    public void createRelation(Tid tid, Tid consoleTid) {
        jooq.insertInto(TOURNAMENT_RELATION,
                TOURNAMENT_RELATION.TYPE,
                TOURNAMENT_RELATION.PARENT_TID,
                TOURNAMENT_RELATION.CHILD_TID)
                .values(TournamentRelationType.Console, tid, consoleTid)
                .execute();
    }
}
