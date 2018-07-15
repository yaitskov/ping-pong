package org.dan.ping.pong.app.tournament.console;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.Tid;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateConsoleTournamentReq {
    @Valid
    @NotNull
    private Tid parentTid;

    @NotNull
    private TournamentRelationType consoleType;
}
