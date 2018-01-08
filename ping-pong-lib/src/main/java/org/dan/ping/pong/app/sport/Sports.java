package org.dan.ping.pong.app.sport;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.Map;

@RequiredArgsConstructor
public class Sports {
    private final Map<SportType, Sport> sports;

    public Sport get(SportType type) {
        return ofNullable(sports.get(type)).orElseThrow(() -> internalError("no sport " + type));
    }

    public void validateMatch(TournamentMemState tournament, MatchInfo match) {
        get(tournament.getSport()).validate(tournament.getRule().getMatch(), match);
    }
}
