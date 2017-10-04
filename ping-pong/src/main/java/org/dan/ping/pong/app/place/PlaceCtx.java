package org.dan.ping.pong.app.place;

import org.dan.ping.pong.app.auth.AuthCtx;
import org.springframework.context.annotation.Import;

@Import({PlaceDaoServer.class, PlaceService.class, PlaceCacheFactory.class,
        PlaceAccessor.class,
        PlaceCacheLoader.class, PlaceResource.class, AuthCtx.class})
public class PlaceCtx {
}
