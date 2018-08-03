package org.dan.ping.pong.app.tournament;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.ConsoleLayered;
import static org.dan.ping.pong.app.group.ConsoleTournament.NO;
import static org.dan.ping.pong.app.table.TableService.STATE;
import static org.dan.ping.pong.app.tournament.EnlistPolicy.ONCE_PER_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.app.tournament.TournamentType.Classic;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConGru;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConOff;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy;
import org.dan.ping.pong.app.group.ConsoleTournament;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.tournament.console.TournamentRelationType;
import org.dan.ping.pong.sys.error.PiPoEx;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

public class TournamentValidationService {
    private static final List<BidState> VALID_ENLIST_BID_STATES = asList(Want, Paid, Here);
    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> tournamentRelations;

    @Inject
    @Named(TOURNAMENT_CACHE)
    private LoadingCache<Tid, TournamentMemState> tournamentCache;

    static PiPoEx notDraftError(TournamentMemState tournament) {
        return badRequest("tournament-not-in-draft",
                ImmutableMap.of(STATE, tournament.getState(),
                        TournamentMemState.TID, tournament.getTid()));
    }

    @SneakyThrows
    public void validateBeginning(TournamentMemState tournament) {
        if (tournament.getState() != TournamentState.Draft) {
            throw notDraftError(tournament);
        }
        final ConsoleTournament group = tournament.getRule().consoleGroup();
        final ConsoleTournament playOff = tournament.getRule().consolePlayOff();
        if (group != NO || playOff != NO) {
            final RelatedTids relatedTids = tournamentRelations.get(tournament.getTid());
            validateChildBegin(group, relatedTids, ConGru);
            validateChildBegin(playOff, relatedTids, ConOff);
        }
    }

    @SneakyThrows
    public void validateChildBegin(ConsoleTournament con, RelatedTids relatedTids,
            TournamentRelationType relType) {
        switch (con) {
            case INDEPENDENT_RULES:
                final Tid childTid = relatedTids.child(relType);
                final TournamentMemState childT = tournamentCache.get(childTid);
                if (childT.getState() != Draft) {
                    throw badRequest("Child tournament " + childTid
                            + " not in Draft state but " + childT.getState());
                }
                break;
            case NO:
                // ok
                break;
            default:
                throw internalError("not implemented " + con);
        }
    }

    private void validateEnlist(TournamentMemState tournament, Enlist enlist, Uid uid) {
        if (tournament.getType() != Classic) {
            throw badRequest("Tournament does not allow direct enlistment");
        }

        tournament.getRule().getEnlist()
                .orElse(ONCE_PER_TOURNAMENT)
                .validate(tournament, enlist.getCid(), uid);

        tournament.checkCategory(enlist.getCid());
        final CastingLotsRule casting = tournament.getRule().getCasting();
        if (casting.getPolicy() == ParticipantRankingPolicy.ProvidedRating) {
            final int rank = enlist.getProvidedRank()
                    .orElseThrow(() -> badRequest("Ranking is required in",
                            TournamentMemState.TID, tournament.getTid()));
            casting.getProvidedRankOptions()
                    .orElseThrow(() -> internalError("no rank options"))
                    .validate(rank);
        } else {
            if (enlist.getProvidedRank().isPresent()) {
                throw badRequest("Provided ranking is not used",
                        TournamentMemState.TID, tournament.getTid());
            }
        }
    }

    public void validateEnlistOnline(TournamentMemState tournament, Enlist enlist, Uid uid) {
        if (tournament.getState() != Draft) {
            throw TournamentValidationService.notDraftError(tournament);
        }
        validateEnlist(tournament, enlist, uid);
    }

    @Inject
    private GroupService groupService;

    public void validateEnlistOffline(TournamentMemState tournament, EnlistOffline enlist) {
        if (tournament.getState() == Draft) {
            if (!VALID_ENLIST_BID_STATES.contains(enlist.getBidState())) {
                throw badRequest("Bid state could be "
                        + VALID_ENLIST_BID_STATES + " but " + enlist.getBidState());
            }
            enlist.getGroupId()
                    .ifPresent(gid -> { throw badRequest("group is not expected"); });
        } else if (tournament.getState() == Open) {
            if (enlist.getBidState() != Wait) {
                throw badRequest("Bid state should be Wait");
            }
            final Optional<Gid> ogid = enlist.getGroupId();
            if (ogid.isPresent()) {
                if (!groupService.isNotCompleteGroup(tournament, ogid.get())) {
                    throw badRequest("group is complete");
                }
            } else {
                groupService.ensureThatNewGroupCouldBeAdded(tournament, enlist.getCid());
            }
        } else {
            throw TournamentValidationService.notDraftError(tournament);
        }
        validateEnlist(tournament, enlist, enlist.getUid());
    }

    @SneakyThrows
    private void validateRulesOfConsoleTournament(TournamentRules rules) {
        rules.getGroup().ifPresent(gr -> {
            if (gr.getConsole() != NO) {
                throw badRequest("console group cannot have console tournament");
            }
        });
        rules.getPlayOff().ifPresent(pr -> {
            if (pr.getConsole() != NO) {
                throw badRequest("console playOff cannot have console tournament");
            }
            if (rules.getCasting().getSplitPolicy() == ConsoleLayered) {
                if (!pr.getLayerPolicy().isPresent()) {
                    throw badRequest("tournament for console layered playOff must have layer policy");
                }
            }
        });
    }

    public void validateRulesWithTournament(
            TournamentMemState tournament, TournamentRules rules) {
        switch (tournament.getType()) {
            case Classic:
                break; // ok
            case Console:
                validateRulesOfConsoleTournament(rules);
                break;
            default:
                throw internalError(
                        "Tournament type " + tournament.getType() + " not supported");
        }
    }
}
