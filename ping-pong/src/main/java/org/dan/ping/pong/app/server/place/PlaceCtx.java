package org.dan.ping.pong.app.server.place;

import org.dan.ping.pong.app.server.auth.AuthCtx;
import org.springframework.context.annotation.Import;

@Import({PlaceDao.class, PlaceService.class, PlaceCacheFactory.class,
        PlaceAccessor.class,
        PlaceCacheLoader.class, PlaceResource.class, AuthCtx.class})
public class PlaceCtx {
}
