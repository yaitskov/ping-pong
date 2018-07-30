package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BalancedMix;
import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BestToBest;
import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.ConsoleLayered;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.Manual;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.MasterOutcome;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.ProvidedRating;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.SignUp;

import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.castinglots.rank.OrderDirection;
import org.dan.ping.pong.app.castinglots.rank.ProvidedRankOptions;

import java.util.Optional;

public class CastingLotsRulesConst {
    public static final CastingLotsRule LAYERED_CONSOLE_CASTING
            = CastingLotsRule.builder()
            .policy(MasterOutcome)
            .direction(OrderDirection.Increase)
            .splitPolicy(ConsoleLayered)
            .build();
    public static final CastingLotsRule INCREASE_SIGNUP_CASTING
            = CastingLotsRule.builder()
            .policy(SignUp)
            .direction(OrderDirection.Increase)
            .splitPolicy(BestToBest)
            .build();
    public static final CastingLotsRule INCREASE_SIGNUP_MIX
            = CastingLotsRule.builder()
            .policy(SignUp)
            .direction(OrderDirection.Increase)
            .splitPolicy(BalancedMix)
            .build();
    public static final CastingLotsRule BALANCED_MANUAL
            = CastingLotsRule.builder()
            .policy(Manual)
            .direction(OrderDirection.Increase)
            .splitPolicy(BalancedMix)
            .build();
    public static final CastingLotsRule INCREASE_PROVIDED_RANKING
            = CastingLotsRule.builder()
            .policy(ProvidedRating)
            .direction(OrderDirection.Increase)
            .splitPolicy(BestToBest)
            .providedRankOptions(Optional.of(ProvidedRankOptions
                    .builder()
                    .label("TEST")
                    .minValue(1)
                    .maxValue(10)
                    .build()))
            .build();
}
