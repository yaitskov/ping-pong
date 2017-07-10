package org.dan.ping.pong.app.auth;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.dan.ping.pong.app.auth.CheckSysAdminSessionResource.ANONYMOUS_SYS_ADMIN_CHECK;
import static org.dan.ping.pong.app.auth.CheckUserSessionResource.AUTH_USER_CHECK_SESSION;
import static org.dan.ping.pong.app.auth.SysAdminSignInResource.ANONYMOUS_SYS_ADMIN_SIGN_IN;
import static org.dan.ping.pong.mock.DaoEntityGenerator.SYS_ADMIN_TEST_PASSWORD;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.sys.ctx.BaseTestContext;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
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
}
