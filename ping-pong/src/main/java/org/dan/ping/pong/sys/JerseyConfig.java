
package org.dan.ping.pong.sys;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.glassfish.jersey.server.ServerProperties.BV_SEND_ERROR_IN_RESPONSE;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.dan.ping.pong.app.auth.AuthResource;
import org.dan.ping.pong.app.auth.SysAdminSignInResource;
import org.dan.ping.pong.app.bid.BidResource;
import org.dan.ping.pong.app.castinglots.CastingLotsResource;
import org.dan.ping.pong.app.category.CategoryResource;
import org.dan.ping.pong.app.city.CityResource;
import org.dan.ping.pong.app.country.CountryResource;
import org.dan.ping.pong.app.group.GroupResource;
import org.dan.ping.pong.app.match.MatchResource;
import org.dan.ping.pong.app.match.dispute.MatchDisputeResource;
import org.dan.ping.pong.app.place.PlaceResource;
import org.dan.ping.pong.app.table.TableResource;
import org.dan.ping.pong.app.tournament.TournamentResource;
import org.dan.ping.pong.app.user.UserResource;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperContextResolver;
import org.dan.ping.pong.sys.error.DefaultExceptionMapper;
import org.dan.ping.pong.sys.error.JerseyExceptionMapper;
import org.dan.ping.pong.sys.error.JerseyValidationExceptionMapper;
import org.dan.ping.pong.sys.error.JooqExceptionMapper;
import org.dan.ping.pong.sys.error.PiPoExMapper;
import org.dan.ping.pong.sys.error.UncheckedExecutionExceptionMapper;
import org.dan.ping.pong.sys.error.UnrecognizedPropertyExceptionMapper;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        property(BV_SEND_ERROR_IN_RESPONSE, true);
        register(new LoggingFilter());
        register(ObjectMapperContextResolver.class);
        register(JacksonJaxbJsonProvider.class);
        register(new PiPoExMapper());
        register(new JerseyExceptionMapper()); // just class get exception
        register(new JerseyValidationExceptionMapper());
        register(new DefaultExceptionMapper());
        register(UnrecognizedPropertyExceptionMapper.class);
        register(JooqExceptionMapper.class);
        register(UncheckedExecutionExceptionMapper.class);
        packages(false,
                asList(UserResource.class, SysAdminSignInResource.class,
                        PlaceResource.class, TournamentResource.class,
                        GroupResource.class, MatchDisputeResource.class,
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
