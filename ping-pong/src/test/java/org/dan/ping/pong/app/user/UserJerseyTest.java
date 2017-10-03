package org.dan.ping.pong.app.user;

import static org.dan.ping.pong.app.auth.AuthService.USER_PART_SESSION_LEN;
import static org.dan.ping.pong.app.user.UserDao.EMAIL_IS_USED;
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
import org.dan.ping.pong.sys.error.Error;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.ws.rs.core.Response;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = {BaseTestContext.class})
public class UserJerseyTest extends AbstractSpringJerseyTest {
    @Test
    public void registerAuthAndGetInfo() {
        final String name = genFirstLastName();
        final String email = genEmail();
        final String sessionPart = genStr(USER_PART_SESSION_LEN);
        final Optional<String> phone = Optional.of(genPhone());
        final UserRegistration response = myRest().post(
                UserResource.USER_REGISTER,
                UserRegRequest.builder()
                        .name(name)
                        .sessionPart(sessionPart)
                        .email(Optional.of(email))
                        .phone(phone)
                        .build())
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

    @Test
    public void registerTwice() {
        final String name = genFirstLastName();
        final String email = genEmail();
        final String sessionPart = genStr(USER_PART_SESSION_LEN);
        final Optional<String> phone = Optional.of(genPhone());
        final UserRegRequest entity = UserRegRequest.builder()
                .name(name)
                .sessionPart(sessionPart)
                .email(Optional.of(email))
                .phone(phone)
                .build();
        myRest().post(UserResource.USER_REGISTER, entity);
        final Response response = myRest().post(UserResource.USER_REGISTER, entity);
        assertEquals(400, response.getStatus());
        assertEquals(EMAIL_IS_USED, response.readEntity(Error.class).getMessage());
    }
}
