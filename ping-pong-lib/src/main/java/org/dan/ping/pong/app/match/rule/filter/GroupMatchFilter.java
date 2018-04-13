package org.dan.ping.pong.app.match.rule.filter;

import org.dan.ping.pong.app.match.MatchInfo;

import java.util.Set;
import java.util.stream.Stream;

public class GroupMatchFilter {
    public static Stream<MatchInfo> applyFilters(
            Stream<MatchInfo> origin, Set<FilterMarker> filters) {
        Stream<MatchInfo> s = origin;
        for (FilterMarker filter : filters) {
            s = filter.filterMatches(s);
        }
        return s;
    }
}
