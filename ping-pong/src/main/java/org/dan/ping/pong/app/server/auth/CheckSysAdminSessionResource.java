package org.dan.ping.pong.app.server.auth;

import static org.dan.ping.pong.app.server.auth.AuthCtx.SYS_ADMIN_SESSIONS;

import com.google.common.cache.Cache;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Slf4j
@Path(CheckSysAdminSessionResource.ANONYMOUS_SYS_ADMIN_CHECK + "{session}")
public class CheckSysAdminSessionResource {
    static final String ANONYMOUS_SYS_ADMIN_CHECK = "/anonymous/sys/admin/check/";

    @Inject
    @Named(SYS_ADMIN_SESSIONS)
    private Cache<String, String> sessionLogin;

    @GET
    public boolean check(@PathParam("session") String session) {
        final boolean valid = sessionLogin.getIfPresent(session) != null;
        log.info("Check sys admin session [{}]/{}", session, valid);
        return valid;
    }
}
