package org.dan.ping.pong.app.place;


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import org.dan.ping.pong.app.auth.AuthService;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/")
public class PlaceResource {
    static final String PLACE_CREATE = "/place/create";
    static final String PLACES = "/places";
    public static final String PLACE_INFO = "/place/";

    @Inject
    private PlaceDao placeDao;

    @Inject
    private AuthService authService;

    @POST
    @Path(PLACE_CREATE)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public int create(@HeaderParam(SESSION) String session, CreatePlace newPlace) {
        return placeDao.createAndGrant(authService.userInfoBySession(session).getUid(),
                newPlace.getName(), newPlace.getAddress());
    }

    @GET
    @Path(PLACES)
    @Produces(APPLICATION_JSON)
    public List<PlaceLink> findEditable(@HeaderParam(SESSION) String session) {
        return placeDao.findEditableByUid(authService.userInfoBySession(session).getUid());
    }

    @GET
    @Path(PLACE_INFO + "{pid}")
    @Produces(APPLICATION_JSON)
    public PlaceInfoCountTables getPlaceById(@PathParam("pid") int pid) {
        return placeDao.getPlaceById(pid)
                .orElseThrow(() -> notFound("Place " + pid + " doesn't exist"));
    }

    @POST
    @Path("/place/update")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public void update(@HeaderParam(SESSION) String session, PlaceLink place) {
        final int uid = authService.userInfoBySession(session).getUid();
        placeDao.update(uid, place);
    }
}
