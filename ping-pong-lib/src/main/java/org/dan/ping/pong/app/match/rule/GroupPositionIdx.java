package org.dan.ping.pong.app.match.rule;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class GroupPositionIdx implements Comparable<GroupPositionIdx> {
    private final int idx;

    @Override
    public int compareTo(GroupPositionIdx o) {
        return Integer.compare(idx, o.idx);
    }

    public GroupPositionIdx plus(int i) {
        return new GroupPositionIdx(idx + i);
    }
}
