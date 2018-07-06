package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResignTournament {
    private Tid tid;
    private Optional<Integer> cid;

    public static ResignTournament resignOfTid(Tid tid) {
        return new ResignTournament(tid, Optional.empty());
    }
}
