package org.dan.ping.pong.app.table;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.util.time.Clocker;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class TableResource {
    public static final String TABLE_SCHEDULE_FREE = "/table/schedule/free";
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
        return tableService.scheduleFreeTables(scheduleFreeTables.getTid(), clocker.get());
    }
}
