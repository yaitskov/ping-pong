package org.dan.ping.pong.app.server.playoff;

import static org.dan.ping.pong.app.server.tournament.rules.GroupRuleValidator.OUT_OF_RANGE;
import static org.dan.ping.pong.app.server.tournament.rules.GroupRuleValidator.VALUE_NULL;
import static org.dan.ping.pong.app.server.tournament.rules.ValidationError.ofTemplate;

import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.server.tournament.rules.ValidationError;

public class PlayOffRuleValidator {
    private static final String THIRD_PLACE = "third-place";
    private static final String PLAYOFF_RULE = "playoff-rule";
    private static final String LOSINGS = ".losings";

    public void validate(Multimap<String, ValidationError> errors, PlayOffRule playOff) {
        if (playOff == null) {
            errors.put(PLAYOFF_RULE, ofTemplate(VALUE_NULL));
            return;
        }
        if (playOff.getLosings() < 1 || playOff.getLosings() > 2) {
            errors.put(PLAYOFF_RULE + LOSINGS, ofTemplate(OUT_OF_RANGE));
        }
        if (playOff.getThirdPlaceMatch() < 0 || playOff.getThirdPlaceMatch() > 1) {
            errors.put(THIRD_PLACE, ofTemplate(OUT_OF_RANGE));
        }
    }
}
