package org.objectweb.proactive.examples.descriptor;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;

/**
 *
 *
 * @author Jerome+Sylvain
 */
public class MiniDescrServer {
    static Logger logger = Logger.getLogger(MiniDescrServer.class);
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
    	if (args.length < 1)
    		new MiniDescrServer(MiniDescrServer.class.getResource("minidescriptor_server.xml").getPath());
    	else
    		new MiniDescrServer(args[0]);
    }
}
