package org.ow2.proactive_grid_cloud_portal;

import java.io.File;

import javax.servlet.ServletContextEvent;

import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.ow2.proactive_grid_cloud_portal.exceptions.NotConnectedExceptionMapper;


public class MyResteasyBootstrap extends ResteasyBootstrap {

    public void contextInitialized(ServletContextEvent event) {

        super.contextInitialized(event);

        ResteasyProviderFactory dispatcher = ResteasyProviderFactory.getInstance();
        //        RuntimeDelegate.setInstance(dispatcher);
        //        RegisterBuiltin.register(dispatcher);

        //        ResteasyProviderFactory dispatcher = new ResteasyProviderFactory();

        //        ResteasyProviderFactory.getInstance().addContextResolver(provider)
        dispatcher.addStringConverter(RestartModeConverter.class);
        dispatcher.addStringConverter(IntWrapperConverter.class);
        dispatcher.registerProvider(PersistentMapConverter.class);
        dispatcher.registerProvider(NotConnectedExceptionMapper.class);
        dispatcher.registerProvider( JacksonProvider.class);

        try {
            PortalConfiguration.load(new File(event.getServletContext().getRealPath(
                    "WEB-INF/portal.properties")));
        } catch (Exception e) {
            throw new IllegalStateException("configuration file ('WEB-INF/portal.properties') not found",e);
        }
    }

}
