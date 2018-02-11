package org.dan.ping.pong.sys.error;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class UnrecognizedPropertyExceptionMapper
        implements ExceptionMapper<UnrecognizedPropertyException> {
    @Override
    public Response toResponse(UnrecognizedPropertyException exception) {
        final Error error = new Error(exception.getMessage());
        log.error("Unrecognized property: [{}]; eid={}",
                exception.getPropertyName(), error.getId(), exception);
        return Response.status(BAD_REQUEST)
                .entity(error)
                .type(APPLICATION_JSON)
                .build();
    }
}
