package org.dan.ping.pong.app.server.country;

import static org.dan.ping.pong.app.server.country.CountryResource.COUNTRY_CREATE;
import static org.dan.ping.pong.app.server.country.CountryResource.COUNTRY_LIST;
import static org.dan.ping.pong.mock.AdminSessionGenerator.ADMIN_SESSION;
import static org.dan.ping.pong.mock.Generators.genStr;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;

import org.dan.ping.pong.JerseySpringTest;
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
public class CountryJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    @Named(ADMIN_SESSION)
    private TestAdmin adminSession;

    @Inject
    private CountryDao countryDao;

    public static class CountryList extends ArrayList<CountryLink> {}

    @Test
    public void create() {
        final String name = genStr();
        final int countryId =  myRest().post(COUNTRY_CREATE, adminSession,
                NewCountry.builder().name(name).build())
                .readEntity(Integer.class);
        assertThat(countryId, greaterThan(0));
        assertThat(
                myRest().get(COUNTRY_LIST, CountryList.class),
                hasItem(allOf(hasProperty("id", is(countryId)),
                        hasProperty("name", is(name)))));
    }
}
