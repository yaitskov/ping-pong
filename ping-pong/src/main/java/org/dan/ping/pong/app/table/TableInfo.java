package org.dan.ping.pong.app.table;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
public class TableInfo {
    private int tableId;
    private String label;
    private int pid;
    private TableState state;
    private Optional<Integer> mid;
}
