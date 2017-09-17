package org.dan.ping.pong.app.tournament.rules;

import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.tournament.TournamentRules;

import javax.inject.Inject;

public class TournamentRulesValidator {
    private static final String THIRD_PLACE = "third-place";
    private static final String PRICE_WINNING_PLACES = "price-winning-places";

    @Inject
    private GroupRuleValidator groupRuleValidator;

    @Inject
    private MatchRuleValidator matchRuleValidator;

    public void validate(TournamentRules rules, Multimap<String, ValidationError> errors) {
        groupRuleValidator.validate(errors, rules.getGroup());
        if (rules.getThirdPlaceMatch() < 0 || rules.getThirdPlaceMatch() > 1) {
            errors.put(THIRD_PLACE, ValidationError.builder()
                    .template("out-of-range")
                    .build());
        }
        if (rules.getPrizeWinningPlaces() < 1 || rules.getPrizeWinningPlaces() > 3) {
            errors.put(PRICE_WINNING_PLACES, ValidationError.builder()
                    .template("out-of-range")
                    .build());
        }
    }
}
