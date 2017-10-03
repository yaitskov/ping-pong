package org.dan.ping.pong.app.server.tournament;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EnlistTournament implements Enlist {
    private int categoryId;
    private int tid;
    private Optional<Integer> providedRank = Optional.empty();

    @JsonIgnore
    public int getCid() {
        return categoryId;
    }

    public static class EnlistTournamentBuilder {
        Optional<Integer> providedRank = Optional.empty();
    }
}
