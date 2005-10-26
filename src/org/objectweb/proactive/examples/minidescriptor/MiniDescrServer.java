package org.objectweb.proactive.examples.minidescriptor;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 *
 *
 * @author Jerome+Sylvain
 */
public class MiniDescrServer {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    MiniDescrActive minidesca = null;

    public MiniDescrServer(String location) {
        VirtualNode virtualnode = null;

        ProActiveDescriptor pad = null;
        logger.info("-+-+-+-+-+-+-+- MiniDescrServer launched -+-+-+-+-+-+-+-");

        try {
            pad = ProActive.getProactiveDescriptor(location);
            virtualnode = pad.getVirtualNode("MiniVNServer");
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
        virtualnode.activate();
    }

    public static void main(String[] args) throws IOException {
        new MiniDescrServer("descriptors/examples/minidescriptor_server.xml");
    }
}
