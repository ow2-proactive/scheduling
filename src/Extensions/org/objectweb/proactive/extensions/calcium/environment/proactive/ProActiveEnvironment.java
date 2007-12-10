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
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PADeployment;
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


/**
 * This class provides distributed execution environment for {@link org.objectweb.proactive.extensions.calcium.Calcium Calcium}.
 * The environment is based on ProActive's deployment and active object models.
 *
 * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class ProActiveEnvironment implements EnvironmentFactory {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);
    ProActiveDescriptor pad;
    AOTaskPool taskpool;
    AOInterpreterPool interpool;
    TaskDispatcher dispatcher;
    FileServerClientImpl fserver;

    /**
     * Constructs an environment using the specified descriptor.
     * The descriptor must satisfy a contract with the following variables:
     *
     *  <pre>
     *         &lt;variables&gt;
              &lt;descriptorVariable name="SKELETON_FRAMEWORK_VN" value="framework" /&gt
                 &lt;descriptorVariable name="INTERPRETERS_VN" value="interpreters" /&gt
            &lt;/variables&gt
            </pre>
     *
     *
     * The variable <code>SKELETON_FRAMEWORK_VN</code> specifies the virtual node that will be used to store the
     * main active objects, such as taskpool, file server, etc.
     *
     * The <code>INTERPRETERS_VN</code> variable specifies the virtual node that will be used to execute the computation.
     *
     * And optionally with:<![CDATA[ <programDefaultVariable name="MAX_CINTERPRETERS" value="3"/> ]]>.
     *
     * @param descriptor The local descriptor path.
     * @throws ProActiveException If an error is detected.
     */
    public ProActiveEnvironment(String descriptor) throws ProActiveException {
        VariableContract vc = new VariableContract();
        vc.setVariableFromProgram("SKELETON_FRAMEWORK_VN", "",
            VariableContractType.DescriptorVariable);
        vc.setVariableFromProgram("INTERPRETERS_VN", "",
            VariableContractType.DescriptorVariable);
        vc.setVariableFromProgram("MAX_CINTERPRETERS", "3",
            VariableContractType.ProgramDefaultVariable);

        pad = PADeployment.getProactiveDescriptor(descriptor, vc);
        vc = pad.getVariableContract();

        int maxCInterp = Integer.parseInt(vc.getValue("MAX_CINTERPRETERS"));

        Node frameworkNode = Util.getFrameWorkNode(pad, vc);
        Node[] nodes = Util.getInterpreterNodes(pad, vc);

        taskpool = Util.createActiveTaskPool(frameworkNode);
        fserver = Util.createFileServer(frameworkNode);
        interpool = Util.createAOInterpreterPool(taskpool, fserver,
                frameworkNode, nodes, maxCInterp);

        dispatcher = new TaskDispatcher(taskpool, interpool);
    }

    /**
     * This method returns an active object version of the taskpool.
     * @see EnvironmentFactory#getTaskPool()
     */
    public TaskPool getTaskPool() {
        return taskpool;
    }

    /**
     * @see EnvironmentFactory#start();
     */
    public void start() {
        dispatcher.start();
    }

    /**
     * @see EnvironmentFactory#shutdown();
     */
    public void shutdown() {
        interpool.shutdown();
        dispatcher.shutdown();
        fserver.shutdown();

        try {
            pad.killall(true);
        } catch (ProActiveException e) {
            //We don't care about ProActive's death exceptions
            //e.printStackTrace();
        }
    }

    /**
     * This method returns an active object version of the file server.
     * @see EnvironmentFactory#getFileServer()
     */
    public FileServerClient getFileServer() {
        return fserver;
    }
}
