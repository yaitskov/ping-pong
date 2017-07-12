package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.mock.MyLocalRestCtx;
import org.springframework.context.annotation.Import;

@Import({MyLocalRestCtx.class, BaseContextWithoutJersey.class})
public class BaseTestContext {
}
