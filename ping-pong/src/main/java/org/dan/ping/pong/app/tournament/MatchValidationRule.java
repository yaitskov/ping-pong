package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.match.IdentifiedScore;
import org.dan.ping.pong.app.match.MatchInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchValidationRule {
    private int minGamesToWin;
    private int minAdvanceInGames;
    private int minPossibleGames;
    private int setsToWin;

    public void validateSet(List<IdentifiedScore> setScore) {
        int aGames = setScore.get(0).getScore();
        int bGames = setScore.get(1).getScore();
        int maxGames = Math.max(aGames, bGames);
        int minGames = Math.min(aGames, bGames);
        if (minGames < minPossibleGames) {
            throw badRequest("Games cannot less than " + minPossibleGames);
        }
        if (maxGames < minGamesToWin) {
            throw badRequest("Winner should have at least "
                    + minGamesToWin + " games");
        }
        if (maxGames - minGames < minAdvanceInGames) {
            throw badRequest("Difference between games in a complete set"
                    + " cannot be less than " + minAdvanceInGames);
        }
        if (maxGames > minGamesToWin && maxGames - minGames > minAdvanceInGames) {
            throw badRequest("Winner games are to big");
        }
    }

    public Optional<Integer> findWinnerId(MatchInfo matchInfo) {
        final List<Integer> uids = new ArrayList<>(
                matchInfo.getParticipantIdScore().keySet());
        final int uidA = uids.get(0);
        final List<Integer> setsA = matchInfo.getParticipantIdScore().get(uidA);
        final int uidB = uids.get(1);
        final List<Integer> setsB = matchInfo.getParticipantIdScore().get(uidB);
        int wonsA = 0;
        int wonsB = 0;
        for (int i = 0; i < setsA.size(); ++i) {
            if (setsA.get(i) > setsB.get(i)) {
                ++wonsA;
            } else {
                ++wonsB;
            }
        }
        if (wonsA >= setsToWin) {
            return Optional.of(uidA);
        }
        if (wonsB >= setsToWin) {
            return Optional.of(uidB);
        }
        return Optional.empty();
    }
}
