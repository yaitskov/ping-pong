package org.dan.ping.pong.app.server.auth;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
    public static final String DEV_CLEAN_SIGN_IN_TOKEN_TABLE = "/dev/clean-sign-in-token-table";

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

    @GET
    @Path("/dev/emails-with-sign-in-token")
    public List<EmailAndToken> emailsWithOneTimeSignInToken()  {
        log.info("dev-feature - load sign in tokens");
        return authService.emailsWithOneTimeSignInToken();
    }

    @POST
    @Path(DEV_CLEAN_SIGN_IN_TOKEN_TABLE)
    public void cleanSignInTokenTable()  {
        log.info("dev-feature - clean sign in token table");
        authService.cleanSignInTokenTable();
    }
}
