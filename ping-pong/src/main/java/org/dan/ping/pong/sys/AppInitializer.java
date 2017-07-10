package org.dan.ping.pong.sys;

import static java.util.Collections.singletonList;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.sys.ctx.AppContext;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Slf4j
@Order(1)
public class AppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        final WebApplicationContext webAppCtx = createWebAppCtx(
                singletonList(AppContext.class));
        servletContext.addListener(new ContextLoaderListener(webAppCtx));
        servletContext.setInitParameter("contextConfigLocation", "");
    }

    private WebApplicationContext createWebAppCtx(Iterable<Class<?>> configClasses) {
        final AnnotationConfigWebApplicationContext context
                = new AnnotationConfigWebApplicationContext();
        configClasses.forEach(context::register);
        return context;
    }
}
