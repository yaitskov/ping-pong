package org.dan.ping.pong.app.tournament;

import static java.util.Collections.emptyMap;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.place.PlaceLink;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DraftingTournamentInfo {
    private Tid tid;
    private String name;
    private Instant opensAt;
    private Optional<Tid> previousTid = Optional.empty();
    private Optional<Double> ticketPrice = Optional.empty();
    private PlaceLink place;
    private Map<Integer, BidState> categoryState = emptyMap();
    private boolean iAmAdmin;
    private Collection<CategoryLink> categories;
    private TournamentState state;
    private TournamentRules rules;
    private int enlisted;
}
