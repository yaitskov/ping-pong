package org.dan.ping.pong.app.match.rule.service.tennis.atp;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDII;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.match.rule.service.common.LostSetsRuleService;
import org.dan.ping.pong.app.match.rule.service.common.WonSetsRuleService;

import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

public class AtpDIIPercentWonSetsRuleService extends BasePercentRuleService {
    @Inject
    @Named("wonSetsRuleService")
    private WonSetsRuleService wonSetsRuleService;

    @Inject
    private LostSetsRuleService lostSetsRuleService;

    @Override
    public OrderRuleName getName() {
        return AtpDII;
    }

    @Override
    protected Map<Bid, Integer> findBid2LostSets(Stream<MatchInfo> matches, GroupRuleParams params) {
        return lostSetsRuleService.findUid2Sets(matches, params);
    }

    @Override
    protected Map<Bid, Integer> findBid2WonSets(Stream<MatchInfo> matches, GroupRuleParams params) {
        return wonSetsRuleService.findUid2Sets(matches, params);
    }
}
