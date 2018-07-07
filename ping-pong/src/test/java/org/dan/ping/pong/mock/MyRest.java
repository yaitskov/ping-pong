package org.dan.ping.pong.mock;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProvider;
import org.dan.ping.pong.sys.error.Error;
import org.dan.ping.pong.sys.error.PiPoEx;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

@RequiredArgsConstructor
public class MyRest {
    private static final ObjectMapper om = ObjectMapperProvider.get();
    private final Client client;
    private final URI baseUri;

    public WebTarget request() {
        return client.target(baseUri);
    }

    public <T> void voidPost(String path, SessionAware userSession, T entity) {
        voidPost(path, userSession.getSession(), entity);
    }

    public <T> void voidPost(String path, String session, T entity) {
        post(path, () -> session, entity, String.class);
    }

    public <T> void voidAnonymousPost(String path, T entity) {
        assertThat(post(path, "anonymous", entity).getStatus(), lessThan(205));
    }

    public <T> Response post(String path, SessionAware sessionAware, T entity) {
        return post(path, sessionAware.getSession(), entity);
    }

    @SneakyThrows
    public <T, R> R post(String path, SessionAware sessionAware, T entity, Class<R> respClass) {
        final Response response = post(path, sessionAware.getSession(), entity);
        switch (response.getStatus()) {
            case 200:
            case 201:
                return response.readEntity(respClass);
            case 400:
                throw new PiPoEx(400, response.readEntity(Error.class), null);
            default:
                throw new PiPoEx(response.getStatus(), new Error("post req ["
                        + path + "] with ["
                        + om.writeValueAsString(entity)
                        + "] responded [" + response.getStatus() + "] ["
                        + IOUtils.toString((InputStream) response.getEntity(), UTF_8)), null);
        }
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
