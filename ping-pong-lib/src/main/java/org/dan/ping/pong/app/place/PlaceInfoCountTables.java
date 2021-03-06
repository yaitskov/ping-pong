package org.dan.ping.pong.app.place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PlaceInfoCountTables {
    private Pid pid;
    private String name;
    private PlaceAddress address;
    private int tables;
}
