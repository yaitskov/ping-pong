package org.dan.ping.pong.app.tournament.console;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.TERMINAL_STATE;
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
import org.dan.ping.pong.app.playoff.PlayOffGuests;
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
                break;
            case ConOff:
                masterTournament.setRule(masterTournament.getRule()
                        .withPlayOff(masterTournament.getRule().getPlayOff()
                                .map(p -> p
                                        .withConsoleParticipants(
                                                Optional.of(p.getConsoleParticipants()
                                                        .orElse(JustLosers)))
                                        .withConsole(INDEPENDENT_RULES))));
                break;
            default:
                throw internalError("Type " + type + " not supported");
        }
        tournamentDao.updateParams(masterTournament.getTid(), masterTournament.getRule(), batch);

        masterTournament.getRule().getGroup().ifPresent(groupRules ->
                enlistPlayersFromCompleteGroups(masterTournament,
                        tournamentCache.load(consoleTid),
                        batch));
        tournamentRelations.invalidate(masterTournament.getTid());

        return consoleTid;
    }

    private void validateCreation(TournamentMemState masterTournament, TournamentRelationType type) {
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

    private void enlistPlayersFromCompleteGroups(TournamentMemState masterTournament,
            TournamentMemState consoleTournament, DbUpdater batch) {
        final Map<Gid, List<MatchInfo>> matchesByGroup = groupService
                .groupMatchesByGroup(masterTournament);
        final Set<Gid> incompleteGroups = groupService.findIncompleteGroups(masterTournament);
        incompleteGroups.forEach(matchesByGroup::remove);

        matchesByGroup.forEach((gid, groupMatches) -> {
            final int quitsGroup = masterTournament.getRule().getGroup().get().getQuits();
            final List<Bid> orderedGroupUids = groupService.orderBidsInGroup(
                    gid, masterTournament, groupMatches);
            final Cid consoleCid = categoryService.findCidOrCreate(
                    masterTournament, gid, consoleTournament, batch);

            orderedGroupUids.stream().skip(quitsGroup)
                    .map(masterTournament::getBidOrQuit)
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
                                    consoleTournament, bid, batch));
        });
    }
}
