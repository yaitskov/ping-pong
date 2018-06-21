package org.dan.ping.pong.app.place;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class PlaceRules {
    @JsonProperty("ad")
    private ArenaDistributionPolicy arenaDistribution;
}
