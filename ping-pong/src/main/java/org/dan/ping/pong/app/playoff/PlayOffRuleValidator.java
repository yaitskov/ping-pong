package org.dan.ping.pong.app.playoff;

import static org.dan.ping.pong.app.group.ConsoleTournament.NO;
import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.OUT_OF_RANGE;
import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.VALUE_NULL;
import static org.dan.ping.pong.app.tournament.rules.ValidationError.ofTemplate;

import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.tournament.rules.ValidationError;

import javax.inject.Inject;

public class PlayOffRuleValidator {
    private static final String THIRD_PLACE = "third-place";
    private static final String PLAYOFF_RULE = "playoff-rule";
    private static final String LOSINGS = ".losings";

    @Inject
    private Sports sports;

    public void validate(Multimap<String, ValidationError> errors,
            PlayOffRule playOff, TournamentRules rules) {
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
        if (playOff.getConsole() != NO) {
            if (!playOff.getConsoleParticipants().isPresent()) {
                errors.put("console-participant", ofTemplate("not-selected"));
            }
        } else {
            if (playOff.getConsoleParticipants().isPresent()) {
                errors.put("console-participant", ofTemplate("must-be-empty"));
            }
        }

        playOff.getMatch().ifPresent(m -> {
            sports.validateMatch(m.sport(), errors, m);
            if (rules.getMatch().sport() != m.sport()) {
                errors.put(PLAYOFF_RULE + ".match", ofTemplate("sport mismatch"));
            }
        });
    }
}
