package org.dan.ping.pong.app.auth;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class HelloResource {
    @GET
    @Path("/hello")
    public String hello() {
        return "hello";
    }
}
