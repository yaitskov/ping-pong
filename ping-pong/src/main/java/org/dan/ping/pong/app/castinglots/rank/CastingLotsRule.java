package org.dan.ping.pong.app.castinglots.rank;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BestToBest;
import static org.dan.ping.pong.app.castinglots.rank.OrderDirection.Increase;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.SignUp;
import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BalancedMix;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CastingLotsRule {
    private ParticipantRankingPolicy policy;
    private OrderDirection direction;
    private GroupSplitPolicy splitPolicy;
    private Optional<ProvidedRankOptions> providedRankOptions;

    public static class CastingLotsRuleBuilder {
        ParticipantRankingPolicy policy = SignUp;
        OrderDirection direction = Increase;
        GroupSplitPolicy splitPolicy = BestToBest;
        Optional<ProvidedRankOptions> providedRankOptions = Optional.empty();
    }
}
