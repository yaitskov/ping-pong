package org.dan.ping.pong.sys.ctx.jackson;

import org.springframework.context.annotation.Import;

@Import({ObjectMapperProvider.class, ObjectMapperContextResolver.class})
public class JacksonContext {}
