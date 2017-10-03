package org.dan.ping.pong.app.server.match;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetSetScore {
    private int tid;
    private int mid;
    private int setNumber;
}
