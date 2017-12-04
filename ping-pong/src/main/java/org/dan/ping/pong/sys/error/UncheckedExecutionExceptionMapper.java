package org.dan.ping.pong.sys.error;

import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.spi.ExceptionMappers;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

@Slf4j
public class UncheckedExecutionExceptionMapper
        implements ExceptionMapper<UncheckedExecutionException> {
    @Inject
    private Provider<ExceptionMappers> mappersProvider;

    @Override
    public Response toResponse(UncheckedExecutionException exception) {
        log.error("Wrapper exception UncheckedExecutionExceptionMapper");
        return mappersProvider.get()
                .findMapping(exception.getCause())
                .toResponse(exception.getCause());
    }
}
