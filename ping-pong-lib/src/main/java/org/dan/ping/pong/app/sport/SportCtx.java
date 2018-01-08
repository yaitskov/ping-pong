package org.dan.ping.pong.app.sport;

import static java.util.stream.Collectors.toMap;

import org.dan.ping.pong.app.sport.pingpong.PingPongSport;
import org.dan.ping.pong.app.sport.tennis.TennisSport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

@Import({PingPongSport.class, TennisSport.class})
public class SportCtx {
    @Bean
    public Sports sports(List<Sport> sports) {
        return new Sports(sports.stream().collect(toMap(Sport::getType, o -> o)));
    }
}
