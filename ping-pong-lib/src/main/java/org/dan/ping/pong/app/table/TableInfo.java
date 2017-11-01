package org.dan.ping.pong.app.table;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.place.Pid;

import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
public class TableInfo {
    private int tableId;
    private String label;
    private Pid pid;
    private TableState state;
    private Optional<Integer> mid;

    public TableLink toLink() {
        return TableLink.builder()
                .id(tableId)
                .label(label)
                .build();
    }
}
