package org.dan.ping.pong.app.match.rule.service.tennis.atp;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDI;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofIntI;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.util.FuncUtils.SUM_INT;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

public class AtpDIRuleService implements GroupOrderRuleService {
    @Inject
    private Sports sports;

    @Override
    public OrderRuleName getName() {
        return AtpDI;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Uid> uids,
            GroupOrderRule _rule, GroupRuleParams params) {
        if (uids.size() != 3) {
            return Optional.empty();
        }

        final Map<Uid, Integer> wons = new TreeMap<>();
        final Map<Uid, Integer> walkOvers = new TreeMap<>();
        final TournamentMemState tournament = params.getTournament();

        matches.get().forEach(m -> {
            final Map<Uid, Integer> uid2Sets = sports.calcWonSets(tournament, m);
            final Optional<Uid> oUid = sports.findWinnerId(
                    tournament.selectMatchRule(m), uid2Sets);
            oUid.filter(uids::contains)
                    .ifPresent(uid -> wons.merge(uid, 1, SUM_INT));
            if (m.getState() == Over && !oUid.equals(m.getWinnerId())) {
                uid2Sets.keySet().stream()
                        .filter(uids::contains)
                        .forEach(uid -> walkOvers.merge(uid, 1, SUM_INT));
            }
        });

        if (wons.size() == 3
                && walkOvers.size() > 0
                && walkOvers.size() < 3
                && wons.values().stream().collect(toSet()).equals(singleton(1))) {
            if (walkOvers.size() == 2) {
                return Optional.of(Stream.concat(
                        Stream.of(ofIntI(
                                wons.keySet().stream()
                                        .filter(u -> !walkOvers.containsKey(u))
                                        .findAny()
                                        .orElseThrow(() -> internalError("all walk over")),
                                1,
                                getName())),
                        walkOvers.keySet().stream()
                                .map(u -> ofIntI(u, 2, getName()))));
            } else if (walkOvers.size() == 1) {
                // not clear in the point d.i: it tells that out come must be 1-2,
                // why 1-2 and not 2-1 ? and after all i, ii, iii and iv
                // point v tels that head-to-head applied as a fallback
                return Optional.of(
                        Stream.concat(
                                wons.keySet().stream()
                                        .filter(u -> !walkOvers.containsKey(u))
                                        .map(u -> ofIntI(u, 1, getName())),
                                walkOvers.keySet().stream()
                                        .map(u -> ofIntI(u, 2, getName()))));
            } else {
                throw internalError("walks overs " + walkOvers.size());
            }
        }
        return Optional.empty();
    }
}
