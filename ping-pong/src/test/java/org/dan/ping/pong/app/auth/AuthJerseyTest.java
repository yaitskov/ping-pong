package org.dan.ping.pong.app.auth;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.dan.ping.pong.app.auth.AuthResource.AUTH_BY_ONE_TIME_TOKEN;
import static org.dan.ping.pong.app.auth.AuthResource.AUTH_GENERATE_SIGN_IN_LINK;
import static org.dan.ping.pong.app.auth.CheckSysAdminSessionResource.ANONYMOUS_SYS_ADMIN_CHECK;
import static org.dan.ping.pong.app.auth.CheckUserSessionResource.AUTH_USER_CHECK_SESSION;
import static org.dan.ping.pong.app.auth.SysAdminSignInResource.ANONYMOUS_SYS_ADMIN_SIGN_IN;
import static org.dan.ping.pong.app.user.UserType.User;
import static org.dan.ping.pong.mock.SysAdminGenerator.SYS_ADMIN_TEST_PASSWORD;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.UserSessionGenerator;
import org.dan.ping.pong.sys.ctx.BaseTestContext;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = {BaseTestContext.class})
public class AuthJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    SysAdmin sysAdmin;

    @Test
    public void authAndCheckAccess() {
        final Response response = myRest().post(
                ANONYMOUS_SYS_ADMIN_SIGN_IN,
                SysAdminAuth.builder()
                        .login(sysAdmin.getLogin())
                        .password(SYS_ADMIN_TEST_PASSWORD)
                        .build());
        checkSession(response.readEntity(String.class), true);
    }

    @Test
    public void checkSession() {
        checkSession("bad-session-id", false);
    }

    private void checkSession(String sessionId, boolean valid) {
        assertThat(
                myRest().get(ANONYMOUS_SYS_ADMIN_CHECK + sessionId, Boolean.class),
                is(valid));
    }

    @Test
    public void checkUnAuthorized() {
        assertEquals(UNAUTHORIZED.getStatusCode(),
                myRest().post(AUTH_USER_CHECK_SESSION, 1)
                        .getStatus());
        assertEquals(UNAUTHORIZED.getStatusCode(),
                myRest().post(AUTH_USER_CHECK_SESSION, "bad-session-123", 1)
                        .getStatus());
    }

    @Inject
    private UserSessionGenerator userSessionGenerator;

    @Inject
    private AuthDao authDao;

    @Test
    public void generateOneTimeSignInToken() {
        final TestUserSession userSession = userSessionGenerator
                .generateUserSessions(1).get(0);
        myRest().voidPost(AUTH_GENERATE_SIGN_IN_LINK,
                "no-session", userSession.getEmail());
        final OneTimeSignInToken tokenAndUid = authDao.findUidByEmail(
                userSession.getEmail()).get();
        assertEquals(userSession.getUid(), tokenAndUid.getUid());

        myRest().voidPost(AUTH_GENERATE_SIGN_IN_LINK,
                "no-session", userSession.getEmail());

        assertEquals(tokenAndUid.getToken(), authDao.findUidByEmail(
                userSession.getEmail()).get().getToken());

        final Authenticated authenticated = myRest().get(AUTH_BY_ONE_TIME_TOKEN
                + tokenAndUid.getToken().get()
                + "/" + userSession.getEmail(), Authenticated.class);
        assertEquals(User, authenticated.getType());
        assertEquals(userSession.getUid(), authenticated.getUid());
        assertNotNull(authenticated.getSession());
        myRest().voidPost(AUTH_USER_CHECK_SESSION,
                authenticated.getSession(),
                "");

        try {
            myRest().get(AUTH_BY_ONE_TIME_TOKEN
                    + tokenAndUid.getToken().get()
                    + "/" + userSession.getEmail(), Authenticated.class);
            fail("expected non authorized");
        } catch (WebApplicationException e) {
            assertEquals(401, e.getResponse().getStatus());
        }
    }
}
