package org.dan.ping.pong.test;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.MyRest;
import org.dan.ping.pong.sys.JerseyConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

public abstract class AbstractSpringJerseyTest extends JerseyTest {
    private static ThreadLocal<AnnotationConfigWebApplicationContext> contextThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<URI> baseUri = new ThreadLocal<>();

    @BeforeClass
    public static void initContext() {
        contextThreadLocal.set(new AnnotationConfigWebApplicationContext());
    }

    @AfterClass
    public static void closeContext() {
        contextThreadLocal.get().close();
    }

    @Before
    public void setBaseUri() {
        baseUri.set(getBaseUri());
    }

    public static URI provideBaseUri() {
        return baseUri.get();
    }

    @After
    public void dropBaseUri() {
        baseUri.remove();
    }

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = getResourceConfig();
        ContextConfiguration annotation = this.getClass().getAnnotation(ContextConfiguration.class);
        checkNotNull(annotation,
                "Please annotate class with @ContextConfiguration(classes={<spring config classes>})");
        Category category = this.getClass().getAnnotation(Category.class);
        checkArgument(category != null && asList(category.value()).contains(JerseySpringTest.class),
                "Please annotate class with @Category(JerseySpringTest.class)");
        AnnotationConfigWebApplicationContext appContext = contextThreadLocal.get();
        ActiveProfiles activeProfiles = this.getClass().getAnnotation(ActiveProfiles.class);
        if (activeProfiles != null) {
            appContext.getEnvironment().setActiveProfiles(activeProfiles.value());
        }
        appContext.register(annotation.classes());
        appContext.refresh();
        resourceConfig.property("contextConfig", appContext);
        appContext.getAutowireCapableBeanFactory().autowireBean(this);
        return resourceConfig;
    }

    protected ResourceConfig getResourceConfig() {
        return new JerseyConfig();
    }

    @Inject
    protected Client client;

    public WebTarget request() {
        return client.target(getBaseUri());
    }

    public MyRest myRest() {
        return new MyRest(client, getBaseUri());
    }
}
