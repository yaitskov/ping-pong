package org.dan.ping.pong.app.sport.pingpong;

import static org.dan.ping.pong.app.sport.SportType.PingPong;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.sport.Sport;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.tournament.rules.PingPongMatchRuleValidator;
import org.dan.ping.pong.app.tournament.rules.ValidationError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PingPongSport implements Sport<PingPongMatchRules> {
    private static final String SET = "set";
    private static final String MIN_POSSIBLE_GAMES = "minPossibleGames";
    private static final String MIN_GAMES_TO_WIN = "minGamesToWin";
    private static final int A = 0;
    private static final int B = 1;
    public static final String TO_MANY_SETS = "to many sets";

    @Override
    public void validate(Multimap<String, ValidationError> errors, PingPongMatchRules rules) {
        PingPongMatchRuleValidator.validate(errors, rules);
    }

    @Override
    public void validate(PingPongMatchRules rules, MatchInfo match) {
        final Map<Uid, List<Integer>> scores = match.getParticipantIdScore();
        final Iterator<Uid> uidIterator = scores.keySet().iterator();
        final Uid uidA = uidIterator.next();
        final Uid uidB = uidIterator.next();
        final int[] wonSets = new int[2];
        for (int iSet = 0; iSet < scores.get(uidA).size(); ++iSet) {
            ++wonSets[validate(rules, iSet,
                    scores.get(uidA).get(iSet),
                    scores.get(uidB).get(iSet))];
            if (wonSets[A] > rules.getSetsToWin()) {
                throw badRequest(TO_MANY_SETS);
            }
            if (wonSets[B] > rules.getSetsToWin()) {
                throw badRequest(TO_MANY_SETS);
            }
        }
    }

    private int validate(PingPongMatchRules rules, int iSet, int aGames, int bGames) {
        final int maxGames = Math.max(aGames, bGames);
        final int minGames = Math.min(aGames, bGames);
        if (minGames < rules.getMinPossibleGames()) {
            throw badRequest("Games cannot be less than",
                    ImmutableMap.of(SET, iSet,
                            MIN_POSSIBLE_GAMES, rules.getMinPossibleGames()));
        }
        if (maxGames < rules.getMinGamesToWin()) {
            throw badRequest("Winner should have at least n games",
                    ImmutableMap.of(SET, iSet,
                            MIN_GAMES_TO_WIN, rules.getMinGamesToWin()));
        }
        if (maxGames - minGames < rules.getMinAdvanceInGames()) {
            throw badRequest("Difference between games cannot be less than",
                    ImmutableMap.of(SET, iSet,
                            "minAdvanceInGames", rules.getMinAdvanceInGames()));
        }
        if (maxGames > rules.getMinGamesToWin()
                && maxGames - minGames > rules.getMinAdvanceInGames()) {
            throw badRequest("Winner games are to big", SET, iSet);
        }
        if (aGames < bGames) {
            return B;
        }
        return A;
    }

    @Override
    public SportType getType() {
        return PingPong;
    }

    @Override
    public Optional<Uid> findWinnerId(PingPongMatchRules rules, Map<Uid, Integer> wonSets) {
        return wonSets.entrySet().stream()
                .filter(e -> e.getValue() >= rules.getSetsToWin())
                .map(Map.Entry::getKey)
                .findAny();
    }

    public void checkWonSets(PingPongMatchRules rules, Map<Uid, Integer> uidWonSets) {
        final Collection<Integer> wonSets = uidWonSets.values();
        wonSets.stream()
                .filter(n -> n >  rules.getSetsToWin()).findAny()
                .ifPresent(o -> {
                    throw badRequest("won sets more that required");
                });
        final long winners = wonSets.stream()
                .filter(n -> n == rules.getSetsToWin())
                .count();
        if (winners > 1) {
            throw badRequest("winners are more that 1");
        }
    }
}
