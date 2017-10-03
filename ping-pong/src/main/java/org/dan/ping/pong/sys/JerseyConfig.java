
package org.dan.ping.pong.sys;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import org.dan.ping.pong.app.server.auth.AuthResource;
import org.dan.ping.pong.app.server.auth.SysAdminSignInResource;
import org.dan.ping.pong.app.server.bid.BidResource;
import org.dan.ping.pong.app.server.castinglots.CastingLotsResource;
import org.dan.ping.pong.app.server.category.CategoryResource;
import org.dan.ping.pong.app.server.city.CityResource;
import org.dan.ping.pong.app.server.country.CountryResource;
import org.dan.ping.pong.sys.error.JerseyExceptionMapper;
import org.dan.ping.pong.sys.error.JooqExceptionMapper;
import org.dan.ping.pong.sys.error.PiPoExMapper;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperContextResolver;
import org.dan.ping.pong.sys.error.DefaultExceptionMapper;
import org.dan.ping.pong.app.server.match.MatchResource;
import org.dan.ping.pong.app.server.place.PlaceResource;
import org.dan.ping.pong.app.server.table.TableResource;
import org.dan.ping.pong.app.server.tournament.TournamentResource;
import org.dan.ping.pong.app.server.user.UserResource;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ext.ExceptionMapper;

@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(new LoggingFilter());
        register(ObjectMapperContextResolver.class);
        register(new JacksonFeature());
        register(new PiPoExMapper());
        register(new JerseyExceptionMapper());
        final ExceptionMapper defaultMapper = new DefaultExceptionMapper();
        register(new JooqExceptionMapper(defaultMapper));
        register(defaultMapper);
        packages(false,
                asList(UserResource.class, SysAdminSignInResource.class,
                        PlaceResource.class, TournamentResource.class,
                        BidResource.class, CategoryResource.class,
                        MatchResource.class, AuthResource.class,
                        CountryResource.class, CityResource.class,
                        TableResource.class, CastingLotsResource.class)
                        .stream()
                        .map(Class::getPackage)
                        .map(Package::getName)
                        .collect(toList())
                        .toArray(new String[0]));
    }
}
