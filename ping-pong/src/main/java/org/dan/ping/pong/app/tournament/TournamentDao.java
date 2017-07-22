package org.dan.ping.pong.app.tournament;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.BID;
import static ord.dan.ping.pong.jooq.Tables.MATCHES;
import static ord.dan.ping.pong.jooq.Tables.PLACE;
import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT;
import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT_ADMIN;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Rest;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.tournament.TournamentState.Announce;
import static org.dan.ping.pong.app.tournament.TournamentState.Canceled;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentState.Hidden;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.app.tournament.TournamentState.Replaced;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.place.PlaceAddress;
import org.dan.ping.pong.app.place.PlaceLink;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectField;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public int create(int uid, CreateTournament newTournament) {
        final int tid = jooq.insertInto(TOURNAMENT, TOURNAMENT.STATE,
                TOURNAMENT.OPENS_AT,
                TOURNAMENT.PID,
                TOURNAMENT.PREVIOUS_TID,
                TOURNAMENT.QUITS_FROM_GROUP,
                TOURNAMENT.TICKET_PRICE,
                TOURNAMENT.THIRD_PLACE_MATCH,
                TOURNAMENT.NAME,
                TOURNAMENT.MAX_GROUP_SIZE)
                .values(Hidden,
                        newTournament.getOpensAt(),
                        newTournament.getPlaceId(),
                        newTournament.getPreviousTid(),
                        newTournament.getQuitsFromGroup(),
                        newTournament.getTicketPrice(),
                        newTournament.getThirdPlaceMatch(),
                        newTournament.getName(),
                        newTournament.getMaxGroupSize())
                .returning(TOURNAMENT.TID)
                .fetchOne()
                .getTid();
        log.info("User {} created tournament {}", uid, tid);

        jooq.insertInto(TOURNAMENT_ADMIN, TOURNAMENT_ADMIN.TID,
                TOURNAMENT_ADMIN.UID, TOURNAMENT_ADMIN.TYPE)
                .values(tid, uid, AUTHOR)
                .execute();
        return tid;
    }

    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public List<DatedTournamentDigest> findWritableForAdmin(int uid) {
        return selectDatedDigest()
                .innerJoin(TOURNAMENT_ADMIN)
                .on(TOURNAMENT.TID.eq(TOURNAMENT_ADMIN.TID))
                .where(TOURNAMENT_ADMIN.UID.eq(uid))
                .fetch()
                .map(this::mapToDatedDigest);
    }

    private <T extends Record> DatedTournamentDigest mapToDatedDigest(T r) {
        return DatedTournamentDigest.builder()
                .name(r.get(TOURNAMENT.NAME))
                .tid(r.get(TOURNAMENT.TID))
                .opensAt(r.get(TOURNAMENT.OPENS_AT))
                .build();
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<DatedTournamentDigest> findEnlistedIn(int uid) {
        return selectDatedDigest()
                .innerJoin(BID)
                .on(TOURNAMENT.TID.eq(BID.TID))
                .where(BID.UID.eq(uid), BID.STATE.ne(Quit))
                .fetch()
                .map(this::mapToDatedDigest);
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

    @Transactional(TRANSACTION_MANAGER)
    public void setState(int tid, TournamentState state) {
        log.info("Switch tournament {} into {} state.", tid, state);
        jooq.update(TOURNAMENT).set(TOURNAMENT.STATE, state)
                .where(TOURNAMENT.TID.eq(tid))
                .execute();
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<TournamentInfo> lockById(int tid) {
        return ofNullable(jooq.select(TOURNAMENT.QUITS_FROM_GROUP,
                TOURNAMENT.STATE, TOURNAMENT.MAX_GROUP_SIZE)
                .from(TOURNAMENT)
                .where(TOURNAMENT.TID.eq(tid))
                .forUpdate().fetchOne())
                .map(r -> TournamentInfo.builder()
                        .tid(tid)
                        .state(r.get(TOURNAMENT.STATE))
                        .maxGroupSize(r.get(TOURNAMENT.MAX_GROUP_SIZE))
                        .quitesFromGroup(r.get(TOURNAMENT.QUITS_FROM_GROUP))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<TournamentInfo> getById(int tid) {
        return ofNullable(jooq.select(TOURNAMENT.QUITS_FROM_GROUP,
                TOURNAMENT.STATE, TOURNAMENT.MAX_GROUP_SIZE)
                .from(TOURNAMENT)
                .where(TOURNAMENT.TID.eq(tid))
                .fetchOne())
                .map(r -> TournamentInfo.builder()
                        .tid(tid)
                        .state(r.get(TOURNAMENT.STATE))
                        .maxGroupSize(r.get(TOURNAMENT.MAX_GROUP_SIZE))
                        .quitesFromGroup(r.get(TOURNAMENT.QUITS_FROM_GROUP))
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

    @Transactional(TRANSACTION_MANAGER)
    public boolean tryToCompleteTournament(int tid) {
        final int categoriesLeft = jooq.selectCount()
                .from(MATCHES)
                .where(MATCHES.TID.eq(tid),
                        MATCHES.STATE.ne(MatchState.Over))
                .fetchOne()
                .value1();
        if (categoriesLeft == 0) {
            log.info("All matches of tid {} are complete", tid);
            return jooq.update(TOURNAMENT)
                    .set(TOURNAMENT.STATE, TournamentState.Close)
                    .where(TOURNAMENT.TID.eq(tid))
                    .execute() > 0;
        }
        return false;
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<DraftingTournamentInfo> getDraftingTournament(int tid,
            Optional<Integer> participantId) {
        return ofNullable(jooq.select(TOURNAMENT.NAME, TOURNAMENT.OPENS_AT,
                BID.CID, TOURNAMENT_ADMIN.UID, TOURNAMENT.TICKET_PRICE,
                TOURNAMENT.PREVIOUS_TID, PLACE.PID, PLACE.POST_ADDRESS,
                PLACE.NAME, PLACE.PHONE, TOURNAMENT.STATE,
                jooq.selectCount().from(BID)
                        .where(BID.TID.eq(TOURNAMENT.TID),
                                BID.STATE.ne(Quit))
                        .asField(ENLISTED))
                .from(TOURNAMENT)
                .innerJoin(PLACE).on(TOURNAMENT.PID.eq(PLACE.PID))
                .leftJoin(TOURNAMENT_ADMIN)
                .on(TOURNAMENT.TID.eq(TOURNAMENT_ADMIN.TID),
                        TOURNAMENT_ADMIN.UID.eq(participantId.orElse(0)))
                .leftJoin(BID)
                .on(TOURNAMENT.TID.eq(BID.TID),
                        BID.STATE.ne(Quit),
                        BID.UID.eq(participantId.orElse(0)))
                .where(TOURNAMENT.TID.eq(tid))
                .fetchOne())
                .map(r -> DraftingTournamentInfo.builder()
                        .tid(tid)
                        .name(r.get(TOURNAMENT.NAME))
                        .state(r.get(TOURNAMENT.STATE))
                        .ticketPrice(r.get(TOURNAMENT.TICKET_PRICE))
                        .previousTid(r.get(TOURNAMENT.PREVIOUS_TID))
                        .alreadyEnlisted(r.get(ENLISTED, Integer.class))
                        .place(PlaceLink.builder()
                                .name(r.get(PLACE.NAME))
                                .address(PlaceAddress.builder()
                                        .address(r.get(PLACE.POST_ADDRESS))
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
        return ofNullable(jooq.select(TOURNAMENT.QUITS_FROM_GROUP,
                TOURNAMENT.STATE, TOURNAMENT.MAX_GROUP_SIZE, TOURNAMENT.NAME)
                .from(TOURNAMENT)
                .where(TOURNAMENT.TID.eq(tid))
                .fetchOne())
                .map(r -> MyTournamentInfo.builder()
                        .tid(tid)
                        .name(r.get(TOURNAMENT.NAME))
                        .state(r.get(TOURNAMENT.STATE))
                        .maxGroupSize(r.get(TOURNAMENT.MAX_GROUP_SIZE))
                        .quitesFromGroup(r.get(TOURNAMENT.QUITS_FROM_GROUP))
                        .build());

    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<OpenTournamentDigest> findRunning() {
        return jooq.select(TOURNAMENT.TID, TOURNAMENT.OPENS_AT, TOURNAMENT.NAME,
                DSL.selectCount().from(BID)
                        .where(BID.TID.eq(TOURNAMENT.TID))
                        .asField(PARTICIPANTS),
                DSL.selectCount().from(MATCHES).where(MATCHES.TID.eq(TOURNAMENT.TID))
                        .asField(GAMES),
                DSL.selectCount().from(MATCHES).where(MATCHES.TID.eq(TOURNAMENT.TID),
                        MATCHES.STATE.eq(MatchState.Over))
                        .asField(GAMES_COMPLETE))
                .from(TOURNAMENT)
                .where(TOURNAMENT.STATE.eq(Open))
                .orderBy(TOURNAMENT.OPENS_AT)
                .fetch()
                .map(r -> OpenTournamentDigest.builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .startedAt(r.get(TOURNAMENT.OPENS_AT))
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
    public Optional<DatedTournamentDigest> findMyPreviousTournament(Instant now, int uid) {
        return ofNullable(jooq.select(TOURNAMENT.TID, TOURNAMENT.NAME, TOURNAMENT.OPENS_AT)
                .from(BID)
                .innerJoin(TOURNAMENT).on(BID.TID.eq(TOURNAMENT.TID))
                .where(BID.UID.eq(uid),
                        BID.STATE.in(Win1, Win2, Win3, Expl, Lost, Quit),
                        TOURNAMENT.OPENS_AT.gt(now.minus(DAYS_TO_SHOW_PAST_TOURNAMENT, DAYS)))
                .orderBy(TOURNAMENT.OPENS_AT.desc())
                .limit(1)
                .fetchOne())
                .map(r -> DatedTournamentDigest
                        .builder()
                        .tid(r.get(TOURNAMENT.TID))
                        .name(r.get(TOURNAMENT.NAME))
                        .opensAt(r.get(TOURNAMENT.OPENS_AT))
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
    public MyRecentTournaments findMyRecentTournaments(Instant now, int uid) {
        return MyRecentTournaments.builder()
                .next(findMyNextTournament(uid))
                .current(findCurrentTournaments(uid))
                .previous(findMyPreviousTournament(now, uid))
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
                        TOURNAMENT.OPENS_AT.gt(now.minus(DAYS_TO_SHOW_PAST_TOURNAMENT, DAYS)))
                .orderBy(TOURNAMENT.OPENS_AT.desc())
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
    public MyRecentTournaments findMyRecentJudgedTournaments(Instant now, int uid) {
        return MyRecentTournaments.builder()
                .next(findMyNextJudgeTournament(uid))
                .current(findCurrentJudgeTournaments(uid))
                .previous(findMyPreviousJudgeTournament(now, uid))
                .build();
    }
}
