package org.dan.ping.pong.app.castinglots.rank;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GroupSplitPolicy {
    /**
     * group A => (p1, p4), group B => (p2, p3)
     */
    @JsonProperty("bm")
    BalancedMix,
    /**
     * group A => (p1, p2) group B => (p3, p4)
     */
    @JsonProperty("btb")
    BestToBest,
    /**
     *  group A => (master group A 2, master group B 2),
     *  group B => (master group A 3, ,master group B 3)
     *
     *  though actually console tournament in that case does not have groups.
     */
    @JsonProperty("cl")
    ConsoleLayered
}
