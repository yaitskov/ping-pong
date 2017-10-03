package org.dan.ping.pong.app.server.city;

import org.springframework.context.annotation.Import;

@Import({CityDao.class, CityService.class, CityResource.class})
public class CityCtx {
}
