package org.dan.ping.pong.app.place;

import org.dan.ping.pong.app.auth.AuthCtx;
import org.springframework.context.annotation.Import;

@Import({PlaceDao.class, PlaceResource.class, AuthCtx.class})
public class PlaceCtx {
}
