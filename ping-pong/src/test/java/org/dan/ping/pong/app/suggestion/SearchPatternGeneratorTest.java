package org.dan.ping.pong.app.suggestion;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.dan.ping.pong.app.suggestion.SuggestionIndexType.F3L3;
import static org.dan.ping.pong.app.suggestion.SuggestionIndexType.Initials;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SearchPatternGeneratorTest {
    private SearchPatternGenerator sut = new SearchPatternGenerator();

    @Test
    public void skipEmpty() {
        assertThat(sut.interpretPattern(""), is(emptyList()));
    }

    @Test
    public void skipSpace() {
        assertThat(sut.interpretPattern(" "), is(emptyList()));
    }

    @Test
    public void oneLetterAsInitial() {
        assertThat(sut.interpretPattern("F"),
                is(singletonList(initialsLike("f_"))));
    }

    public PatternInterpretation initialsLike(String f) {
        return PatternInterpretation
                .builder()
                .like(true)
                .pattern(f)
                .idxType(Initials)
                .build();
    }

    @Test
    public void twoLettersAsInitial() {
        assertThat(sut.interpretPattern("fA"),
                is(asList(
                        initials("fa"),
                        f3l3Like("fa%"))));
    }

    @Test
    public void twoSameLettersAsInitial() {
        assertThat(sut.interpretPattern("fF"),
                is(asList(initials("ff"),
                        f3l3Like("ff%"))));
    }

    @Test
    public void twoSpaceDelimitedLettersAsInitial() {
        assertThat(sut.interpretPattern("A b"),
                is(singletonList(initials("ab"))));
    }

    @Test
    public void oneLongWord() {
        assertThat(sut.interpretPattern("hello"),
                is(asList(
                        f3l3Like("hel%"),
                        initials("he"))));
    }

    @Test
    public void twoLongWords() {
        assertThat(sut.interpretPattern("hello world"),
                is(asList(
                        f3l3Like("helwor"),
                        initials("hw"))));
    }

    @Test
    public void twoShortWords() {
        assertThat(sut.interpretPattern("he wo"),
                is(asList(
                        f3l3Like("he_wo_"),
                        initials("hw"))));
    }

    @Test
    public void longestWordFirst() {
        assertThat(sut.interpretPattern("h wo"),
                is(asList(
                        f3l3Like("wo_h__"),
                        initials("wh"))));
    }

    public PatternInterpretation initials(String fa) {
        return PatternInterpretation
                .builder()
                .pattern(fa)
                .idxType(Initials)
                .build();
    }

    public PatternInterpretation f3l3Like(String pattern) {
        return PatternInterpretation
                .builder()
                .pattern(pattern)
                .idxType(F3L3)
                .like(true)
                .build();
    }
}
