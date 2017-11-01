package org.dan.ping.pong.app.match;


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
public class ResetSetScore {
    private Tid tid;
    private int mid;
    private int setNumber;
}
