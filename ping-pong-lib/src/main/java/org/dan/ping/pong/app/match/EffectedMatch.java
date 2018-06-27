package org.dan.ping.pong.app.match;

import static java.util.stream.Collectors.toList;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.ParticipantLink;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EffectedMatch {
    private Mid mid;
    private List<ParticipantLink> participants;

    public static EffectedMatch ofMatchInfo(TournamentMemState tournament, MatchInfo minfo) {
        return EffectedMatch.builder()
                .mid(minfo.getMid())
                .participants(minfo.getParticipantIdScore()
                        .keySet().stream().map(tournament::getBid)
                        .map(ParticipantMemState::toBidLink).collect(toList()))
                .build();
    }
}
