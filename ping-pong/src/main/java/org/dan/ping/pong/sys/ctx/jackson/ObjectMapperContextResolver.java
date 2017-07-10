package org.dan.ping.pong.sys.ctx.jackson;


import static org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProvider.OBJECT_MAPPER;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ext.ContextResolver;

public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
    @Inject
    @Named(OBJECT_MAPPER)
    private ObjectMapper objectMapper;

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
