package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.app.auth.AuthCtx;
import org.dan.ping.pong.app.user.UserCtx;
import org.dan.ping.pong.mock.GeneratorCtx;
import org.dan.ping.pong.sys.ctx.jackson.JacksonContext;
import org.dan.ping.pong.sys.db.DbContext;
import org.springframework.context.annotation.Import;

@Import({PropertiesContext.class, TimeContext.class, DbContext.class,
        JacksonContext.class, AuthCtx.class, UserCtx.class,
        JerseyCtx.class, GeneratorCtx.class})
public class BaseContextWithoutJersey {
}
