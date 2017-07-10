package org.dan.ping.pong.app.tournament;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.place.PlaceLink;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DraftingTournamentInfo {
    private int tid;
    private String name;
    private Instant opensAt;
    private Optional<Integer> previousTid = Optional.empty();
    private Optional<Double> ticketPrice = Optional.empty();
    private PlaceLink place;
    private boolean iAmEnlisted;
    private boolean iAmAdmin;
    private int alreadyEnlisted;
    private List<CategoryInfo> categories;
    @JsonIgnore
    private TournamentState state;
}
