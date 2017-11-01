package org.dan.ping.pong.app.tournament;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.place.PlaceLink;

import java.time.Instant;
import java.util.Collection;
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
    private Optional<Integer> myCategoryId = Optional.empty();
    private boolean iAmAdmin;
    private Collection<CategoryInfo> categories;
    private TournamentState state;
    private Optional<BidState> bidState = Optional.empty();
    private TournamentRules rules;
    private int enlisted;
}
