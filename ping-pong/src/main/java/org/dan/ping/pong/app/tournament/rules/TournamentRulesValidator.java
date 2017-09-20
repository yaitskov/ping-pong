package org.dan.ping.pong.app.tournament.rules;

import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRuleValidator;
import org.dan.ping.pong.app.tournament.TournamentRules;

import javax.inject.Inject;

public class TournamentRulesValidator {
    private static final String THIRD_PLACE = "third-place";
    private static final String PRIZE_WINNING_PLACES = "prize-winning-places";

    @Inject
    private GroupRuleValidator groupRuleValidator;

    @Inject
    private MatchRuleValidator matchRuleValidator;

    @Inject
    private CastingLotsRuleValidator rankValidator;

    public void validate(TournamentRules rules, Multimap<String, ValidationError> errors) {
        rankValidator.validate(errors, rules.getCasting());
        groupRuleValidator.validate(errors, rules.getGroup());
        matchRuleValidator.validate(errors, rules.getMatch());
        if (rules.getThirdPlaceMatch() < 0 || rules.getThirdPlaceMatch() > 1) {
            errors.put(THIRD_PLACE, ValidationError.builder()
                    .template("out-of-range")
                    .build());
        }
        if (rules.getPrizeWinningPlaces() < 1 || rules.getPrizeWinningPlaces() > 3) {
            errors.put(PRIZE_WINNING_PLACES, ValidationError.builder()
                    .template("out-of-range")
                    .build());
        }
    }
}
