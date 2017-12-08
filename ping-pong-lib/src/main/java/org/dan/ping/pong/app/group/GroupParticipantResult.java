package org.dan.ping.pong.app.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Uid;

import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GroupParticipantResult {
    private Uid uid;
    private String name;
    private Map<Uid, GroupMatchResult> matches;
    private int punkts;
    private int seedPosition;
    private int finishPosition;
}
