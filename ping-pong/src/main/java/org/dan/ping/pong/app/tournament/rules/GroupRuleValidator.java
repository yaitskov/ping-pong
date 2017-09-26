package org.dan.ping.pong.app.tournament.rules;

import static org.dan.ping.pong.app.tournament.rules.ValidationError.ofTemplate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.group.GroupSchedule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupRuleValidator {
    public static final String GROUP_RULE = "group-rule";
    public static final String MAX_SIZE = ".max-size";
    public static final String OUT_OF_RANGE = "out-of-range";
    public static final String QUITS = ".quits";
    public static final String VALUE_NULL = "value-null";
    public static final String SCHEDULE = ".schedule";
    static final String UNEXPECTED_MATCHES = "unexpected-matches";
    public static final String MISSING_MATCHES = "missing-matches";
    static final String MATCHES = "matches";

    public void validate(Multimap<String, ValidationError> errors, GroupRules group) {
        if (group == null) {
            errors.put(GROUP_RULE, ofTemplate(VALUE_NULL));
            return;
        }
        if (group.getGroupSize() < 2 || group.getGroupSize() > 20) {
            errors.put(GROUP_RULE + MAX_SIZE, ofTemplate(OUT_OF_RANGE));
        }
        if (group.getQuits() < 1 || group.getQuits() >= group.getGroupSize()) {
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
            } else {
                validateScheduleLine(errors, n, matches);
            }
        });
    }

    void validateScheduleLine(
            Multimap<String, ValidationError> errors,
            int n, List<Integer> matches) {
        final Set<Set<Integer>> setOfPairs = new HashSet<>();
        for (int i = 0; i < matches.size();) {
            setOfPairs.add(ImmutableSet.of(matches.get(i++), matches.get(i++)));
        }
        final Set<Set<Integer>> setOfExpectedPairs = new HashSet<>();
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                setOfExpectedPairs.add(ImmutableSet.of(i, j));
            }
        }
        final Set<Set<Integer>> unexpectedMatches =  new HashSet<>(setOfPairs);
        unexpectedMatches.removeAll(setOfExpectedPairs);
        if (!unexpectedMatches.isEmpty()) {
            errors.put(GROUP_RULE + SCHEDULE,
                    ofTemplate(UNEXPECTED_MATCHES, MATCHES, unexpectedMatches, n));
            return;
        }
        setOfExpectedPairs.removeAll(setOfPairs);
        if (!setOfExpectedPairs.isEmpty()) {
            errors.put(GROUP_RULE + SCHEDULE,
                    ofTemplate(MISSING_MATCHES, MATCHES, setOfExpectedPairs, n));
        }
    }
}
