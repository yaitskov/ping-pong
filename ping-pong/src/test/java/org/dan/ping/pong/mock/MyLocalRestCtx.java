package org.dan.ping.pong.mock;

import org.springframework.context.annotation.Bean;

import javax.ws.rs.client.Client;

public class MyLocalRestCtx {
    @Bean
    public MyLocalRest myLocalRest(Client client) {
        return new MyLocalRest(client);
    }
}
