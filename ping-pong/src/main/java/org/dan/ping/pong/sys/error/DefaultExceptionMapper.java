package org.dan.ping.pong.sys.error;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        final Error error = new Error("Internal server error");
        log.error("Internal server error: {}", error.getId(), exception);
        return Response.status(INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(APPLICATION_JSON)
                .build();
    }
}
