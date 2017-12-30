package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;

import java.util.Optional;

@Getter
@Setter
@Wither
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsoleTournamentRules {
    private Optional<CustomConsoleRules> custom;
}
