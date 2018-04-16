package org.dan.ping.pong.sys.error;

import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.spi.ExceptionMappers;

import java.lang.reflect.UndeclaredThrowableException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

@Slf4j
public class UndeclaredThrowableExecutionExceptionMapper
        implements ExceptionMapper<UndeclaredThrowableException> {
    @Inject
    private Provider<ExceptionMappers> mappersProvider;

    @Override
    public Response toResponse(UndeclaredThrowableException exception) {
        log.error("Wrapper exception UndeclaredThrowableExecutionExceptionMapper");
        return mappersProvider.get()
                .findMapping(exception.getCause())
                .toResponse(exception.getCause());
    }
}
