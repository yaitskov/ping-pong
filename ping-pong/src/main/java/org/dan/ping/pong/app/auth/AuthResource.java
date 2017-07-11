package org.dan.ping.pong.app.auth;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class AuthResource {
    public static final String AUTH_BY_ONE_TIME_TOKEN = "/anonymous/auth/by-one-time-token/";
    public static final String AUTH_GENERATE_SIGN_IN_LINK = "/anonymous/auth/generate/sign-in-link";

    @Inject
    private AuthService authService;

    @GET
    @Path(AUTH_BY_ONE_TIME_TOKEN + "{oneTimeToken}/{email}")
    public Authenticated checkUserSession(
            @PathParam("oneTimeToken") String oneTimeToken,
            @PathParam("email") String email) {
        return authService.authByOneTimeSession(oneTimeToken, email);
    }

    @POST
    @Path(AUTH_GENERATE_SIGN_IN_LINK)
    public void generateSignInLink(String email) {
        log.info("Generate sign-in link for {}", email);
        authService.generateSignInLink(email);
    }
}
