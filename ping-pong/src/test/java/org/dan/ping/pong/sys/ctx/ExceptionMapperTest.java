package org.dan.ping.pong.sys.ctx;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RULES;
import static org.dan.ping.pong.util.matcher.UuidMatcher.isUuid;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.tournament.TournamentUpdate;
import org.dan.ping.pong.sys.error.Error;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.core.Response;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class ExceptionMapperTest extends AbstractSpringJerseyTest {
    public static final String UNEXPECTED_FIELD = "unexpectedField";
    public static class BadObject {
        @JsonProperty(UNEXPECTED_FIELD)
        public String a = "q231";
    }

    @Test
    public void wrongObjectGenereatesBadRequest() {
        final Response response = myRest().post(TOURNAMENT_RULES, new BadObject());
        assertThat(response.getHeaderString(CONTENT_TYPE), is(APPLICATION_JSON));
        assertThat(response.getStatus(), is(BAD_REQUEST.getStatusCode()));
        assertThat(response.readEntity(Error.class),
                allOf(hasProperty("id", isUuid()),
                        hasProperty("message", containsString(UNEXPECTED_FIELD))));
    }
}
