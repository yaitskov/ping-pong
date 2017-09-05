package org.dan.ping.pong.app.table;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.util.time.Clocker;

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
public class TableResource {
    public static final String TABLE_SCHEDULE_FREE = "/table/schedule/free";
    private static final String PLACE_ID = "placeId";
    @Inject
    private AuthService authService;
    @Inject
    private TableService tableService;
    @Inject
    private Clocker clocker;

    @POST
    @Path(TABLE_SCHEDULE_FREE)
    @Consumes(APPLICATION_JSON)
    public int scheduleFreeTables(@HeaderParam(SESSION) String session,
            ScheduleFreeTables scheduleFreeTables) {
        final int uid = authService.userInfoBySession(session).getUid();
        log.info("Admin {} uid schedules free table in {}", uid, scheduleFreeTables);
        return tableService.scheduleFreeTables(place, scheduleFreeTables.getTid(), clocker.get(), batch);
    }

    @GET
    @Path("/tables/by-place/{" + PLACE_ID + "}")
    @Produces(APPLICATION_JSON)
    public List<TableStatedLink> tablesByPlaceId(
            @PathParam(PLACE_ID) int placeId) {
        return tableService.findByPlaceId(placeId);
    }

    @POST
    @Path("/table/state")
    @Consumes(APPLICATION_JSON)
    public void setState(@HeaderParam(SESSION) String session, SetTableState update) {
        final int uid = authService.userInfoBySession(session).getUid();
        log.info("Admin {} uid set table {}", uid, update);
        tableService.setStatus(update);
    }

    @POST
    @Path("/table/create")
    @Consumes(APPLICATION_JSON)
    public void addTables(@HeaderParam(SESSION) String session, CreateTables create) {
        final int uid = authService.userInfoBySession(session).getUid();
        log.info("Admin {} uid add tables {}", uid, create);
        tableService.create(create);
    }
}
