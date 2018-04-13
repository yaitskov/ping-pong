package org.dan.ping.pong.app.match.rule.filter;

import org.dan.ping.pong.app.match.MatchInfo;

import java.util.stream.Stream;

public interface FilterMarker {
    Stream<MatchInfo> filterMatches(Stream<MatchInfo> s);
}
