package org.dan.ping.pong.app.castinglots;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.ParticipantLink;

import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankedBid {
    private ParticipantLink user;
    private Optional<Integer> seed;
    private Optional<Integer> providedRank;
}
