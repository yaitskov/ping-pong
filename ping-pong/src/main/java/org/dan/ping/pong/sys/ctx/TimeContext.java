package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.util.time.NowClocker;
import org.springframework.context.annotation.Import;

@Import(NowClocker.class)
public class TimeContext {}
