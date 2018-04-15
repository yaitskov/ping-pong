package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.user.UserLink;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEffectedMatch {
    private List<UserLink> participants;

    public static List<NewEffectedMatch> toNewDisambiguationMatches(
            TournamentMemState tournament, Set<MatchParticipants> toBeCreated) {
        return toBeCreated.stream()
                .map(nm -> NewEffectedMatch
                        .builder()
                        .participants(asList(
                                tournament.getParticipant(nm.getUidLess()).toLink(),
                                tournament.getParticipant(nm.getUidMore()).toLink()))
                        .build())
                .collect(toList());
    }
}