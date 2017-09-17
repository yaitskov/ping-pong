package org.dan.ping.pong.app.tournament.rules;

import static org.dan.ping.pong.app.tournament.rules.ValidationError.ofTemplate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.tournament.GroupRules;
import org.dan.ping.pong.app.tournament.GroupSchedule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupRuleValidator {
    private static final String GROUP_RULE = "group-rule";
    private static final String MAX_SIZE = ".max-size";
    public static final String OUT_OF_RANGE = "out-of-range";
    private static final String QUITS = ".quits";
    public static final String VALUE_NULL = "value-null";
    private static final String SCHEDULE = ".schedule";

    public void validate(Multimap<String, ValidationError> errors, GroupRules group) {
        if (group == null) {
            errors.put(GROUP_RULE, ofTemplate(VALUE_NULL));
            return;
        }
        if (group.getMaxSize() < 2 || group.getMaxSize() > 20) {
            errors.put(GROUP_RULE + MAX_SIZE, ofTemplate(OUT_OF_RANGE));
        }
        if (group.getQuits() < 1 || group.getQuits() >= group.getMaxSize()) {
            errors.put(GROUP_RULE + QUITS, ofTemplate(OUT_OF_RANGE));
        }
        group.getSchedule().ifPresent(schedule -> validateSchedule(schedule, errors));
    }

    private void validateSchedule(GroupSchedule schedule,
            Multimap<String, ValidationError> errors) {
        if (schedule.getSize2Schedule() == null) {
            errors.put(GROUP_RULE + SCHEDULE, ofTemplate(VALUE_NULL));
            return;
        }
        schedule.getSize2Schedule().forEach((n, matches) -> {
            if (matches.size() % 2 == 1) {
                errors.put(GROUP_RULE + SCHEDULE,
                        ofTemplate("schedule-n-odd", "n", n));
            } else if (n < matches.size() / 2) {
                errors.put(GROUP_RULE + SCHEDULE,
                        ofTemplate("schedule-n-too-much", "n", n));
            } else if (n > matches.size() / 2) {
                errors.put(GROUP_RULE + SCHEDULE,
                        ofTemplate("schedule-n-not-enough", "n", n));
            } else {
                validateScheduleLine(errors, n, matches);
            }
        });
    }

    private void validateScheduleLine(
            Multimap<String, ValidationError> errors,
            int n, List<Integer> matches) {
        final Set<Set<Integer>> setOfPairs = new HashSet<>();
        for (int i = 0; i < matches.size();) {
            setOfPairs.add(ImmutableSet.of(matches.get(i++), matches.get(i++)));
        }
        final Set<Set<Integer>> setOfExpectedPairs = new HashSet<>();
        for (int i = 0; i < n; ++i) {
            for (int j = i; j < n; ++j) {
                setOfExpectedPairs.add(ImmutableSet.of(i, j));
            }
        }
        final Set<Set<Integer>> unexpectedMatches =  new HashSet<>(setOfPairs);
        unexpectedMatches.removeAll(setOfExpectedPairs);
        if (!unexpectedMatches.isEmpty()) {
            errors.put(GROUP_RULE + SCHEDULE,
                    ofTemplate("unexpected-matches", "matches", unexpectedMatches));
            return;
        }
        setOfExpectedPairs.removeAll(setOfPairs);
        if (!setOfExpectedPairs.isEmpty()) {
            errors.put(GROUP_RULE + SCHEDULE,
                    ofTemplate("missing-matches", "matches", setOfExpectedPairs));
        }
    }
}
