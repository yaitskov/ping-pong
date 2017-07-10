package org.dan.ping.pong.app.user;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.dan.ping.pong.app.auth.AuthService.USER_PART_SESSION_LEN;
import static org.dan.ping.pong.app.user.UserResource.USER_INFO_BY_SESSION;
import static org.dan.ping.pong.mock.Generators.genEmail;
import static org.dan.ping.pong.mock.Generators.genFirstLastName;
import static org.dan.ping.pong.mock.Generators.genPhone;
import static org.dan.ping.pong.mock.Generators.genStr;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.sys.ctx.BaseTestContext;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.ws.rs.client.Entity;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = {BaseTestContext.class})
public class UserJerseyTest extends AbstractSpringJerseyTest {
    @Test
    public void registerAuthAndGetInfo() {
        final String name = genFirstLastName();
        final String email = genEmail();
        final String sessionPart = genStr(USER_PART_SESSION_LEN);
        final Optional<String> phone = Optional.of(genPhone());
        final UserRegistration response = request().path(UserResource.USER_REGISTER)
                .request(APPLICATION_JSON_TYPE)
                .post(Entity.entity(UserRegRequest.builder()
                        .name(name)
                        .sessionPart(sessionPart)
                        .email(Optional.of(email))
                        .phone(phone)
                        .build(), APPLICATION_JSON_TYPE))
                .readEntity(UserRegistration.class);
        assertThat(response.getUid(), Matchers.greaterThan(0));
        assertThat(response.getSession(), containsString(sessionPart));
        final UserInfo userInfo = request().path(USER_INFO_BY_SESSION
                + "/" + response.getSession())
                .request().get(UserInfo.class);
        assertEquals(response.getUid(), userInfo.getUid());
        assertEquals(name, userInfo.getName());
        assertEquals(Optional.of(email), userInfo.getEmail());
        assertEquals(phone, userInfo.getPhone());
    }
}
