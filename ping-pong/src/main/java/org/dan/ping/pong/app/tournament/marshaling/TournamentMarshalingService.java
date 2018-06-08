package org.dan.ping.pong.app.tournament.marshaling;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.db.DbStrictUpdater.DB_STRICT_UPDATER;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.category.NewCategory;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.Mid;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
        final StrictUniMap<Integer> categories = createCategories(expTournament);
        final StrictUniMap<Integer> groups = createGroups(categories, expTournament);
        final StrictUniMap<Uid> users = createUsers(expTournament);
        enlistUsers(categories, groups, users, expTournament);
        createMatches(categories, groups, users, expTournament);
        return tid;
    }

    @Inject
    private BidDao bidDao;

    private void enlistUsers(
            StrictUniMap<Integer> categories,
            StrictUniMap<Integer> groups,
            StrictUniMap<Uid> users,
            ExportedTournament expTournament) {
        expTournament.getParticipants().values()
                .forEach(pa -> {
                    pa.setUid(users.apply(pa.getUid()));
                    pa.setCid(categories.apply(pa.getCid()));
                    pa.setGid(pa.getGid().map(groups));
                    bidDao.enlist(pa, Optional.of(pa.getUid().getId()), DB_STRICT_UPDATER);
                });
    }

    private Map<Boolean, List<MatchInfo>> resolvableMatches(
            Stream<MatchInfo> s, Predicate<Mid> resolvableP) {
        return s.collect(groupingBy(
                mi -> Stream.of((Supplier<Optional<Mid>>) mi::getWinnerMid, mi::getLoserMid)
                        .allMatch(me -> me.get()
                                .map(resolvableP::test)
                                .orElse(true))));
    }

    @Inject
    private MatchDao matchDao;

    private void createMatches(
            StrictUniMap<Integer> categories,
            StrictUniMap<Integer> groups,
            StrictUniMap<Uid> users,
            ExportedTournament expTournament) throws PiPoEx {
        final Map<Mid, Mid> matchIds = new HashMap<>();
        final StrictUniMap<Mid> matches = StrictUniMap.of("matches", matchIds);
        Stream<MatchInfo> matchStream = expTournament.getMatches().values().stream();
        for (;;) {
            final Map<Boolean, List<MatchInfo>> resGroupsOfMatches = resolvableMatches(
                    matchStream, matchIds::containsKey);
            final List<MatchInfo> resMatches = ofNullable(resGroupsOfMatches.get(true))
                    .orElse(emptyList());
            matchIds.putAll(
                    resMatches
                            .stream()
                            .peek(mi -> {
                                mi.setTid(expTournament.getTid());
                                mi.setWinnerMid(mi.getWinnerMid().map(matches));
                                mi.setLoserMid(mi.getLoserMid().map(matches));
                                mi.setCid(categories.apply(mi.getCid()));
                                mi.setGid(mi.getGid().map(groups));
                                mi.setWinnerId(mi.getWinnerId().map(users));
                                mi.getParticipantUids(users);
                            })
                            .collect(toMap(MatchInfo::getMid, matchDao::createMatch)));
            final List<MatchInfo> nonResMatches = ofNullable(resGroupsOfMatches.get(false))
                    .orElse(emptyList());
            log.info("Left matches {} / {}",
                    matchIds.size(), nonResMatches.size());
            if (nonResMatches.isEmpty()) {
                break;
            }
            if (resMatches.isEmpty()) {
                throw internalError("Import of matches loops");
            }
            matchStream = nonResMatches.stream();
        }
    }

    @Inject
    private GroupDao groupDao;

    private StrictUniMap<Integer> createGroups(
            StrictUniMap<Integer> categories,
            ExportedTournament expTournament) {
        return StrictUniMap.of(
                "groups",
                expTournament.getGroups().values()
                        .stream()
                        .collect(toMap(
                                GroupInfo::getGid,
                                gi -> groupDao.createGroup(
                                        expTournament.getTid(),
                                        categories.apply(gi.getCid()),
                                        gi.getLabel(),
                                        1,
                                        gi.getOrdNumber()))));
    }

    @Inject
    private CategoryDao categoryDao;

    private StrictUniMap<Integer> createCategories(ExportedTournament expTournament) {
        return StrictUniMap.of(
                "categories",
                expTournament.getCategories().values().stream()
                        .collect(toMap(CategoryLink::getCid,
                                category ->
                                        categoryDao.create(NewCategory.builder()
                                                .name(category.getName())
                                                .tid(expTournament.getTid())
                                                .build()))));
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
