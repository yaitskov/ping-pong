package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Want;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.BidState;

import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EnlistTournament implements Enlist {
    private int categoryId;
    private Tid tid;
    private Optional<Integer> providedRank = Optional.empty();
    private BidState bidState = Want;

    @JsonIgnore
    public int getCid() {
        return categoryId;
    }

    public static class EnlistTournamentBuilder {
        Optional<Integer> providedRank = Optional.empty();
        BidState bidState = Want;
    }
}
