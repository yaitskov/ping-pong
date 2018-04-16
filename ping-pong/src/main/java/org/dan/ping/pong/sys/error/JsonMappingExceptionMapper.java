package org.dan.ping.pong.sys.error;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class JsonMappingExceptionMapper
        implements ExceptionMapper<JsonMappingException> {
    @Override
    public Response toResponse(JsonMappingException exception) {
        final Error error = new Error(exception.getMessage());
        log.error("{}: [{}]; eid={}",
                exception.getClass().getName(), exception.getMessage(),
                error.getId(), exception);
        return Response.status(INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(APPLICATION_JSON)
                .build();
    }
}
