package org.dan.ping.pong.app.place;

import static org.dan.ping.pong.app.place.PlaceResource.PLACES;
import static org.dan.ping.pong.app.place.PlaceResource.PLACE_CREATE;
import static org.dan.ping.pong.mock.AdminSessionGenerator.ADMIN_SESSION;
import static org.dan.ping.pong.mock.Generators.genStr;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.city.CityLink;
import org.dan.ping.pong.mock.DaoEntityGenerator;
import org.dan.ping.pong.mock.TestAdmin;
import org.dan.ping.pong.sys.ctx.BaseTestContext;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = {BaseTestContext.class})
public class PlaceJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    @Named(ADMIN_SESSION)
    private TestAdmin session;

    @Inject
    private DaoEntityGenerator generator;

    @Test
    public void create() {
        final String name = genStr();
        final String address = genStr();
        final String cityName = genStr();
        final int cityId = generator.genCity(cityName, session.getUid());
        final Pid placeId = myRest().post(PLACE_CREATE, session,
                CreatePlace.builder()
                        .name(name)
                        .address(PlaceAddress.builder()
                                .address(address)
                                .city(CityLink.builder().id(cityId).build())
                                .build())
                        .build())
                .readEntity(Pid.class);

        assertThat(placeId.getPid(), Matchers.greaterThan(0));
        final List<PlaceLink> placeLink = myRest().get(PLACES,
                 session, new GenericType<List<PlaceLink>>(){});
        assertThat(placeLink, hasItems(
                hasProperty("pid", is(placeId)),
                hasProperty("address",
                        allOf(
                                hasProperty("city",
                                        allOf(
                                                hasProperty("name", is(cityName)),
                                                hasProperty("id", is(cityId)))),
                                hasProperty("address", is(address)))),
                hasProperty("name", is(name))));
    }
}
