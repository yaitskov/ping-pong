package org.dan.ping.pong.app.tournament;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class TournamentCounters {
    @JsonProperty("g")
    private int groupId;

    @JsonProperty("m")
    private int matchId;

    @JsonProperty("c")
    private int categoryId;
}
