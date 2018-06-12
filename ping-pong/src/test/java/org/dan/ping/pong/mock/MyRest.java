package org.dan.ping.pong.mock;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import lombok.RequiredArgsConstructor;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

@RequiredArgsConstructor
public class MyRest {
    private final Client client;
    private final URI baseUri;

    public WebTarget request() {
        return client.target(baseUri);
    }

    public <T> void voidPost(String path, SessionAware userSession, T entity) {
        voidPost(path, userSession.getSession(), entity);
    }

    public <T> void voidPost(String path, String session, T entity) {
        assertThat(post(path, session, entity).getStatus(), lessThan(205));
    }

    public <T> void voidAnonymousPost(String path, T entity) {
        assertThat(post(path, "anonymous", entity).getStatus(), lessThan(205));
    }

    public <T> Response post(String path, SessionAware sessionAware, T entity) {
        return post(path, sessionAware.getSession(), entity);
    }

    public <T> Invocation.Builder postBuilder(String path, String session) {
        return request().path(path).request(APPLICATION_JSON)
                .header(SESSION, session);
    }

    public <T> Response post(String path, String session, T entity) {
        return postBuilder(path, session)
                .post(Entity.entity(entity, APPLICATION_JSON));
    }

    public <T> Response post(String path, T entity) {
        return request().path(path).request(APPLICATION_JSON)
                .post(Entity.entity(entity, APPLICATION_JSON));
    }

    public <T> T get(String path, SessionAware session, GenericType<T> gt) {
        return request().path(path).request(APPLICATION_JSON)
                .header(SESSION, session.getSession())
                .get(gt);
    }

    public <T> T get(String path, SessionAware session, Class<T> resultClass) {
        return request().path(path).request(APPLICATION_JSON)
                .header(SESSION, session.getSession())
                .get(resultClass);
    }

    public <T> T get(String path, Class<T> c) {
        return request().path(path).request(APPLICATION_JSON).get(c);
    }

    public <T> T get(String path, GenericType<T> c) {
        return request().path(path).request(APPLICATION_JSON).get(c);
    }
}
