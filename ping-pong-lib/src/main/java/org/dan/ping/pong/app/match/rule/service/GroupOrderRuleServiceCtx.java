package org.dan.ping.pong.app.match.rule.service;

import static java.util.stream.Collectors.toMap;

import org.dan.ping.pong.app.match.rule.GroupParticipantOrderService;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleService;
import org.dan.ping.pong.app.match.rule.service.common.DirectOutcomeRuleService;
import org.dan.ping.pong.app.match.rule.service.common.OrderUidRuleService;
import org.dan.ping.pong.app.match.rule.service.common.WonMatchesRuleService;
import org.dan.ping.pong.app.match.rule.service.common.LostBallsRuleService;
import org.dan.ping.pong.app.match.rule.service.common.LostSetsRuleService;
import org.dan.ping.pong.app.match.rule.service.common.PickRandomlyRuleService;
import org.dan.ping.pong.app.match.rule.service.common.SetsBalanceRuleService;
import org.dan.ping.pong.app.match.rule.service.common.WonBallsRuleService;
import org.dan.ping.pong.app.match.rule.service.common.WonSetsRuleService;
import org.dan.ping.pong.app.match.rule.service.meta.UseDisambiguationMatchesDirectiveService;
import org.dan.ping.pong.app.match.rule.service.meta.DisambiguationPreviewRuleService;
import org.dan.ping.pong.app.match.rule.service.ping.CountJustPunktsRuleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

@Import({WonMatchesRuleService.class,
        PickRandomlyRuleService.class,
        OrderUidRuleService.class,
        CountJustPunktsRuleService.class,
        SetsBalanceRuleService.class,
        WonSetsRuleService.class,
        LostSetsRuleService.class,
        BallsBalanceRuleService.class,
        WonBallsRuleService.class,
        LostBallsRuleService.class,
        DirectOutcomeRuleService.class,
        UseDisambiguationMatchesDirectiveService.class,
        DisambiguationPreviewRuleService.class,
        GroupParticipantOrderService.class
})
public class GroupOrderRuleServiceCtx {
    public static final String WON_MATCHES = "WM";
    public static final String BALLS_BALANCE = "BB";
    public static final String SETS_BALANCE = "SB";
    public static final String DM_MATCHES = "DM";
    public static final String PUNKTS = "Punkts";
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
