package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class OpenTournamentParams {
    private int tables;
    private int numberOfParticipants;
    private TournamentProps props;

    public static class OpenTournamentParamsBuilder {
        private TournamentProps props = TournamentProps.builder()
                .quitsFromGroup(1).build();
        private int tables = 1;
        private int numberOfParticipants = 2;
    }
}
