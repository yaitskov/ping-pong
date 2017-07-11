
package org.dan.ping.pong.sys;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import org.dan.ping.pong.app.auth.AuthResource;
import org.dan.ping.pong.app.auth.SysAdminSignInResource;
import org.dan.ping.pong.app.bid.BidResource;
import org.dan.ping.pong.app.castinglots.CastingLotsResource;
import org.dan.ping.pong.app.category.CategoryResource;
import org.dan.ping.pong.sys.error.JerseyExceptionMapper;
import org.dan.ping.pong.sys.error.PiPoExMapper;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperContextResolver;
import org.dan.ping.pong.sys.error.DefaultExceptionMapper;
import org.dan.ping.pong.app.match.MatchResource;
import org.dan.ping.pong.app.place.PlaceResource;
import org.dan.ping.pong.app.table.TableResource;
import org.dan.ping.pong.app.tournament.TournamentResource;
import org.dan.ping.pong.app.user.UserResource;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(new LoggingFilter());
        register(ObjectMapperContextResolver.class);
        register(new JacksonFeature());
        register(new PiPoExMapper());
        register(new JerseyExceptionMapper());
        register(new DefaultExceptionMapper());
        packages(false,
                asList(UserResource.class, SysAdminSignInResource.class,
                        PlaceResource.class, TournamentResource.class,
                        BidResource.class, CategoryResource.class,
                        MatchResource.class, AuthResource.class,
                        TableResource.class, CastingLotsResource.class)
                        .stream()
                        .map(Class::getPackage)
                        .map(Package::getName)
                        .collect(toList())
                        .toArray(new String[0]));
    }
}
