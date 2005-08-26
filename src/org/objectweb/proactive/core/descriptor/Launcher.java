package org.objectweb.proactive.core.descriptor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.MainDefinition;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 *
 * @author Terence FERUT - ProActive team
 * August 2005
 *
 * this class provides the stuff useful to launch an
 * application directly from a proactive descriptor XML file.
 *
 * be careful, your xml file must define a mainDefinition tag
 * that contains the main class path, otherwise the application will
 * not be launched.
 *
 */
public class Launcher {
    private ProActiveDescriptor pad;

    /**
     * Conctructor for a launcher
     * parse a xml pad url
     *
     * @param fileDescriptorPath the file path of the xml descriptor
     * @throws IOException
     * @throws ProActiveException
     * @throws NodeException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Launcher(String fileDescriptorPath)
        throws ProActiveException, NodeException {
        // replace spaces by %20 char (hexadecimal space code) to avoid bug with the property
        String filePathWithoutSpaces = fileDescriptorPath.replaceAll(" ", "%20");

        // parse and reify the descriptor
        pad = ProActive.getProactiveDescriptor(filePathWithoutSpaces);
    }

    /**
     * activate all main nodes and launch the main classes
     * @throws ProActiveException
     * @throws NodeException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    public void activate()
        throws ProActiveException, NodeException, ClassNotFoundException, 
            NoSuchMethodException {
        MainDefinition[] mainDefinitions = pad.getMainDefinitions();

        // activate mains
        pad.activateMains();

        // launch the main classes
        for (int i = 0; i < mainDefinitions.length; i++) {
            MainDefinition mainDefinition = mainDefinitions[i];
            VirtualNode[] virtualNodes = mainDefinition.getVirtualNodes();
            for (int j = 0; j < virtualNodes.length; j++) {
                VirtualNode virtualNode = virtualNodes[j];
                Node node = virtualNode.getNode();

                ProActive.newMain(mainDefinition.getMainClass(),
                    mainDefinition.getParameters(), node);
            }
        }
    }

    /**
     * return the launcher's pad
     * @return ProActiveDescriptor
     */
    public ProActiveDescriptor getProActiveDescriptor() {
        return pad;
    }
}
