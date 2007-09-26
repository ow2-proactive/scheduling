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
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory;
import org.objectweb.proactive.extensions.calcium.environment.FileServer;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;


public class ProActiveEnvironment implements EnvironmentFactory {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);
    ActiveTaskPool taskpool;
    TaskDispatcher dispatcher;
    FileServer fserver;

    public ProActiveEnvironment(String descriptor, String virtualNode)
        throws ProActiveException, ClassNotFoundException {
        Node localnode = NodeFactory.getDefaultNode();
        taskpool = Util.createActiveTaskPool(localnode);
        fserver = null; //TODO fix this
        Node[] nodes = Util.getNodes(descriptor, virtualNode);
        dispatcher = new TaskDispatcher(taskpool, fserver, nodes);
    }

    public TaskPool getTaskPool() {
        return taskpool;
    }

    public void start() {
        dispatcher.start();
    }

    public void shutdown() {
        dispatcher.shutdown();
        //TODO shutdown
    }

    public FileServer getFileServer() {
        return fserver;
    }

    public File getOutPutDir() {
        // TODO Auto-generated method stub
        return null;
    }
}
