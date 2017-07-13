package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.mock.AdminGeneratorCtx;
import org.dan.ping.pong.mock.MyLocalRestCtx;
import org.springframework.context.annotation.Import;

@Import({MyLocalRestCtx.class, AdminGeneratorCtx.class, BaseContextWithoutJersey.class})
public class BaseTestContext {
}
