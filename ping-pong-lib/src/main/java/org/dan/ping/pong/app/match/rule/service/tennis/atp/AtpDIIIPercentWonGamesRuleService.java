package org.dan.ping.pong.app.match.rule.service.tennis.atp;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDIII;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingDoubleScalarReason.ofDoubleD;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.util.FuncUtils.SUM_INT;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.sport.tennis.TennisMatchRules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class AtpDIIIPercentWonGamesRuleService implements GroupOrderRuleService {

    @Override
    public OrderRuleName getName() {
        return AtpDIII;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Uid> _uids, GroupOrderRule _rule, GroupRuleParams params) {
        final Map<Uid, Integer> uid2WonBalls = new TreeMap<>();
        final Map<Uid, Integer> uid2LostBalls = new HashMap<>();

        matches.get().forEach(m -> {
            final Uid[] uids = m.uidsArray();
            final TennisMatchRules rules = (TennisMatchRules) params
                    .getTournament().selectMatchRule(m);

            final List<Integer> games0 = m.getParticipantScore(uids[0]);
            final List<Integer> games1 = m.getParticipantScore(uids[1]);
            for (int i = 0; i < games0.size(); ++i) {
                if (rules.isSuperTieBreak(i)) {
                    uid2WonBalls.merge(uids[0],
                            games0.get(i) > games1.get(i) ? 1 : 0,
                            SUM_INT);
                    uid2WonBalls.merge(uids[1],
                            games0.get(i) < games1.get(i) ? 1 : 0,
                            SUM_INT);
                } else {
                    uid2WonBalls.merge(uids[0], games0.get(i), SUM_INT);
                    uid2WonBalls.merge(uids[1], games1.get(i), SUM_INT);

                    uid2LostBalls.merge(uids[0], games1.get(i), SUM_INT);
                    uid2LostBalls.merge(uids[1], games0.get(i), SUM_INT);
                }
            }
        });

        return Optional.of(uid2WonBalls.entrySet()
                .stream()
                .map((e) -> ofDoubleD(
                        e.getKey(),
                        e.getValue().doubleValue() / (e.getValue()
                                + ofNullable(uid2LostBalls.get(e.getKey()))
                                .orElseThrow(() -> internalError(
                                        "no lost " + e.getKey()))),
                        getName()))
                .sorted());
    }
}
