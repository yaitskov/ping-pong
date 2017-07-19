package org.dan.ping.pong.sys.error;

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
        final Error error = new Error(exception.getMessage());
        log.error("Error: id {} => msg [{}]",
                error.getId(), error.getMessage(), exception);
        return Response.status(exception.getResponse().getStatus())
                .entity(error)
                .type(APPLICATION_JSON)
                .build();
    }
}
