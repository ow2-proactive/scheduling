/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.descriptor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PALauncher;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.MainDefinition;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * This class provides the stuff useful to launch an
 * application directly from a proactive descriptor XML file.
 *
 * be careful, your xml file must define a mainDefinition tag
 * that contains the main class path, otherwise the application will
 * not be launched.
 *
 * @version 1.0,  2005/09/20
 * @since   ProActive 3.0
 * @author ProActive team
 */
public class Launcher {
    private ProActiveDescriptorInternal pad;
    private boolean activated;

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
    public Launcher(String fileDescriptorPath) throws ProActiveException, NodeException {
        // replace spaces by %20 char (hexadecimal space code) to avoid bug with the property
        String filePathWithoutSpaces = fileDescriptorPath.replaceAll(" ", "%20");

        // parse and reify the descriptor
        pad = PADeployment.getProactiveDescriptor(filePathWithoutSpaces).getProActiveDescriptorInternal();
        activated = false;
    }

    /**
     * activate all main nodes and launch the main classes
     * @throws ProActiveException
     * @throws NodeException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    public void activate() throws ProActiveException, NodeException, ClassNotFoundException,
            NoSuchMethodException {
        MainDefinition[] mainDefinitions = pad.getMainDefinitions();

        // activate mains
        pad.activateMains();

        // launch the main classes
        for (int i = 0; i < mainDefinitions.length; i++) {
            MainDefinition mainDefinition = mainDefinitions[i];
            VirtualNodeInternal[] virtualNodes = mainDefinition.getVirtualNodes();
            for (int j = 0; j < virtualNodes.length; j++) {
                VirtualNodeInternal virtualNode = virtualNodes[j];
                Node node = virtualNode.getNode();

                PALauncher.newMain(mainDefinition.getMainClass(), mainDefinition.getParameters(), node);
            }
        }
        activated = true;
    }

    /**
     * return the launcher's pad
     * @return ProActiveDescriptor
     */
    public ProActiveDescriptorInternal getProActiveDescriptor() {
        return pad;
    }

    /**
     * return true if the launcher has ever been activated
     * @return true if not activated
     */
    public boolean isActivated() {
        return activated;
    }
}
