/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;


public class ProActiveEnvironment implements EnvironmentFactory {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);
    ProActiveDescriptor pad;
    AOTaskPool taskpool;
    TaskDispatcher dispatcher;
    FileServerClientImpl fserver;
    AOInterpreterPool interPool;

    public ProActiveEnvironment(String descriptor) throws ProActiveException {
        //get the local node: NodeFactory.getDefaultNode();
        VariableContract vc = new VariableContract();
        vc.setVariableFromProgram("SKELETON_FRAMEWORK_VN", "",
            VariableContractType.DescriptorVariable);
        vc.setVariableFromProgram("INTERPRETERS_VN", "",
            VariableContractType.DescriptorVariable);
        vc.setVariableFromProgram("MAX_CINTERPRETERS", "3",
            VariableContractType.ProgramDefaultVariable);

        pad = ProDeployment.getProactiveDescriptor(descriptor, vc);
        vc = pad.getVariableContract();

        int maxCInterp = Integer.parseInt(vc.getValue("MAX_CINTERPRETERS"));

        Node frameworkNode = Util.getFrameWorkNode(pad, vc);
        Node[] nodes = Util.getInterpreterNodes(pad, vc);

        taskpool = Util.createActiveTaskPool(frameworkNode);
        interPool = Util.createActiveInterpreterPool(frameworkNode);
        fserver = Util.createFileServer(frameworkNode);
        AOInterpreter[] aoi = Util.createAOinterpreter(nodes);

        try {
            dispatcher = new TaskDispatcher(maxCInterp, taskpool, fserver,
                    interPool, aoi);
        } catch (ClassNotFoundException e) {
            throw new ProActiveException(e);
        }
    }

    public TaskPool getTaskPool() {
        return taskpool;
    }

    public void start() {
        dispatcher.start();
    }

    public void shutdown() {
        dispatcher.shutdown();
        fserver.shutdown();
        try {
            pad.killall(true);
        } catch (ProActiveException e) {
            //We don't care about ProActive's death exceptions
            //e.printStackTrace();
        }
    }

    public FileServerClient getFileServer() {
        return fserver;
    }
}
