package org.dan.ping.pong.app.sport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.dan.ping.pong.app.match.MyPendingMatchSport;
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;
import org.dan.ping.pong.app.sport.tennis.TennisMatchRules;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        defaultImpl=PingPongMatchRules.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PingPongMatchRules.class, name = "PingPong"),
        @JsonSubTypes.Type(value = TennisMatchRules.class, name = "Tennis") })
public interface MatchRules {
    SportType sport();

    MyPendingMatchSport toMyPendingMatchSport();
}
