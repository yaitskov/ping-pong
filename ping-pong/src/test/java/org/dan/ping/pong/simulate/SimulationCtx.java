package org.dan.ping.pong.simulate;

import static org.dan.ping.pong.sys.ctx.PropertiesContext.DEFAULT_PROPERTY_PRIORITY;
import static org.junit.Assert.assertTrue;

import org.dan.ping.pong.app.match.ForTestBidDao;
import org.dan.ping.pong.app.match.ForTestMatchDao;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.score.MatchScoreDao;
import org.dan.ping.pong.mock.MyRest;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.sys.ctx.BaseContextWithoutJersey;
import org.dan.ping.pong.sys.ctx.PropertyFileRef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;

import java.net.URI;

import javax.ws.rs.client.Client;

@Import({BaseContextWithoutJersey.class, MatchDao.class,
        MatchScoreDao.class, ForTestMatchDao.class,
        ForTestBidDao.class, Simulator.class})
public class SimulationCtx {
    @Bean
    public MyRest myRest(Client client, @Value("${base.api.url}") URI baseUri) {
        return new MyRest(client, baseUri);
    }

    @Bean
    public PropertyFileRef simulationProperties() {
        return PropertyFileRef.builder()
                .resource(new ClassPathResource("simulation.properties"))
                .priority(DEFAULT_PROPERTY_PRIORITY + 1)
                .build();
    }

    @Bean
    public Void check(@Value("${simulation.profile}") boolean enabled) {
        assertTrue("run simulation with -P simulate maven profile", enabled);
        return null;
    }
}
