package org.dan.ping.pong.app.bid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.Tid;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRename {
    private Tid tid;
    private Uid uid;
    private String expectedName;
    private String newName;
}