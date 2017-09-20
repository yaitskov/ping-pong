package org.dan.ping.pong.app.castinglots.rank;

import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.VALUE_NULL;
import static org.dan.ping.pong.app.tournament.rules.ValidationError.ofTemplate;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.dan.ping.pong.app.tournament.rules.ValidationError;

public class ProvidedRankValidator {
    public static final String CASTING_RULE = "casting-rule";
    private static final String PROVIDED_RANK_OPTIONS = ".provided-rank-options";
    private static final String NOT_EXPECTED = "not-expected";
    private static final String PROVIDED_RANK_RANGE = ".provided-rank-range";
    private static final String INVALID = "invalid";
    private static final String PROVIDED_RANK_LABEL = ".provided-rank-label";

    public void validate(Multimap<String, ValidationError> errors, CastingLotsRule rule) {
        if (rule == null) {
            errors.put(CASTING_RULE, ofTemplate(VALUE_NULL));
            return;
        }
        if (rule.getPolicy() == ParticipantRankingPolicy.ProvidedRating) {
            if (rule.getProvidedRankOptions().isPresent()) {
                final ProvidedRankOptions options = rule.getProvidedRankOptions().get();
                if (options.getMaxValue() <= options.getMinValue()) {
                    errors.put(CASTING_RULE + PROVIDED_RANK_RANGE, ofTemplate(INVALID));
                }
                if (StringUtils.isBlank(options.getLabel())) {
                    errors.put(CASTING_RULE + PROVIDED_RANK_LABEL, ofTemplate(VALUE_NULL));
                }
            } else {
                errors.put(CASTING_RULE + PROVIDED_RANK_OPTIONS, ofTemplate(VALUE_NULL));
            }
        } else if (rule.getProvidedRankOptions().isPresent()) {
            errors.put(CASTING_RULE + PROVIDED_RANK_OPTIONS, ofTemplate(NOT_EXPECTED));
        }
    }
}
