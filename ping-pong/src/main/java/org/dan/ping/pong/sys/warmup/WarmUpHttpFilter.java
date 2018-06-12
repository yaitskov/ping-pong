package org.dan.ping.pong.sys.warmup;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.util.Integers.parseInteger;
import static org.dan.ping.pong.util.Integers.parseLongInt;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

@Slf4j
public class WarmUpHttpFilter implements ContainerResponseFilter {
    public static final String CS_WARM_UP_ID = "cs-warm-up-id";
    public static final String CS_CLIENT_STARTED = "cs-client-started";

    @Inject
    private WarmUpService warmUpService;

    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) {
        ofNullable(requestContext.getHeaderString(CS_WARM_UP_ID))
                .filter(s -> !s.isEmpty())
                .map(s -> parseInteger(s,
                        () -> "[" + CS_WARM_UP_ID
                                + "] header is not number but [ "
                                + s + "]"))
                .ifPresent(wmId ->
                        ofNullable(requestContext.getHeaderString(CS_CLIENT_STARTED))
                                .filter(s -> !s.isEmpty())
                                .map(s -> parseLongInt(s, () -> "[" + CS_CLIENT_STARTED
                                        + "] header is not number but [ "
                                        + s + "]"))
                                .map(Instant::ofEpochMilli)
                                .ifPresent(clientStarted -> warmUpService.logDuration(
                                        wmId, clientStarted)));
    }
}
