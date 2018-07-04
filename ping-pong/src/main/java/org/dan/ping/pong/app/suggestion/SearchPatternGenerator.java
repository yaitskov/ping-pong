package org.dan.ping.pong.app.suggestion;

public class SearchPatternGenerator extends AbstractPatternInterpreter {
    @Override
    protected String appendWildCardSuffix(String pattern) {
        if (pattern.length() < 6) {
            return pattern + "%";
        }
        return pattern;
    }
}
