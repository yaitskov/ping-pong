package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class GeneratedOpenTournament {
    private int tid;
    private int cid;
    private int pid;
    private List<Integer> tableIds;
    private List<TestUserSession> sessions;
}
