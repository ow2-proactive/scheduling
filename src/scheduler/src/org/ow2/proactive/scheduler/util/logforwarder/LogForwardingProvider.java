package org.ow2.proactive.scheduler.util.logforwarder;

import java.net.URI;


/**
 * This interface should be implemented by all log forwarding methods.
 * @see LogForwardingService
 */
public interface LogForwardingProvider {

    /**
     * Create an appender provider (i.e. a container for appender). This provider is serializable and can be remotely sent.
     * Note that a specific AppenderProvider implementation must be provided with a specific LogForwardingProvider implementation.
     * @param serverURI the URI of the server that receives logging events appended in the contained appender.
     * @return a container for an appender.
     */
    public AppenderProvider createAppenderProvider(URI serverURI);

    /**
     * Create locally a server that is able to receive logging events from the appender created by createAppenderProvider.
     * @return the URI to reach this server.
     */
    public URI createServer();

    /**
     * Terminate the server created by createServer().
     */
    public void destroyServer();

}
