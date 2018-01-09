package org.dan.ping.pong.app.tournament.rules;

import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.OUT_OF_RANGE;
import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.VALUE_NULL;
import static org.dan.ping.pong.app.tournament.rules.ValidationError.ofTemplate;

import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;

public class MatchRuleValidator {
    private static final String MATCH_RULE = "match-rule";
    private static final String SET_TO_WIN = ".set-to-win";
    private static final String MIN_POSSIBLE_GAMES = ".min-possible-games";
    private static final String MIN_GAMES_TO_WIN = ".min-games-to-win";

    public static void validate(Multimap<String, ValidationError> errors, PingPongMatchRules rule) {
        if (rule == null) {
            errors.put(MATCH_RULE, ofTemplate(VALUE_NULL));
            return;
        }
        if (rule.getSetsToWin() < 1 || rule.getSetsToWin() > 100) {
            errors.put(MATCH_RULE + SET_TO_WIN, ofTemplate(OUT_OF_RANGE));
        }
        if (rule.getMinPossibleGames() < 0
                || rule.getMinPossibleGames() >= rule.getMinGamesToWin()) {
            errors.put(MATCH_RULE + MIN_POSSIBLE_GAMES, ofTemplate(OUT_OF_RANGE));
        }
        if (rule.getMinGamesToWin() < 1 || rule.getMinGamesToWin() <= rule.getMinPossibleGames()) {
            errors.put(MATCH_RULE + MIN_GAMES_TO_WIN, ofTemplate(OUT_OF_RANGE));
        }
        if (rule.getMinAdvanceInGames() < 1 || rule.getMinAdvanceInGames() > 100) {
            errors.put(MATCH_RULE + ".min-advance-in-games", ofTemplate(OUT_OF_RANGE));
        }
    }
}
