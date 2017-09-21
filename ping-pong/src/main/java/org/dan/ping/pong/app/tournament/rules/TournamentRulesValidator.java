package org.dan.ping.pong.app.tournament.rules;

import static org.dan.ping.pong.app.tournament.rules.ValidationError.ofTemplate;

import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRuleValidator;
import org.dan.ping.pong.app.playoff.PlayOffRuleValidator;
import org.dan.ping.pong.app.tournament.TournamentRules;

import javax.inject.Inject;

public class TournamentRulesValidator {
    private static final String PRIZE_WINNING_PLACES = "prize-winning-places";
    private static final String TOURNAMENT_RULES = "tournament-rules";

    @Inject
    private GroupRuleValidator groupRuleValidator;

    @Inject
    private MatchRuleValidator matchRuleValidator;

    @Inject
    private CastingLotsRuleValidator rankValidator;

    @Inject
    private PlayOffRuleValidator playOffValidator;

    public void validate(TournamentRules rules, Multimap<String, ValidationError> errors) {
        rules.getPlayOff().ifPresent(r -> playOffValidator.validate(errors, r));
        rules.getGroup().ifPresent(r -> groupRuleValidator.validate(errors, r));
        if (!(rules.getGroup().isPresent() || rules.getPlayOff().isPresent())) {
            errors.put(TOURNAMENT_RULES, ofTemplate("no-group-nor-playoff"));
        }
        rankValidator.validate(errors, rules.getCasting());
        matchRuleValidator.validate(errors, rules.getMatch());
        if (rules.getPrizeWinningPlaces() < 1 || rules.getPrizeWinningPlaces() > 3) {
            errors.put(PRIZE_WINNING_PLACES, ValidationError.builder()
                    .template("out-of-range")
                    .build());
        }
    }
}
