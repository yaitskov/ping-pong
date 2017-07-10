package org.dan.ping.pong.app.place;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.mock.AdminSessionGenerator.ADMIN_SESSION;
import static org.dan.ping.pong.app.place.PlaceResource.PLACES;
import static org.dan.ping.pong.app.place.PlaceResource.PLACE_CREATE;
import static org.dan.ping.pong.mock.Generators.genStr;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.sys.ctx.BaseTestContext;
import org.dan.ping.pong.mock.TestAdmin;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = {BaseTestContext.class})
public class PlaceJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    @Named(ADMIN_SESSION)
    private TestAdmin session;

    @Test
    public void create() {
        final String name = genStr();
        final String address = genStr();
        final String city = genStr();
        final int placeId = request().path(PLACE_CREATE)
                .request(APPLICATION_JSON)
                .header(SESSION, session.getSession())
                .post(Entity.entity(CreatePlace.builder()
                        .name(name)
                        .address(PlaceAddress.builder()
                                .address(address)
                                .city(city)
                                .build())
                        .build(), APPLICATION_JSON))
                .readEntity(Integer.class);

        assertThat(placeId, Matchers.greaterThan(0));
        final List<PlaceLink> placeLink = request().path(PLACES).request()
                .header(SESSION, session.getSession())
                .get(new GenericType<List<PlaceLink>>(){});
        assertThat(placeLink, hasItems(
                hasProperty("pid", is(placeId)),
                hasProperty("address",
                        hasProperty("address", is(address))),
                hasProperty("name", is(name))));
    }
}
