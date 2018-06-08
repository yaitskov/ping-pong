package org.dan.ping.pong.app.tournament.marshaling;

import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.util.time.Clocker;

import javax.inject.Inject;

public class TournamentMarshalingService {
    @Inject
    private Clocker clocker;

    public TournamentEnvelope exportState(TournamentMemState tournament) {
        return TournamentEnvelope
                .builder()
                .exportedAt(clocker.get())
                .tournament(
                        ExportedTournamentJune_8th_2018
                                .builder()
                                .name(tournament.getName())
                                .type(tournament.getType())
                                .state(tournament.getState())
                                .opensAt(tournament.getOpensAt())
                                .completeAt(tournament.getCompleteAt())
                                .sport(tournament.getSport())
                                .rule(tournament.getRule())
                                .groups(tournament.getGroups())
                                .participants(tournament.getParticipants())
                                .matches(tournament.getMatches())
                                .categories(tournament.getCategories())
                                .ticketPrice(tournament.getTicketPrice())
                                .build())
                .build();
    }
}
