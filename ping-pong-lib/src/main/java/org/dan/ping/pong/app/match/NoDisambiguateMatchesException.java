package org.dan.ping.pong.app.match;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.match.rule.GroupParticipantOrder;

@Getter
@RequiredArgsConstructor
public class NoDisambiguateMatchesException extends RuntimeException {
    private final GroupParticipantOrder uids;
}
