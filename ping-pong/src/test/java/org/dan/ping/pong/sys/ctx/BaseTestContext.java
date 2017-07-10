package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.app.auth.AuthCtx;
import org.dan.ping.pong.sys.ctx.jackson.JacksonContext;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperContextResolver;
import org.dan.ping.pong.sys.db.DbContext;
import org.dan.ping.pong.mock.GeneratorCtx;
import org.dan.ping.pong.app.user.UserCtx;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@Import({PropertiesContext.class, TimeContext.class, DbContext.class,
        JacksonContext.class, AuthCtx.class, UserCtx.class,
        GeneratorCtx.class})
public class BaseTestContext {
    @Bean
    public Client client(ObjectMapperContextResolver resolver) {
        return ClientBuilder.newBuilder()
                .register(resolver)
                .register(JacksonFeature.class)
                .build();
    }
}
