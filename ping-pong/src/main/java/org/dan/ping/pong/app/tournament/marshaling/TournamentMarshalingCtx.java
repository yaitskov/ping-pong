package org.dan.ping.pong.app.tournament.marshaling;

import org.springframework.context.annotation.Import;

@Import({TournamentMarshalingResource.class, TournamentMarshalingService.class})
public class TournamentMarshalingCtx {
}
