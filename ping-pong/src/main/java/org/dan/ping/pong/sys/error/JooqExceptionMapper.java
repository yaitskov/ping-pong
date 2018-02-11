package org.dan.ping.pong.sys.error;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;

import java.sql.SQLIntegrityConstraintViolationException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class JooqExceptionMapper implements ExceptionMapper<DataAccessException> {
    @Inject
    private DefaultExceptionMapper forward;

    @Override
    public Response toResponse(DataAccessException exception) {
        if (exception.getCause() instanceof SQLIntegrityConstraintViolationException) {
            if (exception.getCause().getMessage().contains("Duplicate entry")) {
                Error error = new Error("entity is already exist");
                log.error("Duplicate entity eid={}", error.getId(), exception);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(error)
                        .type(APPLICATION_JSON)
                        .build();
            }
        }
        return forward.toResponse(exception);
    }
}
