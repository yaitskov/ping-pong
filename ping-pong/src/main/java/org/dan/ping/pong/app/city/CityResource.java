package org.dan.ping.pong.app.city;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.country.CountryResource.COUNTRY_ID;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.bid.Uid;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class CityResource {
    public static final String CITY_LIST = "/city/list/";
    public static final String CITY_CREATE = "/city/create";

    @Inject
    private AuthService authService;
    @Inject
    private CityService cityService;

    @GET
    @Path(CITY_LIST + "{" + COUNTRY_ID + "}")
    @Consumes(APPLICATION_JSON)
    public List<CityLink> listByCountry(
            @PathParam(COUNTRY_ID) int countryId) {
        return cityService.listByCountry(countryId);
    }

    @POST
    @Path(CITY_CREATE)
    @Produces(APPLICATION_JSON)
    public int create(
            @HeaderParam(SESSION) String session,
            NewCity newCity) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        return cityService.create(uid, newCity);
    }
}
