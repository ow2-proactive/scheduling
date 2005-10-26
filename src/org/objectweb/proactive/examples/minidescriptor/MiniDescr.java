package org.objectweb.proactive.examples.minidescriptor;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 *
 *
 * @author Jerome+Sylvain
 */
public class MiniDescr {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    MiniDescrActive minidesca = null;

    public MiniDescr(String location) {
        VirtualNode virtualnode = null;

        ProActiveDescriptor pad = null;
        logger.info("-+-+-+-+-+-+-+- MiniDescr launched -+-+-+-+-+-+-+-");

        try {
            pad = ProActive.getProactiveDescriptor(location);
            virtualnode = pad.getVirtualNode("MiniVN");
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
        virtualnode.activate();

        try {
            Node[] nodes = virtualnode.getNodes();
            Object[] param = null;

            for (int i = 0; i < nodes.length; i++) {
                MiniDescrActive desc = (MiniDescrActive) ProActive.newActive(MiniDescrActive.class.getName(),
                        param, nodes[i]);
                Message msg = desc.getComputerInfo();
                logger.info("-+-+-+-+-+-+-+- " + msg + " -+-+-+-+-+-+-+-");
            }
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        virtualnode.killAll(false);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("mini descriptor example");
        new MiniDescr("descriptors/examples/minidescriptor.xml");

        System.exit(0);
    }
}
