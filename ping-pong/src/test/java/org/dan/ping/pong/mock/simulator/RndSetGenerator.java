package org.dan.ping.pong.mock.simulator;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.match.MatchValidationRule;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RndSetGenerator implements SetGenerator {
    private final Map<Player, Integer> targetSets;
    private final Map<Player, Integer> currentSets;
    private final MatchValidationRule rule;
    private final Player winer;
    private final Player loser;
    private final List<Player> players;
    private final Random random = new Random();
    private int setNumber;

    public RndSetGenerator(Map<Player, Integer> targetSets, MatchValidationRule rule) {
        this.targetSets = targetSets;
        winer = targetSets.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey).get();
        loser = targetSets.keySet().stream().filter(k -> !k.equals(winer))
                .findAny().get();
        this.rule = rule;
        players = asList(winer, loser);
        currentSets = targetSets.keySet().stream().collect(toMap(o -> o, o -> 0));
    }

    @Override
    public Player getPlayerA() {
        return winer;
    }

    @Override
    public Player getPlayerB() {
        return loser;
    }

    @Override
    public Map<Player, Integer> generate(TournamentScenario scenario) {
        final int winGames = random.nextInt(2) + rule.getMinGamesToWin();
        final int loseGames = winGames > rule.getMinGamesToWin()
                ? winGames - rule.getMinAdvanceInGames()
                : random.nextInt(rule.getMinGamesToWin() - rule.getMinAdvanceInGames() + 1);
        ++setNumber;
        final int leftToWin = diff(winer);
        final int leftToLose = diff(loser);
        if (leftToWin == 1 || leftToLose == 0) {
            if (leftToLose == 0) {
                inc(winer);
                return ImmutableMap.of(winer, winGames, loser, loseGames);
            } else {
                inc(loser);
                return ImmutableMap.of(winer, loseGames, loser, winGames);
            }
        } else if (leftToWin == 0) {
            throw new RuntimeException("Set generator for "
                    + winer + " and " + loser + " exhausted");
        }
        final int setWinner = random.nextInt(2);
        inc(players.get(setWinner));
        return ImmutableMap.of(players.get(setWinner), winGames,
                players.get(1 - setWinner), loseGames);
    }

    private void inc(Player p) {
        currentSets.compute(p, (pp, n) -> n + 1);
    }

    private int diff(Player p) {
        return targetSets.get(p) - currentSets.get(p);
    }

    @Override
    public boolean isEmpty() {
        return diff(winer) == 0 && diff(loser) == 0;
    }

    @Override
    public int getSetNumber() {
        return setNumber;
    }
}
