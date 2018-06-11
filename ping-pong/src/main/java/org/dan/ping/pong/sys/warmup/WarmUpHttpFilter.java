package org.dan.ping.pong.sys.warmup;

import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

@Slf4j
public class WarmUpHttpFilter implements ContainerRequestFilter {
    public static final String CS_WARM_UP_ID = "cs-warm-up-id";

    @Inject
    private WarmUpService warmUpService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ofNullable(requestContext.getHeaderString(CS_WARM_UP_ID))
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return parseInt(s);
                    } catch (NumberFormatException e) {
                        throw badRequest("[" + CS_WARM_UP_ID
                                + "] header is not number but [ "
                                + s + "]", e);
                    }
                })
                .ifPresent(warmUpService::logDuration);
    }
}
