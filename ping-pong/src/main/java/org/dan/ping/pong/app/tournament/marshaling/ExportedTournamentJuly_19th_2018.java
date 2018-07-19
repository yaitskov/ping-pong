package org.dan.ping.pong.app.tournament.marshaling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.CategoryMemState;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
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
public class ExportedTournamentJuly_19th_2018 implements ExportedTournament {
    private SportType sport;
    private Tid tid;
    private String name;
    private TournamentType type;
    private Map<Bid, ParticipantMemState> participants;
    private Map<Mid, MatchInfo> matches;
    private Map<Gid, GroupInfo> groups;
    private Map<Cid, CategoryMemState> categories;
    private TournamentRules rule;
    private TournamentState state;
    private Optional<Instant> completeAt;
    private Optional<Double> ticketPrice;
    private Instant opensAt;
}
