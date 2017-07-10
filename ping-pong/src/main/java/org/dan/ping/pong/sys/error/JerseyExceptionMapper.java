package org.dan.ping.pong.sys.error;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class JerseyExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException exception) {
        final String errorId = randomUUID().toString();
        log.error("Error: {}", errorId, exception);
        return Response.status(exception.getResponse().getStatus())
                .entity(new Error(errorId, exception.getMessage()))
                .type(APPLICATION_JSON)
                .build();
    }
}
