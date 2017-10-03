package org.dan.ping.pong.app.server.country;

import org.springframework.context.annotation.Import;

@Import({CountryDao.class, CountryService.class, CountryResource.class})
public class CountryCtx {
}
