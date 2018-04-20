package org.dan.ping.pong.app.sport;

import static java.util.Optional.ofNullable;
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
        get(tournament.getSport()).validate(tournament.selectMatchRule(match), match);
    }

    public Map<Uid, Integer> calcWonSets(TournamentMemState tournament, MatchInfo matchInfo) {
        return get(tournament.getSport()).calcWonSets(matchInfo.getParticipantIdScore());
    }

    public Optional<Uid> findWinnerId(MatchRules rules, Map<Uid, Integer> wonSets) {
        return get(rules.sport()).findWinnerId(rules, wonSets);
    }

    public Optional<Uid> findWinner(TournamentMemState tournament, MatchInfo matchInfo) {
        return findWinnerByScores(tournament.selectMatchRule(matchInfo),
                matchInfo.getParticipantIdScore());
    }

    public Optional<Uid> findWinnerByScores(MatchRules rules, Map<Uid, List<Integer>> sets) {
        final Sport sport = get(rules.sport());
        return sport.findWinnerId(rules, sport.calcWonSets(sets));
    }

    public void checkWonSets(MatchRules rules, Map<Uid, Integer> uidWonSets) {
        final Sport sport = get(rules.sport());
        sport.checkWonSets(rules, uidWonSets);
    }

    public List<SetScoreReq> expandScoreSet(TournamentMemState tournament, SetScoreReq score) {
        final Sport sport = get(tournament.getSport());
        return sport.expandScoreSet(tournament.getRule().getMatch(), score);
    }

    public Optional<Uid> findNewWinnerUid(TournamentMemState tournament,
            MatchSets newSets, MatchInfo mInfo) {
        if (mInfo.getWinnerId().isPresent()) {
            final Optional<Uid> actualPracticalWinnerUid = findWinner(tournament, mInfo);
            // actual uid always = formal uid if actual one is presented,
            // because walkover can happen if match has no scored sets
            // or presented sets are not enough to find out winner
            if (actualPracticalWinnerUid.isPresent()) {
                // match complete normally (by score)
                return findWinnerByScores(
                        tournament.selectMatchRule(mInfo), newSets.getSets());
            } else {
                return mInfo.getWinnerId(); // walkover, quit or expel
            }
        } else {
            return findWinnerByScores(tournament.selectMatchRule(mInfo),
                    newSets.getSets());
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
        findWinnerByScores(tournament.selectMatchRule(mInfo), newSets.getSets())
                .ifPresent(wUid -> clone.setWinnerId(Optional.of(wUid)));
        return clone;
    }
}
