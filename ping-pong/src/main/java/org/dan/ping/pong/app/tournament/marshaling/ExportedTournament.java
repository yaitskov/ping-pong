package org.dan.ping.pong.app.tournament.marshaling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        defaultImpl = ExportedTournamentJuly_19th_2018.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExportedTournamentJuly_19th_2018.class, name = "June8th2018"),
        })
public interface ExportedTournament {
    String getName();
    Optional<Double> getTicketPrice();
    SportType getSport();
    Instant getOpensAt();
    TournamentType getType();
    TournamentRules getRule();
    Tid getTid();
    void setTid(Tid tid);
    Map<Cid, CategoryMemState> getCategories();
    Map<Gid, GroupInfo> getGroups();
    Map<Bid, ParticipantMemState> getParticipants();
    Map<Mid, MatchInfo> getMatches();
    TournamentState getState();
}
