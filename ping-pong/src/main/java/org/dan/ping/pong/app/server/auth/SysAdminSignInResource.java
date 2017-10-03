package org.dan.ping.pong.app.server.auth;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
@Path(SysAdminSignInResource.ANONYMOUS_SYS_ADMIN_SIGN_IN)
public class SysAdminSignInResource {
    static final String ANONYMOUS_SYS_ADMIN_SIGN_IN = "/anonymous/sys/admin/sign-in";

    @Inject
    private AuthService authService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(SysAdminAuth authReq) {
        log.info("Sys admin [{}] sign in attempt", authReq.getLogin());
        return authService.authSysAdmin(authReq)
                .map(token -> {
                    log.info("Sys admin [{}] signed in successfully", authReq.getLogin());
                    return Response.ok().entity(token).build();
                })
                .orElseGet(() -> {
                    log.info("Sys admin [{}] failed to sign in", authReq.getLogin());
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                });
    }
}
