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

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.calcium.environment.Environment;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentServices;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * This class provides distributed execution environment for {@link org.objectweb.proactive.extensions.calcium.Calcium Calcium}.
 * The environment is based on ProActive's deployment and active object models.
 *
 * @author The ProActive Team (mleyton)
 */
public class ProActiveEnvironment implements EnvironmentServices {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);
    ProActiveDescriptor pad;
    AOTaskPool taskpool;
    AOInterpreterPool interpool;
    TaskDispatcher dispatcher;
    FileServerClientImpl fserver;

    public static Environment factory(String descriptor, boolean useGCM) throws ProActiveException {

        VariableContractImpl vc = new VariableContractImpl();
        vc.setVariableFromProgram("SKELETON_FRAMEWORK_VN", "", VariableContractType.DescriptorVariable);
        vc.setVariableFromProgram("INTERPRETERS_VN", "", VariableContractType.DescriptorVariable);
        vc.setVariableFromProgram("MAX_CINTERPRETERS", "3", VariableContractType.ProgramDefaultVariable);

        if (!useGCM) {
            return new ProActiveEnvironment(descriptor, vc);
        }

        return new ProActiveEnvironment(new File(descriptor), vc);
    }

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
    ProActiveEnvironment(String descriptor, VariableContract vc) throws ProActiveException {

        pad = PADeployment.getProactiveDescriptor(descriptor, vc);
        vc = pad.getVariableContract();

        int maxCInterp = Integer.parseInt(vc.getValue("MAX_CINTERPRETERS"));

        Node frameworkNode = Util.getFrameWorkNode(pad, vc);
        Node[] nodes = Util.getInterpreterNodes(pad, vc);

        taskpool = Util.createActiveTaskPool(frameworkNode);
        fserver = Util.createFileServer(frameworkNode);
        interpool = Util.createAOInterpreterPool(taskpool, fserver, frameworkNode, nodes, maxCInterp);

        dispatcher = new TaskDispatcher(taskpool, interpool);
    }

    /**
     * Loads a ProActive Environment using the new GCM Deployment
     * 
     * @param descriptor The location of the deployment file.
     * @param vContract The variables contract
     * @throws ProActiveException 
     */
    ProActiveEnvironment(File descriptor, VariableContractImpl vContract) throws ProActiveException {

        GCMApplication pad = PAGCMDeployment.loadApplicationDescriptor(descriptor, vContract);
        VariableContract vc = pad.getVariableContract();

        pad.startDeployment();

        int maxCInterp = Integer.parseInt(vc.getValue("MAX_CINTERPRETERS"));

        GCMVirtualNode vnFramework = pad.getVirtualNode(vc.getValue("SKELETON_FRAMEWORK_VN"));
        Node frameworkNode = vnFramework.getANode();

        taskpool = Util.createActiveTaskPool(frameworkNode);
        fserver = Util.createFileServer(frameworkNode);
        interpool = Util.createAOInterpreterPool(taskpool, fserver, frameworkNode);
        dispatcher = new TaskDispatcher(taskpool, interpool);

        GCMVirtualNode vnInterpreters = pad.getVirtualNode(vc.getValue("INTERPRETERS_VN"));
        vnInterpreters.subscribeNodeAttachment(new NodeCreationListener(taskpool, fserver, interpool,
            maxCInterp), "listener", true);
    }

    /**
     * This method returns an active object version of the taskpool.
     * @see Environment#getTaskPool()
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
        } catch (Exception e) {
            //We don't care about ProActive's death exceptions
            //e.printStackTrace();
        }
    }

    /**
     * This method returns an active object version of the file server.
     * @see Environment#getFileServer()
     */
    public FileServerClient getFileServer() {
        return fserver;
    }

    public String getName() {
        return "ProActive Environment";
    }

    public int getVersion() {
        return 1;
    }
}