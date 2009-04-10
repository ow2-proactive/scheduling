/**
 *
 */
package org.ow2.proactive.scheduler.util.logforwarder.providers;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Appender;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.log.remote.ProActiveAppender;
import org.objectweb.proactive.core.util.log.remote.ProActiveLogCollector;
import org.objectweb.proactive.core.util.log.remote.ProActiveLogCollectorDeployer;
import org.objectweb.proactive.core.util.log.remote.ThrottlingProvider;
import org.ow2.proactive.scheduler.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.util.logforwarder.LogForwardingProvider;

import sun.rmi.runtime.NewThreadAction;


/**
 * @author cdelbe
 *
 */
public class ProActiveBasedForwardingProvider implements LogForwardingProvider {

    private ProActiveLogCollector collector;

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.util.logforwarder.LogForwardingProvider#createAppenderProvider(java.net.URI)
     */
    public AppenderProvider createAppenderProvider(URI serverURI) {
        return new ProActiveAppenderProvider(this.collector);
    }

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.util.logforwarder.LogForwardingProvider#createServer()
     */
    public URI createServer() {
        try {
            ProActiveLogCollectorDeployer plcd = new ProActiveLogCollectorDeployer("scheduler_collector");
            this.collector = (ProActiveLogCollector) (PARemoteObject.lookup(new URI(plcd.getCollectorURL())));
            System.out.println("ProActiveBasedForwardingProvider.createServer() : " + plcd.getCollectorURL());
            return new URI(plcd.getCollectorURL());
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.util.logforwarder.LogForwardingProvider#destroyServer()
     */
    public void destroyServer() {
        // TODO Auto-generated method stub
        // CMATHIEU ????????

    }

    public static class ProActiveAppenderProvider implements AppenderProvider {

        private ProActiveLogCollector remoteCollector;

        ProActiveAppenderProvider(ProActiveLogCollector remoteCollector) {
            this.remoteCollector = remoteCollector;
        }

        public Appender getAppender() {
            return new ProActiveAppender(new ThrottlingProvider(), this.remoteCollector);
        }

    }

}
