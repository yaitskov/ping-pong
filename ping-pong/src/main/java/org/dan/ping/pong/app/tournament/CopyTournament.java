package org.dan.ping.pong.app.tournament;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Wither;

import java.time.Instant;

@Getter
@Setter
@Wither
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class CopyTournament {
    private Tid originTid;
    private Instant opensAt;
    private String name;
}
