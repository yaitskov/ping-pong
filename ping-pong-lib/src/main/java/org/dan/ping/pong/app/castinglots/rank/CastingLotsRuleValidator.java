package org.dan.ping.pong.app.castinglots.rank;

import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.VALUE_NULL;
import static org.dan.ping.pong.app.tournament.rules.ValidationError.ofTemplate;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.dan.ping.pong.app.tournament.rules.ValidationError;

public class CastingLotsRuleValidator {
    public static final String CASTING_RULE = "casting-rule";
    private static final String PROVIDED_RANK_OPTIONS = ".provided-rank-options";
    private static final String NOT_EXPECTED = "not-expected";
    private static final String PROVIDED_RANK_RANGE = ".provided-rank-range";
    private static final String INVALID = "invalid";
    private static final String PROVIDED_RANK_LABEL = ".provided-rank-label";
    private static final String SPLIT_POLICY = ".split-policy";
    private static final String DIRECTION = ".direction";
    private static final String POLICY = ".policy";

    public void validate(Multimap<String, ValidationError> errors, CastingLotsRule rule) {
        if (rule == null) {
            errors.put(CASTING_RULE, ofTemplate(VALUE_NULL));
            return;
        }
        if (rule.getDirection() == null) {
            errors.put(CASTING_RULE + DIRECTION, ofTemplate(VALUE_NULL));
        }
        if (rule.getSplitPolicy() == null) {
            errors.put(CASTING_RULE + SPLIT_POLICY, ofTemplate(VALUE_NULL));
        }
        if (rule.getPolicy() == null) {
            errors.put(CASTING_RULE + POLICY, ofTemplate(VALUE_NULL));
        } else if (rule.getPolicy() == ParticipantRankingPolicy.ProvidedRating) {
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
