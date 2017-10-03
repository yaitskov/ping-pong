package org.dan.ping.pong.app.tournament.rules;

import static com.google.common.collect.HashMultimap.create;
import static com.google.common.primitives.Ints.asList;
import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.GROUP_RULE;
import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.MATCHES;
import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.MISSING_MATCHES;
import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.SCHEDULE;
import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.UNEXPECTED_MATCHES;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.group.GroupSchedule;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

public class GroupRuleValidatorTest {
    private static final HashMultimap<Object, Object> EMPTY_MMAP = create();
    private GroupRuleValidator sut = new GroupRuleValidator();
    private Multimap<String, ValidationError> errors;

    @Before
    public void setUp() {
        errors = create();
    }

    @Test
    public void findMissingMatches() {
        sut.validateScheduleLine(errors, 3, asList(0, 1, 1, 2));
        final ValidationError validationError = errors.get(GROUP_RULE + SCHEDULE)
                .stream().findFirst().get();
        assertEquals(
                ImmutableSet.of(ImmutableSet.of(0, 2)),
                validationError.getParams().get(MATCHES));
        assertEquals(MISSING_MATCHES, validationError.getMessage());
    }

    @Test
    public void findUnexpectedMatches() {
        sut.validateScheduleLine(errors, 2, asList(0, 1, 1, 2));
        final ValidationError validationError = errors.get(GROUP_RULE + SCHEDULE)
                .stream().findFirst().get();
        assertEquals(
                ImmutableSet.of(ImmutableSet.of(1, 2)),
                validationError.getParams().get(MATCHES));
        assertEquals(UNEXPECTED_MATCHES, validationError.getMessage());
    }

    @Test
    public void passWithoutSchedule() {
        sut.validate(errors, GroupRules.builder()
                .quits(2).groupSize(8).build());
        assertEquals(errors, EMPTY_MMAP);
    }

    @Test
    public void passWithDefaultSchedule() {
        sut.validate(errors, GroupRules.builder()
                .quits(1).groupSize(2)
                .schedule(Optional.of(GroupSchedule.DEFAULT_SCHEDULE))
                .build());
        assertEquals(errors, EMPTY_MMAP);
    }

    @Test
    public void passWithSchedule() {
        sut.validate(errors, GroupRules.builder()
                .quits(2).groupSize(8)
                .schedule(Optional.of(GroupSchedule.builder()
                        .size2Schedule(ImmutableMap.of(2, asList(0, 1)))
                        .build()))
                .build());
        assertEquals(errors, EMPTY_MMAP);
    }
}
