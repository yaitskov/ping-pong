package org.dan.ping.pong.sys.error;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.list;
import static javax.ws.rs.core.Response.status;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

@Slf4j
public class PiPoExMapper implements ExceptionMapper<PiPoEx> {
    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(PiPoEx e) {
        if (request == null) {
            log.error("Background exception [{}], status: [{}], eid {}",
                    e.getClientMessage(), e.getStatus(),
                    e.getClientMessage().getId(), e);
        } else {
            Map<String, String> headers = newHashMap();
            list(request.getHeaderNames())
                    .forEach(name -> headers.put(name, request.getHeader(name)));
            log.error("Url exception [{}], eid {}, query: [{}], headers: [{}], status: [{}], message: [{}]",
                    request.getRequestURI(),
                    e.getClientMessage().getId(),
                    request.getQueryString(),
                    headers,
                    e.getStatus(),
                    e.getClientMessage(),
                    e);
        }
        return status(e.getStatus()).entity(e.getClientMessage()).build();
    }
}
