package org.ow2.proactive_grid_cloud_portal;

import javax.servlet.ServletContextEvent;
import javax.ws.rs.ext.RuntimeDelegate;

import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;

public class MyResteasyBootstrap extends ResteasyBootstrap {

    public void contextInitialized(ServletContextEvent event) {
        RuntimeDelegate.setInstance(new org.jboss.resteasy.spi.ResteasyProviderFactory());
        super.contextInitialized(event);
    }
    
    
}
