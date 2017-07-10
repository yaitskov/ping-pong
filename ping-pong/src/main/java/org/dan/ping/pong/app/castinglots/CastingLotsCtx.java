package org.dan.ping.pong.app.castinglots;

import org.springframework.context.annotation.Import;

@Import({CastingLotsDao.class, CastingLotsService.class, CastingLotsResource.class})
public class CastingLotsCtx {
}
