package org.dan.ping.pong.app.suggestion;

import static org.dan.ping.pong.app.suggestion.SuggestionIndexType.F3L3;
import static org.dan.ping.pong.app.suggestion.SuggestionIndexType.Initials;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPatternInterpreter {
    public List<PatternInterpretation> interpretPattern(String pattern) {
        pattern = pattern.trim().toLowerCase();
        final List<PatternInterpretation> result = new ArrayList<>();
        if (pattern.length() == 1) {
            interpretSingleLetterPattern(pattern, result);
        } else if (pattern.length() == 2) {
            exact2Letters(pattern, result);
        } else if (pattern.length() > 2) {
            interpretMore2LettersPattern(pattern, result);
        } else if (pattern.isEmpty()) {
            // ok
        } else {
            throw internalError("pattern [" + pattern + "] is not handled");
        }
        return result;
    }

    protected void exact2Letters(String pattern, List<PatternInterpretation> result) {
        fullInitials(pattern, result);
        first3Last3Name(pattern, result);
    }

    protected void interpretMore2LettersPattern(
            String pattern, List<PatternInterpretation> result) {
        final int spaceIdx = pattern.indexOf(' ');
        if (spaceIdx > 0) {
            String part2 = pattern.substring(spaceIdx + 1).trim();
            String part1 = pattern.substring(0, spaceIdx);
            if (part1.length() == 1 && part2.length() == 1) {
                fullInitials(part1 + part2, result);
            } else {
                if (part1.length() < part2.length()) {
                    String tmp = part1;
                    part1 = part2;
                    part2 = tmp;
                }
                final String prefix1 = cutPrefix(part1);
                final String prefix2 = cutPrefix(part2);
                first3Last3Name(prefix1 + prefix2, result);
                fullInitials("" + part1.charAt(0) + part2.charAt(0), result);
            }
        } else {
            first3Last3Name(pattern.substring(0, 3), result);
            fullInitials(pattern.substring(0, 2), result);
        }
    }

    protected void interpretSingleLetterPattern(
            String pattern, List<PatternInterpretation> result) {
        result.add(PatternInterpretation
                .builder()
                .pattern(pattern + "_")
                .like(true)
                .idxType(Initials)
                .build());
    }

    protected void fullInitials(String pattern, List<PatternInterpretation> result) {
        result.add(PatternInterpretation
                .builder()
                .idxType(Initials)
                .pattern(pattern)
                .build());
    }

    protected String cutPrefix(String part) {
        switch (part.length()) {
            case 1:
                return part + "__";
            case 2:
                return part + "_";
            case 3:
                return part;
            default:
                return part.substring(0, 3);
        }
    }

    protected void first3Last3Name(String pattern, List<PatternInterpretation> result) {
        result.add(PatternInterpretation
                .builder()
                .pattern(appendWildCardSuffix(pattern))
                .like(true)
                .idxType(F3L3)
                .build());
    }

    protected abstract String appendWildCardSuffix(String pattern);
}