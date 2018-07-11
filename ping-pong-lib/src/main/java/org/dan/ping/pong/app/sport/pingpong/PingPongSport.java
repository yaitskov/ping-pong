package org.dan.ping.pong.app.sport.pingpong;

import static java.lang.Math.max;
import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.sport.SportType.PingPong;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.SET_LENGTH_MISMATCH;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.IdentifiedScore;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.SetScoreReq;
import org.dan.ping.pong.app.sport.Sport;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.sport.tennis.CanonicalSetValidator;
import org.dan.ping.pong.app.tournament.rules.PingPongMatchRuleValidator;
import org.dan.ping.pong.app.tournament.rules.ValidationError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PingPongSport implements Sport<PingPongMatchRules> {
    public static final String TO_MANY_SETS = "to many sets";

    @Override
    public void validate(Multimap<String, ValidationError> errors, PingPongMatchRules rules) {
        PingPongMatchRuleValidator.validate(errors, rules);
    }

    @Override
    public void validate(PingPongMatchRules rules, MatchInfo match) {
        final Map<Bid, List<Integer>> scores = match.getParticipantIdScore();
        final Iterator<Bid> uidIterator = scores.keySet().iterator();
        final Bid bidA = uidIterator.next();
        final Bid bidB = uidIterator.next();
        final int[] wonSets = new int[2];
        final int bSets = scores.get(bidB).size();
        final int aSets = scores.get(bidA).size();
        if (bSets != aSets) {
            throw badRequest(SET_LENGTH_MISMATCH);
        }
        for (int iSet = 0; iSet < aSets; ++iSet) {
            ++wonSets[CanonicalSetValidator.validate(rules, iSet,
                    scores.get(bidA).get(iSet),
                    scores.get(bidB).get(iSet))];
            if (wonSets[CanonicalSetValidator.A] > rules.getSetsToWin()) {
                throw badRequest(TO_MANY_SETS);
            }
            if (wonSets[CanonicalSetValidator.B] > rules.getSetsToWin()) {
                throw badRequest(TO_MANY_SETS);
            }
        }
    }

    @Override
    public SportType getType() {
        return PingPong;
    }

    @Override
    public Optional<Bid> findWinnerId(PingPongMatchRules rules, Map<Bid, Integer> wonSets) {
        return wonSets.entrySet().stream()
                .filter(e -> e.getValue() >= rules.getSetsToWin())
                .map(Map.Entry::getKey)
                .findAny();
    }

    public void checkWonSets(PingPongMatchRules rules, Map<Bid, Integer> uidWonSets) {
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

    @Override
    public List<SetScoreReq> expandScoreSet(PingPongMatchRules rules, SetScoreReq score) {
        final List<SetScoreReq> result = new ArrayList<>();
        final int bidIdxA = 0;
        final int bidIdxB = 1;
        int winnerIdx;
        int loserIdx;
        if (score.getScores().get(bidIdxA).getScore() < score.getScores().get(bidIdxB).getScore()) {
            winnerIdx = bidIdxB;
            loserIdx = bidIdxA;
        } else {
            winnerIdx = bidIdxA;
            loserIdx = bidIdxB;
        }
        final int loserWonSets = score.getScores().get(loserIdx).getScore();
        if (loserWonSets > 0) {
            final List<IdentifiedScore> loserScores = asList(
                    buildScore(score, winnerIdx, 0),
                    buildScore(score, loserIdx, rules.getMinGamesToWin()));
            for (int i = 0; i < loserWonSets; ++i) {
                result.add(score.atomic(i, loserScores));
            }
        }
        final int winnerWonSets = score.getScores().get(winnerIdx).getScore();
        final List<IdentifiedScore> winnerScores = asList(
                buildScore(score, loserIdx, 0),
                buildScore(score, winnerIdx, rules.getMinGamesToWin()));
        for (int i = 0; i < winnerWonSets; ++i) {
            result.add(score.atomic(loserWonSets + i, winnerScores));
        }
        return result;
    }

    private IdentifiedScore buildScore(SetScoreReq scores, int loserIdx, int score) {
        return IdentifiedScore.builder()
                .bid(scores.getScores().get(loserIdx).getBid())
                .score(score)
                .build();
    }

    @Override
    public int maxUpLimitSetsDiff(PingPongMatchRules rules) {
        return max(2, rules.getSetsToWin());
    }

    @Override
    public int maxUpLimitBallsDiff(PingPongMatchRules rules) {
        return max(2, max(rules.getMinGamesToWin(), rules.getMinAdvanceInGames())
                * (2 * rules.getSetsToWin() - 1));
    }
}
