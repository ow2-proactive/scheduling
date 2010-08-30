package org.ow2.proactive_grid_cloud_portal;

import javax.servlet.ServletContextEvent;
import javax.ws.rs.ext.RuntimeDelegate;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.spi.ResteasyProviderFactory;


public class MyResteasyBootstrap extends ResteasyBootstrap {

    public void contextInitialized(ServletContextEvent event) {
        ResteasyProviderFactory dispatcher = new ResteasyProviderFactory();
        RuntimeDelegate.setInstance(dispatcher);

        //        ResteasyProviderFactory.getInstance().addContextResolver(provider)
        dispatcher.addStringConverter(RestartModeConverter.class);

        super.contextInitialized(event);
    }

}
