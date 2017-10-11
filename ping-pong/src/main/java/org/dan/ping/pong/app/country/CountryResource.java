package org.dan.ping.pong.app.country;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.tournament.Uid;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class CountryResource {
    public static final String COUNTRY_ID = "countryId";
    public static final String COUNTRY_LIST = "/country/list";
    public static final String COUNTRY_CREATE = "/country/create";

    @Inject
    private AuthService authService;
    @Inject
    private CountryService countryService;

    @GET
    @Path(COUNTRY_LIST)
    @Consumes(APPLICATION_JSON)
    public List<CountryLink> list() {
        return countryService.list();
    }

    @POST
    @Path(COUNTRY_CREATE)
    @Produces(APPLICATION_JSON)
    public int create(
            @HeaderParam(SESSION) String session,
            NewCountry newCountry) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        return countryService.create(uid, newCountry);
    }
}
