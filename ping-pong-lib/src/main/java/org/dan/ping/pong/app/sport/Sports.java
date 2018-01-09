package org.dan.ping.pong.app.sport;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class Sports {
    private final Map<SportType, Sport> sports;

    public Sport get(SportType type) {
        return ofNullable(sports.get(type)).orElseThrow(() -> internalError("no sport " + type));
    }

    public void validateMatch(TournamentMemState tournament, MatchInfo match) {
        get(tournament.getSport()).validate(tournament.getRule().getMatch(), match);
    }

    public Map<Uid, Integer> calcWonSets(TournamentMemState tournament,MatchInfo matchInfo) {
        return get(tournament.getSport()).calcWonSets(matchInfo.getParticipantIdScore());
    }

    public Optional<Uid> findWinnerId(TournamentMemState tournament, Map<Uid, Integer> wonSets) {
        return get(tournament.getSport()).findWinnerId(tournament.getRule().getMatch(), wonSets);
    }

    public Optional<Uid> findWinner(TournamentMemState tournament, MatchInfo matchInfo) {
        return findWinnerByScores(tournament, matchInfo.getParticipantIdScore());
    }

    public Optional<Uid> findWinnerByScores(TournamentMemState tournament, Map<Uid, List<Integer>> sets) {
        final Sport sport = get(tournament.getSport());
        return sport.findWinnerId(tournament.getRule().getMatch(), sport.calcWonSets(sets));
    }

    public Optional<Uid> findStronger(TournamentMemState tournament, MatchInfo mInfo) {
        final Sport sport = get(tournament.getSport());
        return sport.findStronger(sport.calcWonSets(mInfo.getParticipantIdScore()));
    }
}
