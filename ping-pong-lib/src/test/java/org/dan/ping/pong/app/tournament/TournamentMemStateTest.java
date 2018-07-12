package org.dan.ping.pong.app.tournament;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.dan.ping.pong.app.group.GroupRulesConst.DM_ORDER_RULES_S2A2G11;
import static org.dan.ping.pong.app.group.GroupRulesConst.G8Q1;
import static org.dan.ping.pong.app.group.GroupServiceTest.BID2;
import static org.dan.ping.pong.app.group.GroupServiceTest.UID2;
import static org.dan.ping.pong.app.match.MatchInfoConst.GROUP_DM_MATCH;
import static org.dan.ping.pong.app.match.MatchInfoConst.GROUP_ORIGIN_MATCH;
import static org.dan.ping.pong.app.match.MatchInfoConst.PLAY_OFF_MATCH_HF;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID4;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.dan.ping.pong.app.tournament.PlayOffRulesConst.L1_S3A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S1A2G11;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

public class TournamentMemStateTest {
    public static final TournamentRules G8Q1_S1A2G11_L1_S3A2G11 = RULES_G8Q1_S1A2G11.withPlayOff(Optional.of(L1_S3A2G11));
    public static final TournamentRules G8Q1_S1AG11_DM_S2A2G11 = RULES_G8Q1_S1A2G11.withGroup(
            Optional.of(G8Q1.withOrderRules(DM_ORDER_RULES_S2A2G11)));
    private final Cid CID = Cid.of(3);
    private final Gid gid1 = Gid.of(1);
    private final Gid gid2 = Gid.of(2);

    private Map<Bid, ParticipantMemState> participants =
            ImmutableMap.<Bid, ParticipantMemState>builder()
                    .put(BID2, ParticipantMemState
                            .builder()
                            .uid(UID2)
                            .bid(BID2)
                            .cid(CID)
                            .gid(Optional.of(gid1))
                            .build())
                    .put(BID3, ParticipantMemState
                            .builder()
                            .bid(BID3)
                            .uid(UID3)
                            .cid(CID)
                            .gid(Optional.of(gid2))
                            .build())
                    .put(BID4, ParticipantMemState
                            .builder()
                            .uid(UID4)
                            .bid(BID4)
                            .cid(CID)
                            .gid(Optional.empty())
                            .build())
                    .build();

    private TournamentMemState tournament = TournamentMemState
            .builder()
            .participants(participants)
            .build();

    @Test
    public void uidsInGroup() {
        assertThat(tournament.bidsInGroup(gid1), is(singleton(BID2)));
    }

    @Test
    public void uidsInCategory() {
        assertThat(tournament.bidsInCategory(Cid.of(CID.intValue() - 2)), is(emptySet()));
        assertThat(tournament.bidsInCategory(CID), is(ImmutableSet.of(BID3, BID4, BID2)));
    }

    @Test
    public void selectDefaultMatchRuleForPlayOff() {
        checkSelectMatchRules(RULES_G8Q1_S1A2G11, PLAY_OFF_MATCH_HF, 1);
        checkSelectMatchRules(G8Q1_S1AG11_DM_S2A2G11, PLAY_OFF_MATCH_HF, 1);
    }

    private void checkSelectMatchRules(TournamentRules rules,
            MatchInfo matchInfo, int expectedSetsToWin) {
        assertThat(((PingPongMatchRules) TournamentMemState.builder()
                        .rule(rules)
                        .build().selectMatchRule(matchInfo))
                        .getSetsToWin(),
                is(expectedSetsToWin));
    }

    @Test
    public void selectDefaultMatchRule() {
        checkSelectMatchRules(RULES_G8Q1_S1A2G11, GROUP_ORIGIN_MATCH, 1);
        checkSelectMatchRules(G8Q1_S1A2G11_L1_S3A2G11, GROUP_ORIGIN_MATCH, 1);
        checkSelectMatchRules(G8Q1_S1AG11_DM_S2A2G11, GROUP_ORIGIN_MATCH, 1);
    }

    @Test
    public void selectDefaultMatchRuleForDisambiguationMatch() {
        checkSelectMatchRules(G8Q1_S1A2G11_L1_S3A2G11, GROUP_DM_MATCH, 1);
        checkSelectMatchRules(RULES_G8Q1_S1A2G11, GROUP_DM_MATCH, 1);
    }

    @Test
    public void selectCustomMatchRuleForPlayOff() {
        checkSelectMatchRules(G8Q1_S1A2G11_L1_S3A2G11, PLAY_OFF_MATCH_HF, 3);
    }

    @Test
    public void selectCustomMatchRuleForDisambiguationMatch() {
        checkSelectMatchRules(G8Q1_S1AG11_DM_S2A2G11, GROUP_DM_MATCH, 2);
    }
}
