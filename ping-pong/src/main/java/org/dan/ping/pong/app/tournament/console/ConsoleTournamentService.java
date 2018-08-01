package org.dan.ping.pong.app.tournament.console;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.TERMINAL_STATE;
import static org.dan.ping.pong.app.castinglots.PlayOffLoserSelector.selectLosersForConsole;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.MasterOutcome;
import static org.dan.ping.pong.app.group.ConsoleTournament.INDEPENDENT_RULES;
import static org.dan.ping.pong.app.playoff.PlayOffGuests.JustLosers;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentType.Classic;
import static org.dan.ping.pong.app.tournament.TournamentType.Console;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConGru;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConOff;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.CreateTournament;
import org.dan.ping.pong.app.tournament.EnlistTournament;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentCache;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

public class ConsoleTournamentService {
    @Inject
    private GroupService groupService;

    @Inject
    private CategoryService categoryService;

    @Inject
    private TournamentService tournamentService;

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> tournamentRelations;

    @Inject
    private TournamentCache tournamentCache;

    @SneakyThrows
    public Tid getOrCreateConsoleFor(
            TournamentMemState masterTournament,
            TournamentRelationType type,
            UserInfo user, DbUpdater batch) {
        final RelatedTids relatedTids = tournamentRelations
                .get(masterTournament.getTid());
        return ofNullable(relatedTids.getChildren().get(type))
                .orElseGet(() -> createConsoleFor(masterTournament, type, user, batch));
    }

    private Tid createConsoleFor(
            TournamentMemState masterTournament,
            TournamentRelationType type,
            UserInfo user, DbUpdater batch) {
        validateCreation(masterTournament, type);
        final Tid consoleTid = tournamentService.create(user.getUid(),
                CreateTournament.builder()
                        .sport(masterTournament.getSport())
                        .ticketPrice(masterTournament.getTicketPrice())
                        .name(masterTournament.getName())
                        .placeId(masterTournament.getPid())
                        .opensAt(masterTournament.getOpensAt())
                        .previousTid(Optional.of(masterTournament.getTid()))
                        .state(Draft)
                        .type(Console)
                        .rules(TournamentRules.builder()
                                .place(masterTournament.getRule().getPlace())
                                .match(masterTournament.getRule().getMatch())
                                .playOff(masterTournament.getRule().getPlayOff())
                                .casting(masterTournament.getRule()
                                        .getCasting().withPolicy(MasterOutcome))
                                .rewards(Optional.empty())
                                .group(Optional.empty())
                                .build())
                        .build());

        tournamentDao.createRelation(masterTournament.getTid(), consoleTid, type);

        switch (type) {
            case ConGru:
                masterTournament.setRule(masterTournament.getRule()
                        .withGroup(masterTournament.getRule().getGroup()
                                .map(g -> g.withConsole(INDEPENDENT_RULES))));
                masterTournament.getRule().getGroup().ifPresent(groupRules ->
                        enlistPlayersFromCompleteGroups(masterTournament,
                                tournamentCache.load(consoleTid),
                                batch));
                break;
            case ConOff:
                masterTournament.setRule(masterTournament.getRule()
                        .withPlayOff(masterTournament.getRule().getPlayOff()
                                .map(p -> p
                                        .withConsoleParticipants(
                                                Optional.of(p.getConsoleParticipants()
                                                        .orElse(JustLosers)))
                                        .withConsole(INDEPENDENT_RULES))));
                masterTournament.getRule().getPlayOff().ifPresent(playOffRules ->
                        enlistPlayersFromCompleteCategories(masterTournament,
                                tournamentCache.load(consoleTid), batch));
                break;
            default:
                throw internalError("Type " + type + " not supported");
        }
        tournamentDao.updateParams(
                masterTournament.getTid(), masterTournament.getRule(), batch);
        tournamentRelations.invalidate(masterTournament.getTid());
        return consoleTid;
    }

    private void validateCreation(
            TournamentMemState masterTournament, TournamentRelationType type) {
        if (masterTournament.getType() != Classic) {
            throw badRequest("Tournament " + masterTournament.getType()
                    + " does not support console tournaments");
        }
        if (type == ConGru && !masterTournament.getRule().getGroup().isPresent()) {
            throw badRequest("Master tournament " + masterTournament.getTid()
                    + " has no groups");
        }
        if (type == ConOff && !masterTournament.getRule().getPlayOff().isPresent()) {
            throw badRequest("Master tournament " + masterTournament.getTid()
                    + " has no playOff");
        }
    }

    private void enlistPlayersFromCompleteCategories(TournamentMemState mTour,
            TournamentMemState conTour, DbUpdater batch) {
        mTour.matches()
                .filter(m -> !m.getGid().isPresent())
                .collect(groupingBy(MatchInfo::getCid, toList()))
                .forEach((mCid, mPlayOffBids) -> {
                    final Cid conCid = categoryService.findCidOrCreate(mTour, mCid, conTour, batch);
                    selectLosersForConsole(mTour, mPlayOffBids.stream())
                            .forEach((level, consoleBids) ->
                                    consoleBids.stream().map(mTour::getBidOrEx).forEach(par ->
                                            tournamentService.enlistToConsole(
                                                    par.getBid(),
                                                    EnlistTournament.builder()
                                                            .categoryId(conCid)
                                                            .bidState(Here)
                                                            .providedRank(Optional.empty())
                                                            .build(),
                                                    conTour, par, batch)));
                });
    }

    private void enlistPlayersFromCompleteGroups(TournamentMemState mTour,
            TournamentMemState conTour, DbUpdater batch) {
        final Map<Gid, List<MatchInfo>> matchesByGroup = groupService
                .groupMatchesByGroup(mTour);
        final Set<Gid> incompleteGroups = groupService.findIncompleteGroups(mTour);
        incompleteGroups.forEach(matchesByGroup::remove);

        matchesByGroup.forEach((gid, groupMatches) -> {
            final int quitsGroup = mTour.getRule().getGroup().get().getQuits();
            final List<Bid> orderedGroupUids = groupService.orderBidsInGroup(
                    gid, mTour, groupMatches);
            final Cid consoleCid = categoryService.findCidOrCreate(
                    mTour, gid, conTour, batch);

            orderedGroupUids.stream().skip(quitsGroup)
                    .map(mTour::getBidOrQuit)
                    .forEach(bid ->
                            tournamentService.enlistToConsole(
                                    bid.getBid(),
                                    EnlistTournament.builder()
                                            .categoryId(consoleCid)
                                            .bidState(
                                                    TERMINAL_STATE.contains(bid.getBidState())
                                                            ? bid.getBidState()
                                                            : Here)
                                            .providedRank(Optional.empty())
                                            .build(),
                                    conTour, bid, batch));
        });
    }
}
