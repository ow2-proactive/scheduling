package org.ow2.proactive_grid_cloud_portal;

import java.io.File;

import javax.servlet.ServletContextEvent;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive_grid_cloud_portal.exceptions.NotConnectedExceptionMapper;


public class MyResteasyBootstrap extends ResteasyBootstrap {

    private Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.PREFIX + ".rest.webapps");


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
        dispatcher.addStringConverter(UpdatablePropertiesConverter.class);
        
        try {
            PortalConfiguration.load(new File(event.getServletContext().getRealPath(
                    "WEB-INF/portal.properties")));
        } catch (Exception e) {
            throw new IllegalStateException("configuration file ('WEB-INF/portal.properties') not found",e);
        }

        PropertyConfigurator.configure(event.getServletContext().getRealPath("WEB-INF/log4j.properties"));


    }


    @Override
    public void contextDestroyed(ServletContextEvent event) {

        System.out.println("shutting down ProActive Rest API at " + event.getServletContext().getContextPath());

        // happily terminate sessions

        String[] sessionids = SchedulerSessionMapper.getInstance().getSessionsMap().keySet().toArray(new String[] {});
        int i = 0;
        for (; i < sessionids.length ; i++ ) {
            Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionids[i]);
            try{
                s.disconnect();
            } catch (NotConnectedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (PermissionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                SchedulerSessionMapper.getInstance().getSessionsMap().remove(sessionids[i]);
                PAActiveObject.terminateActiveObject(s, true);
                System.out.println("sessionid " + sessionids[i] + "terminated");
            }
        }

        // force the shutdown of the runtime
        ProActiveRuntimeImpl.getProActiveRuntime().cleanJvmFromPA();

        super.contextDestroyed(event);
        System.out.println("ProActive Rest API shutdown sequence completed");

    }
}
