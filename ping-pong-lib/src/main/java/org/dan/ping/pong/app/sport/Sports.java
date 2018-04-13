package org.dan.ping.pong.app.sport;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.match.dispute.MatchSets.ofSets;
import static org.dan.ping.pong.app.tournament.rules.TennisMatchRuleValidator.MATCH_RULE;
import static org.dan.ping.pong.app.tournament.rules.ValidationError.ofTemplate;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.SetScoreReq;
import org.dan.ping.pong.app.match.dispute.MatchSets;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.rules.ValidationError;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class Sports {
    public static final int MAX_BASE = 80;
    private final Map<SportType, Sport> sports;

    public Sport get(SportType type) {
        return ofNullable(sports.get(type)).orElseThrow(() -> internalError("no sport " + type));
    }

    public void validateMatch(SportType sportType, Multimap<String, ValidationError> errors, MatchRules rules) {
        final Sport sport = get(sportType);
        sport.validate(errors, rules);
        if (sport.maxUpLimitSetsDiff(rules) > MAX_BASE) {
            errors.put(MATCH_RULE, ofTemplate("max sets is to big"));
        } else if (sport.maxUpLimitBallsDiff(rules) > MAX_BASE) {
            errors.put(MATCH_RULE, ofTemplate("max balls is to big"));
        }
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
        return findWinnerByScores(tournament, ofSets(matchInfo.getParticipantIdScore()));
    }

    public Optional<Uid> findWinnerByScores(TournamentMemState tournament, MatchSets sets) {
        final Sport sport = get(tournament.getSport());
        return sport.findWinnerId(tournament.getRule().getMatch(),
                sport.calcWonSets(sets.getSets()));
    }

    public Optional<Uid> findStronger(TournamentMemState tournament, MatchInfo mInfo) {
        final Sport sport = get(tournament.getSport());
        return sport.findStronger(sport.calcWonSets(mInfo.getParticipantIdScore()));
    }

    public void checkWonSets(TournamentMemState tournament, Map<Uid, Integer> uidWonSets) {
        final Sport sport = get(tournament.getSport());
        sport.checkWonSets(tournament.getRule().getMatch(), uidWonSets);
    }

    public List<SetScoreReq> expandScoreSet(TournamentMemState tournament, SetScoreReq score) {
        final Sport sport = get(tournament.getSport());
        return sport.expandScoreSet(tournament.getRule().getMatch(), score);
    }

    public Optional<Uid> findNewWinnerUid(TournamentMemState tournament,
            MatchSets newSets, MatchInfo minfo) {
        if (minfo.getWinnerId().isPresent()) {
            final Optional<Uid> actualPracticalWinnerUid = findWinner(tournament, minfo);
            // actual uid always = formal uid if actual one is presented,
            // because walkover can happen if match has no scored sets
            // or presented sets are not enough to find out winner
            if (actualPracticalWinnerUid.isPresent()) {
                // match complete normally (by score)
                return findWinnerByScores(tournament, newSets);
            } else {
                return minfo.getWinnerId(); // walkover, quit or expel
            }
        } else {
            return findWinnerByScores(tournament, newSets);
        }
    }

    public MatchInfo alternativeSetsWithoutWinner(MatchInfo mInfo, MatchSets newSets) {
        final MatchInfo clone = mInfo.clone();
        clone.setParticipantIdScore(newSets.getSets());
        return clone;
    }

    public MatchInfo alternativeSets(TournamentMemState tournament,
            MatchInfo mInfo, MatchSets newSets) {
        final MatchInfo clone = alternativeSetsWithoutWinner(mInfo, newSets);
        findWinnerByScores(tournament, newSets)
                .ifPresent(wUid -> clone.setWinnerId(Optional.of(wUid)));
        return clone;
    }
}
