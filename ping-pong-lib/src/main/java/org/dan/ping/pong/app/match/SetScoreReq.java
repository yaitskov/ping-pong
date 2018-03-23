package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetScoreReq {
    private Tid tid;
    private Mid mid;
    private int setOrdNumber;
    private List<IdentifiedScore> scores;

    public SetScoreReq atomic(int setOrdNumber, List<IdentifiedScore> scores) {
        return SetScoreReq.builder()
                .tid(tid)
                .mid(mid)
                .setOrdNumber(setOrdNumber)
                .scores(scores)
                .build();
    }
}
