package org.dan.ping.pong.sys.error;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class InvalidTypeIdExceptionMapper
        implements ExceptionMapper<InvalidTypeIdException> {
    @Override
    public Response toResponse(InvalidTypeIdException exception) {
        final Error error = new Error(exception.getMessage());
        log.error("Unknown subclass id: [{}] of interface {}; eid={}",
                exception.getTypeId(), exception.getBaseType(),
                error.getId(), exception);
        return Response.status(BAD_REQUEST)
                .entity(error)
                .type(APPLICATION_JSON)
                .build();
    }
}
