package org.dan.ping.pong.app.place;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.match.Pid;
import org.dan.ping.pong.app.table.TableInfo;

import java.util.Map;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class PlaceMemState {
    private final Pid pid;
    private String name;
    private Map<Integer, TableInfo> tables;
}
