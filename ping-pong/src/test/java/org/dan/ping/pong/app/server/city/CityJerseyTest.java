package org.dan.ping.pong.app.server.city;

import static org.dan.ping.pong.app.server.city.CityResource.CITY_CREATE;
import static org.dan.ping.pong.app.server.city.CityResource.CITY_LIST;
import static org.dan.ping.pong.app.server.country.CountryResource.COUNTRY_CREATE;
import static org.dan.ping.pong.mock.AdminSessionGenerator.ADMIN_SESSION;
import static org.dan.ping.pong.mock.Generators.genStr;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.server.country.CountryDao;
import org.dan.ping.pong.app.server.country.NewCountry;
import org.dan.ping.pong.mock.TestAdmin;
import org.dan.ping.pong.sys.ctx.TestCtx;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = TestCtx.class)
public class CityJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    @Named(ADMIN_SESSION)
    private TestAdmin adminSession;

    @Inject
    private CountryDao countryDao;

    @Inject
    private CountryDao cityDao;

    public static class CityList extends ArrayList<CityLink> {}

    @Test
    public void create() {
        final String name = genStr();
        final int countryId =  myRest().post(COUNTRY_CREATE, adminSession,
                NewCountry.builder().name(name).build())
                .readEntity(Integer.class);
        final int cityId =  myRest().post(CITY_CREATE, adminSession,
                NewCity.builder().name(name).countryId(countryId).build())
                .readEntity(Integer.class);

        assertThat(countryId, greaterThan(0));
        assertThat(myRest().get(CITY_LIST + countryId, CityList.class),
                hasItem(allOf(hasProperty("id", is(cityId)),
                        hasProperty("name", is(name)))));
    }
}
