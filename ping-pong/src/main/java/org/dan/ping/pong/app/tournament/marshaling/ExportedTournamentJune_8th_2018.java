package org.dan.ping.pong.app.tournament.marshaling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.app.tournament.TournamentType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExportedTournamentJune_8th_2018 implements ExportedTournament {
    private SportType sport;
    private Tid tid;
    private String name;
    private TournamentType type;
    private Map<Uid, ParticipantMemState> participants;
    private Map<Mid, MatchInfo> matches;
    private Map<Integer, GroupInfo> groups;
    private Map<Integer, CategoryLink> categories;
    private TournamentRules rule;
    private TournamentState state;
    private Optional<Instant> completeAt;
    private Optional<Double> ticketPrice;
    private Instant opensAt;
}
