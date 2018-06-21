package org.dan.ping.pong.app.tournament.marshaling;

import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.db.DbStrictUpdater.DB_STRICT_UPDATER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.NewCategory;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.tournament.CreateTournament;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserRegRequest;
import org.dan.ping.pong.sys.error.PiPoEx;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class TournamentMarshalingService {
    @Inject
    private Clocker clocker;

    public TournamentEnvelope exportState(TournamentMemState tournament) {
        return TournamentEnvelope
                .builder()
                .exportedAt(clocker.get())
                .tournament(
                        ExportedTournamentJune_8th_2018
                                .builder()
                                .name(tournament.getName())
                                .type(tournament.getType())
                                .state(tournament.getState())
                                .opensAt(tournament.getOpensAt())
                                .completeAt(tournament.getCompleteAt())
                                .sport(tournament.getSport())
                                .rule(tournament.getRule())
                                .groups(tournament.getGroups())
                                .participants(tournament.getParticipants())
                                .matches(tournament.getMatches())
                                .categories(tournament.getCategories())
                                .ticketPrice(tournament.getTicketPrice())
                                .build())
                .build();
    }

    @Inject
    private TournamentDao tournamentDao;
    @Inject
    private TournamentService tournamentService;

    @Transactional(TRANSACTION_MANAGER)
    public Tid importState(CreateTournament newTournament,
            Uid importerUid, ExportedTournament expTournament) {
        final Tid tid = tournamentService.create(importerUid, newTournament);
        expTournament.setTid(tid);
        createCategories(expTournament);
        createGroups(expTournament);
        final StrictUniMap<Uid> users = createUsers(expTournament);
        enlistUsers(users, expTournament);
        createMatches(users, expTournament);
        fillSets(expTournament);
        return tid;
    }

    private void fillSets(ExportedTournament expTournament) {
        expTournament.getMatches().values().forEach(
                m -> matchDao.insertScores(m, DB_STRICT_UPDATER));
    }

    @Inject
    private BidDao bidDao;

    private void enlistUsers(
            StrictUniMap<Uid> users,
            ExportedTournament expTournament) {
        expTournament.getParticipants().values()
                .forEach(pa -> {
                    pa.setTid(expTournament.getTid());
                    pa.setUid(users.apply(pa.getUid()));
                    pa.setCid(pa.getCid());
                    pa.setGid(pa.getGid());
                    bidDao.enlist(pa, Optional.of(pa.getUid().getId()), DB_STRICT_UPDATER);
                });
    }

    @Inject
    private MatchDao matchDao;

    private void createMatches(StrictUniMap<Uid> users,
            ExportedTournament expTournament) throws PiPoEx {
        expTournament.getMatches().values().forEach(mi -> {
            mi.setTid(expTournament.getTid());
            mi.setWinnerId(mi.getWinnerId().map(users));
            mi.replaceParticipantUids(users);
            matchDao.createMatch(mi, DB_STRICT_UPDATER);
        });
    }

    @Inject
    private GroupDao groupDao;

    private void createGroups(
            ExportedTournament expTournament) {
        expTournament.getGroups().values().forEach(
                gi -> groupDao.createGroup(
                        gi.getGid(), DB_STRICT_UPDATER, expTournament.getTid(),
                        gi.getCid(), gi.getLabel(), 1));
    }

    @Inject
    private CategoryDao categoryDao;

    private void createCategories(ExportedTournament expTournament) {
        expTournament.getCategories().values().forEach(
                category ->
                        categoryDao.create(
                                NewCategory.builder()
                                        .name(category.getName())
                                        .cid(category.getCid())
                                        .tid(expTournament.getTid())
                                        .build(),
                                DB_STRICT_UPDATER));
    }

    @Inject
    private UserDao userDao;

    private StrictUniMap<Uid> createUsers(ExportedTournament expTournament) {
        return StrictUniMap.of(
                "users",
                expTournament.getParticipants().values().stream()
                        .collect(toMap(
                                ParticipantMemState::getUid,
                                pa -> userDao.register(UserRegRequest.builder()
                                        .name(pa.getName())
                                        .build()))));
    }
}
