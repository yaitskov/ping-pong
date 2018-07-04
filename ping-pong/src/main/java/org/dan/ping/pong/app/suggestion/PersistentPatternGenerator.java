package org.dan.ping.pong.app.suggestion;

import java.util.List;

public class PersistentPatternGenerator extends AbstractPatternInterpreter  {
    @Override
    protected void exact2Letters(String pattern, List<PatternInterpretation> result) {
        first3Last3Name(pattern, result);
        interpretSingleLetterPattern(pattern.substring(0, 1), result);
    }

    protected void interpretMore2LettersPattern(
            String pattern, List<PatternInterpretation> result) {
        final int spaceIdx = pattern.indexOf(' ');
        if (spaceIdx > 0) {
            String part2 = pattern.substring(spaceIdx + 1).trim();
            String part1 = pattern.substring(0, spaceIdx);
            if (part1.length() == 1 && part2.length() == 1) {
                fullInitials(part1 + part2, result);
                if (!part1.equals(part2)) {
                    fullInitials(part2 + part1, result);
                }
            } else {
                final String prefix1 = cutPrefix(part1);
                final String prefix2 = cutPrefix(part2);
                first3Last3Name(prefix1 + prefix2, result);
                if (!prefix2.equals(prefix1)) {
                    first3Last3Name(prefix2 + prefix1, result);
                }
                fullInitials("" + part1.charAt(0) + part2.charAt(0), result);
                if (part2.charAt(0) != part1.charAt(0)) {
                    fullInitials("" + part2.charAt(0) + part1.charAt(0), result);
                }
            }
        } else {
            first3Last3Name(pattern.substring(0, 3), result);
            fullInitials(pattern.substring(0, 2), result);
            if (pattern.charAt(1) != pattern.charAt(0)) {
                fullInitials("" + pattern.charAt(1) + pattern.charAt(0), result);
            }
        }
    }

    @Override
    protected String appendWildCardSuffix(String pattern) {
        return pattern;
    }
}
