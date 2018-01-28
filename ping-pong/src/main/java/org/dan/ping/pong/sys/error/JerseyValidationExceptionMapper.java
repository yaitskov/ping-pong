package org.dan.ping.pong.sys.error;


import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class JerseyValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        final Error error = new Error(exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(joining(";\n")));
        log.error("Validation error: id {} => msg [{}]",
                error.getId(), error.getMessage(), exception);
        return Response.status(BAD_REQUEST)
                .entity(error)
                .type(APPLICATION_JSON)
                .build();
    }
}
