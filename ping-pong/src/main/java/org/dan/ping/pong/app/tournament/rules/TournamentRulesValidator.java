package org.dan.ping.pong.app.tournament.rules;

import static org.dan.ping.pong.app.group.ConsoleTournament.NO;
import static org.dan.ping.pong.app.tournament.rules.ValidationError.ofTemplate;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRuleValidator;
import org.dan.ping.pong.app.playoff.PlayOffRuleValidator;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.sys.error.ValidationErrors;

import javax.inject.Inject;

public class TournamentRulesValidator {
    private static final String TOURNAMENT_RULES = "tournament-rules";

    @Inject
    private GroupRuleValidator groupRuleValidator;

    @Inject
    private Sports sports;

    @Inject
    private CastingLotsRuleValidator rankValidator;

    @Inject
    private PlayOffRuleValidator playOffValidator;

    public void validate(TournamentRules rules, Multimap<String, ValidationError> errors) {
        rules.getPlayOff().ifPresent(r -> playOffValidator.validate(errors, r, rules));
        rules.getGroup().ifPresent(r -> groupRuleValidator.validate(errors, r));
        if (!(rules.getGroup().isPresent() || rules.getPlayOff().isPresent())) {
            errors.put(TOURNAMENT_RULES, ofTemplate("no-group-nor-playoff"));
        }
        rankValidator.validate(errors, rules.getCasting());
        sports.validateMatch(rules.getMatch().sport(), errors, rules.getMatch());
    }

    public void validateNew(TournamentRules rules) {
        rules.getPlayOff()
                .filter(pr -> pr.getConsole() != NO)
                .ifPresent(pr -> {
                    throw badRequest("new tournament cannot have console tournament for playoff");
                });
        rules.getGroup()
                .filter(pr -> pr.getConsole() != NO)
                .ifPresent(pr -> {
                    throw badRequest("new tournament cannot have console tournament for group");
                });
    }

    public void validate(TournamentRules rules) {
        final Multimap<String, ValidationError> errors = HashMultimap.create();
        validate(rules, errors);
        if (!errors.isEmpty()) {
            throw badRequest(new ValidationErrors("tournament-rules-are-wrong", errors));
        }
    }
}
