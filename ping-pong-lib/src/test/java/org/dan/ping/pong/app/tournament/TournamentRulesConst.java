package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.group.GroupRulesConst.G2Q1;
import static org.dan.ping.pong.app.group.GroupRulesConst.G3Q1;
import static org.dan.ping.pong.app.group.GroupRulesConst.G3Q2;
import static org.dan.ping.pong.app.group.GroupRulesConst.G8Q1;
import static org.dan.ping.pong.app.group.GroupRulesConst.G8Q2;
import static org.dan.ping.pong.app.match.MatchRulesConst.S1A2G11;
import static org.dan.ping.pong.app.match.MatchRulesConst.S2A2G11;
import static org.dan.ping.pong.app.match.MatchRulesConst.S3A2G11;
import static org.dan.ping.pong.app.playoff.ConsoleLayersPolicy.IndependentLayers;
import static org.dan.ping.pong.app.playoff.PlayOffRule.L1_3P;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing1;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing2;
import static org.dan.ping.pong.app.tournament.CastingLotsRulesConst.INCREASE_PROVIDED_RANKING;
import static org.dan.ping.pong.app.tournament.CastingLotsRulesConst.INCREASE_SIGNUP_CASTING;
import static org.dan.ping.pong.app.tournament.CastingLotsRulesConst.INCREASE_SIGNUP_MIX;
import static org.dan.ping.pong.app.tournament.CastingLotsRulesConst.LAYERED_CONSOLE_CASTING;

import org.dan.ping.pong.app.place.ArenaDistributionPolicy;
import org.dan.ping.pong.app.place.PlaceRules;
import org.dan.ping.pong.app.playoff.ConsoleLayersPolicy;

import java.util.Optional;

public class TournamentRulesConst {
    public static final PlaceRules GLOBAL = PlaceRules.builder()
            .arenaDistribution(ArenaDistributionPolicy.GLOBAL).build();
    public static final TournamentRules RULES_G2Q1_S3A2G11 = TournamentRules
            .builder()
            .match(S3A2G11)
            .group(Optional.of(G2Q1))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();
    public static final TournamentRules RULES_G8Q2_S3A2G11 = TournamentRules
            .builder()
            .match(S3A2G11)
            .group(Optional.of(G8Q2))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();
    public static final TournamentRules RULES_G8Q2_S1A2G11 = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G8Q2))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();
    public static final TournamentRules RULES_G3Q1_S1A2G11 = RULES_G8Q2_S1A2G11
            .withGroup(Optional.of(G3Q1));
    public static final TournamentRules RULES_G3Q1_S1A2G11_NP = RULES_G3Q1_S1A2G11
            .withPlace(Optional.empty());
    public static final TournamentRules RULES_G3Q2_S1A2G11 = RULES_G8Q2_S1A2G11
            .withGroup(Optional.of(G3Q2));
    public static final TournamentRules RULES_G2Q1_S1A2G11_3P = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G2Q1))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(L1_3P))
            .place(Optional.of(GLOBAL))
            .build();
    public static final TournamentRules RULES_G2Q1_S1A2G11_MIX = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G2Q1))
            .casting(INCREASE_SIGNUP_MIX)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();
    public static final TournamentRules RULES_G2Q1_S1A2G11 = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G2Q1))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();
    public static final TournamentRules RULES_G2Q1_S1A2G11_NP = RULES_G2Q1_S1A2G11
            .withPlace(Optional.empty());
    public static final TournamentRules RULES_G2Q1_S1A2G11_PRNK = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G2Q1))
            .casting(INCREASE_PROVIDED_RANKING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();
    public static final TournamentRules RULES_G8Q1_S1A2G11 = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G8Q1))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();

    public static final TournamentRules RULES_JP_S1A2G11 = RULES_G8Q1_S1A2G11.withGroup(Optional.empty());
    public static final TournamentRules RULES_JP_S1A2G11_NP = RULES_JP_S1A2G11.withPlace(Optional.empty());
    public static final TournamentRules RULES_JP_S1A2G11_NP_3P = RULES_JP_S1A2G11_NP.withPlayOff(Optional.of(L1_3P));
    public static final TournamentRules RULES_JP_S1A2G11_3P = RULES_JP_S1A2G11.withPlayOff(Optional.of(L1_3P));
    public static final TournamentRules RULES_G_S1A2G11 = RULES_G8Q1_S1A2G11.withPlayOff(Optional.empty());
    public static final TournamentRules RULES_G_S1A2G11_NP = RULES_G_S1A2G11.withPlace(Optional.empty());
    public static final TournamentRules RULES_G8Q1_S1A2G11_NP = RULES_G8Q1_S1A2G11.withPlace(Optional.empty());
    public static final TournamentRules RULES_G8Q1_S3A2G11 = TournamentRules
            .builder()
            .match(S3A2G11)
            .group(Optional.of(G8Q1))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();
    public static final TournamentRules RULES_JP_S3A2G11 = RULES_G8Q1_S3A2G11.withGroup(Optional.empty());
    public static final TournamentRules RULES_JP2_S3A2G11 = RULES_JP_S3A2G11.withPlayOff(Optional.of(Losing2));
    public static final TournamentRules RULES_JP2_S1A2G11 = RULES_JP_S1A2G11.withPlayOff(Optional.of(Losing2));
    public static final TournamentRules RULES_JP2_S3A2G11_NP = RULES_JP2_S3A2G11.withPlace(Optional.empty());
    public static final TournamentRules RULES_JP2_S1A2G11_NP = RULES_JP2_S1A2G11.withPlace(Optional.empty());
    public static final TournamentRules RULES_JP_S3A2G11_3P = RULES_JP_S3A2G11.withPlayOff(Optional.of(L1_3P));
    public static final TournamentRules RULES_JP_S3A2G11_NP = RULES_JP_S3A2G11.withPlace(Optional.empty());
    public static final TournamentRules RULES_JP_S3A2G11_NP_3P = RULES_JP_S3A2G11_NP.withPlayOff(Optional.of(L1_3P));
    public static final TournamentRules RULES_G_S3A2G11 = RULES_G8Q1_S3A2G11.withPlayOff(Optional.empty());
    public static final TournamentRules RULES_G_S3A2G11_NP = RULES_G_S3A2G11.withPlace(Optional.empty());
    public static final TournamentRules RULES_G_S2A2G11_NP = RULES_G_S3A2G11_NP.withMatch(S2A2G11);
    public static final TournamentRules RULES_LC_S1A2G11_NP = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.empty())
            .casting(LAYERED_CONSOLE_CASTING)
            .playOff(Optional.of(Losing1.withLayerPolicy(
                    Optional.of(IndependentLayers))))
            .place(Optional.empty())
            .build();
}
