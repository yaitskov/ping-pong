package org.dan.ping.pong.app.match;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        defaultImpl=MyPendingMatchPingPongSport.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MyPendingMatchPingPongSport.class, name = "PingPong"),
        @JsonSubTypes.Type(value = MyPendingMatchTennisSport.class, name = "Tennis") })
public interface MyPendingMatchSport {
}
