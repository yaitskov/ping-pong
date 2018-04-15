package org.dan.ping.pong.app.match.rule.service;

import static java.util.stream.Collectors.toMap;

import org.dan.ping.pong.app.match.rule.GroupParticipantOrderService;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleService;
import org.dan.ping.pong.app.match.rule.service.common.DirectOutcomeRuleService;
import org.dan.ping.pong.app.match.rule.service.common.WonMatchesRuleService;
import org.dan.ping.pong.app.match.rule.service.common.LostBallsRuleService;
import org.dan.ping.pong.app.match.rule.service.common.LostSetsRuleService;
import org.dan.ping.pong.app.match.rule.service.common.PickRandomlyRuleService;
import org.dan.ping.pong.app.match.rule.service.common.SetsBalanceRuleService;
import org.dan.ping.pong.app.match.rule.service.common.WonBallsRuleService;
import org.dan.ping.pong.app.match.rule.service.common.WonSetsRuleService;
import org.dan.ping.pong.app.match.rule.service.meta.CountDisambiguationMatchesRuleService;
import org.dan.ping.pong.app.match.rule.service.meta.DisambiguationPreviewRuleService;
import org.dan.ping.pong.app.match.rule.service.ping.CountJustPunktsRuleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

@Import({WonMatchesRuleService.class,
        PickRandomlyRuleService.class,
        CountJustPunktsRuleService.class,
        SetsBalanceRuleService.class,
        WonSetsRuleService.class,
        LostSetsRuleService.class,
        BallsBalanceRuleService.class,
        WonBallsRuleService.class,
        LostBallsRuleService.class,
        DirectOutcomeRuleService.class,
        CountDisambiguationMatchesRuleService.class,
        DisambiguationPreviewRuleService.class,
        GroupParticipantOrderService.class
})
public class GroupOrderRuleServiceCtx {
    public static final String COUNT_WON_MATCHES = "countWonMatches";
    public static final String USE_BALLS = "useBalls";
    public static final String USE_SETS = "useSets";
    public static final String COUNT_DM_MATCHES = "countDmMatches";
    public static final String COUNT_JUST_PUNKTS = "countJustPunkts";
    public static final String F2F = "f2f";
    public static final String RANDOM = "rnd";

    public static final String GROUP_ORDER_SERVICES_BY_RULE_NAME = "groupOrderServicesByRuleName";

    @Bean(name = GROUP_ORDER_SERVICES_BY_RULE_NAME)
    public Map<OrderRuleName, GroupOrderRuleService> groupOrderServicesByRuleName(
            List<GroupOrderRuleService> services) {
        return services.stream()
                .collect(toMap(GroupOrderRuleService::getName, s -> s));
    }
}
