/**
 *
 */
package org.ow2.proactive.scheduler.util.logforwarder.providers;

import java.net.URI;

import org.apache.log4j.Appender;
import org.apache.log4j.net.SocketAppender;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.scheduler.common.util.SimpleLoggerServer;
import org.ow2.proactive.scheduler.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.util.logforwarder.LogForwardingProvider;


/**
 * A socket-based implementation of LogForwardingProvider.
 */
public class SocketBasedForwardingProvider implements LogForwardingProvider {

    private SimpleLoggerServer sls;

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.util.logforwarder.LogForwardingProvider#createServer()
     */
    public URI createServer() {
        try {
            this.sls = SimpleLoggerServer.createLoggerServer();
            return new URI("raw", "//" + ProActiveInet.getInstance().getInetAddress().getHostName() + ":" +
                sls.getPort(), "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.util.logforwarder.LogForwardingProvider#destroyServer()
     */
    public void destroyServer() {
        this.sls.stop();
    }

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.util.logforwarder.LogForwardingProvider#createAppenderProvider(java.net.URI)
     */
    public AppenderProvider createAppenderProvider(URI serverURI) {
        System.out.println("SocketBasedForwardingProvider.createAppenderProvider() - host = " +
            serverURI.getHost() + " - port = " + serverURI.getPort());
        return new SocketAppenderProvider(serverURI.getHost(), serverURI.getPort());
    }

    /**
     * A simple container for a log4j SocketAppender.
     */
    public static class SocketAppenderProvider implements AppenderProvider {

        private String hostname;
        private int port;

        /**
         * Create a provider for SocketAppender.
         * @param hostname the target host.
         * @param port the port open on the target host.
         */
        SocketAppenderProvider(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
        }

        /* (non-Javadoc)
         * @see org.ow2.proactive.scheduler.util.logforwarder.AppenderProvider#getAppender()
         */
        public Appender getAppender() {
            return new SocketAppender(this.hostname, this.port);
        }

    }

}
