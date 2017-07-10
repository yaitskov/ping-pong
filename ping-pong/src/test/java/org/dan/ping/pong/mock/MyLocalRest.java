package org.dan.ping.pong.mock;

import static org.dan.ping.pong.test.AbstractSpringJerseyTest.provideBaseUri;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

public class MyLocalRest extends MyRest {
    private final Client client;
    public MyLocalRest(Client client) {
        super(client, null);
        this.client = client;
    }

    @Override
    public WebTarget request() {
        return client.target(provideBaseUri());
    }
}
