package org.dan.ping.pong.mock.simulator;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.sport.MatchRules;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;
import org.dan.ping.pong.app.sport.tennis.TennisMatchRules;

import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OutComeGenerator {
    private static final Map<SportType, Function<? extends MatchRules, MatchOutcome>> winGenerators =
            ImmutableMap.of(
                    SportType.PingPong, (PingPongMatchRules rule) -> new MatchOutcome(rule.getSetsToWin(), 0),
                    SportType.Tennis, (TennisMatchRules rule) -> new MatchOutcome(rule.getSetsToWin(), 0));

    private static final Map<SportType, Function<? extends MatchRules, MatchOutcome>> loseGenerators =
            ImmutableMap.of(
                    SportType.PingPong, (PingPongMatchRules rule) -> new MatchOutcome(0, rule.getSetsToWin()),
                    SportType.Tennis, (TennisMatchRules rule) -> new MatchOutcome(0, rule.getSetsToWin()));

    public static MatchOutcome generateWin(MatchRules match) {
        final Function<MatchRules, MatchOutcome> f = (Function<MatchRules, MatchOutcome>) winGenerators.get(match.sport());
        return f.apply(match);
    }

    public static MatchOutcome generateLose(MatchRules match) {
        final Function<MatchRules, MatchOutcome> f = (Function<MatchRules, MatchOutcome>) loseGenerators.get(match.sport());
        return f.apply(match);
    }

    private static final Random random = new Random();

    private static final Map<SportType, BiFunction<Integer, ? extends MatchRules, Integer>> randomWin =
            ImmutableMap.of(
                    SportType.PingPong, (Integer base, PingPongMatchRules rule) -> base + rule.getMinGamesToWin(),
                    SportType.Tennis, (Integer base, TennisMatchRules rule) -> base + rule.getMinGamesToWin());

    public static  int genRandomWin(MatchRules rules, int base) {
        final BiFunction<Integer, MatchRules, Integer> f = (BiFunction<Integer, MatchRules, Integer>) randomWin.get(rules.sport());
        return f.apply(base, rules);
    }

    private static final Map<SportType, BiFunction<Integer, ? extends MatchRules, Integer>> randomLose =
            ImmutableMap.of(
                    SportType.PingPong, (Integer winGames, PingPongMatchRules rule) ->
                            winGames > rule.getMinGamesToWin()
                                    ? winGames - rule.getMinAdvanceInGames()
                                    : random.nextInt(rule.getMinGamesToWin() - rule.getMinAdvanceInGames() + 1),
                    SportType.Tennis, (Integer winGames, TennisMatchRules rule) ->
                            winGames > rule.getMinGamesToWin()
                                    ? winGames - rule.getMinAdvanceInGames()
                                    : random.nextInt(rule.getMinGamesToWin() - rule.getMinAdvanceInGames() + 1));

    public static int genRandomLose(MatchRules rules, int win) {
        final BiFunction<Integer, MatchRules, Integer> f = (BiFunction<Integer, MatchRules, Integer>) randomLose.get(rules.sport());
        return f.apply(win, rules);
    }
}
