package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.app.auth.AuthCtx;
import org.dan.ping.pong.app.country.CountryCtx;
import org.dan.ping.pong.app.user.UserCtx;
import org.dan.ping.pong.mock.GeneratorCtxWithoutAdmin;
import org.dan.ping.pong.sys.ctx.jackson.JacksonContext;
import org.dan.ping.pong.sys.db.DbContext;
import org.springframework.context.annotation.Import;

@Import({PropertiesContext.class, TimeContext.class, DbContext.class,
        JacksonContext.class, AuthCtx.class, UserCtx.class, CountryCtx.class,
        JerseyCtx.class, GeneratorCtxWithoutAdmin.class})
public class BaseContextWithoutJersey {
}
