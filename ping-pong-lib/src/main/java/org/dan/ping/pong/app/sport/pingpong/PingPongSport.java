package org.dan.ping.pong.app.sport.pingpong;

import static org.dan.ping.pong.app.sport.SportType.PingPong;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.sport.MatchRules;
import org.dan.ping.pong.app.sport.Sport;
import org.dan.ping.pong.app.sport.SportType;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PingPongSport implements Sport<PingPongMatchRules> {
    private static final String SET = "set";
    private static final String MIN_POSSIBLE_GAMES = "minPossibleGames";
    private static final String MIN_GAMES_TO_WIN = "minGamesToWin";

    @Override
    public void validate(PingPongMatchRules rules) {
    }

    @Override
    public void validate(PingPongMatchRules rules, MatchInfo match) {
        final Map<Uid, List<Integer>> scores = match.getParticipantIdScore();
        final Iterator<Uid> uidIterator = scores.keySet().iterator();
        final Uid uidA = uidIterator.next();
        final Uid uidB = uidIterator.next();

        for (int iSet = 0; iSet < scores.get(uidA).size(); ++iSet) {
            validate(rules, iSet,
                    scores.get(uidA).get(iSet),
                    scores.get(uidB).get(iSet));
        }
    }

    private void validate(PingPongMatchRules rules, int iSet, int aGames, int bGames) {
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
    }

    @Override
    public MatchRules parse(JsonNode node) {
        return null;
    }

    @Override
    public SportType getType() {
        return PingPong;
    }
}
