/**
 *
 */
package org.ow2.proactive.scheduler.util.logforwarder;

import java.net.URI;

import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * This class provides server and appenders for log forwarding.
 * @see LogForwardingProvider
 */
public final class LogForwardingService {

    // connection to the server created by initialize()
    private URI serverConnection = null;
    // can be initialized only once
    private boolean initialized = false;

    // the protocol specific provider
    private LogForwardingProvider provider;

    /**
     * Create the server side
     */
    public final void initialize() {
        try {
            if (!initialized) {
                // load the provider
                String providerClassname = PASchedulerProperties.LOGS_FORWARDING_PROVIDER.getValueAsString();
                if (providerClassname == null || providerClassname == "") {
                    // TODO ?
                } else {
                    Class<? extends LogForwardingProvider> providerClass = (Class<? extends LogForwardingProvider>) Class
                            .forName(providerClassname);
                    this.provider = providerClass.newInstance();
                    this.serverConnection = provider.createServer();
                    this.initialized = true;
                }
            } else {
                throw new IllegalStateException("The service has already been initialized.");
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassCastException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Create an appender to the server
     * @throws
     * @return
     */
    public final AppenderProvider getAppenderProvider() {
        if (initialized) {
            System.out.println("LogForwardingService.getAppenderProvider() : " + this.serverConnection);
            return provider.createAppenderProvider(this.serverConnection);
        } else {
            throw new IllegalStateException(
                "The service has not been initialized. Cannot create appender provider.");
        }
    }

}
