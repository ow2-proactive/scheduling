/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package scalabilityTests.fixtures;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import scalabilityTests.framework.Action;
import scalabilityTests.framework.ActiveActor;


/**
 * This fixture sets up a GCM infrastructure
 * 	onto which a set of {@link ActiveActor}s
 *  can execute their actions
 * 
 * @author fabratu
 *
 */
public class ActiveFixture {
    /** The GCM descriptor which will be used in order to deploy the Nodes */
    private final File gcmDeployment;
    /** The Virtual Node which will supply the nodes used for the scenario execution */
    private final String virtualNodeName;
    /** ProActive runtime representations of the deployment entities*/
    private GCMVirtualNode virtualNode;
    private GCMApplication gcmad;
    protected List<Node> nodes = null;
    /** List of Active Actors which are currently deployed on the infrastructure */
    protected final List<ActiveActor> knownActors = new LinkedList<>();

    private static final Logger logger = Logger.getLogger(ActiveFixture.class);

    public ActiveFixture(String gcmDeploymentPath, String vnName) throws IllegalArgumentException {
        File gcmDeployment = new File(gcmDeploymentPath);

        // routine checks of the path
        if (!gcmDeployment.exists())
            throw new IllegalArgumentException("File " + gcmDeploymentPath + " does not exist");
        if (!gcmDeployment.isFile())
            throw new IllegalArgumentException("The path " + gcmDeploymentPath + " does not point to a file");
        if (!gcmDeployment.canRead())
            throw new IllegalArgumentException("The file " + gcmDeploymentPath +
                " cannot be read - maybe check your permissions?");

        this.gcmDeployment = gcmDeployment;
        this.virtualNodeName = vnName;
    }

    public List<Node> loadInfrastructure() throws ProActiveException {
        logger.trace("Deploying the GCM infrastructure...");
        this.gcmad = PAGCMDeployment.loadApplicationDescriptor(this.gcmDeployment);
        this.gcmad.startDeployment();
        this.virtualNode = gcmad.getVirtualNode(this.virtualNodeName);
        if (this.virtualNode == null)
            throw new ProActiveException("Failed to acquire " + this.virtualNodeName + " virtual node");
        logger
                .trace("Waiting for the nodes of the virtual node " + this.virtualNodeName +
                    " to become ready");
        this.virtualNode.waitReady();
        this.nodes = this.virtualNode.getCurrentNodes();
        logger.trace("Done!");
        return this.nodes;
    }

    /**
     * This method will execute the same given action
     * on all the nodes previously loaded 
     * @throws NodeException 
     * @throws ActiveObjectCreationException 
     */
    public <T, V> void executeSameActionSameParameter(Action<T, V> action, T parameter)
            throws ActiveObjectCreationException, NodeException {
        if (this.nodes == null)
            throw new IllegalStateException(
                "Invalid usage of this object; loadInfrastructure() needs to be called first");
        logger.trace("# of available nodes: " + this.nodes.size());
        logger.trace("Deploying the actors...");
        List<ActiveActor<T, V>> actors = new LinkedList<>();
        for (Node node : nodes) {
            // TODO templated active objects? how?
            ActiveActor<T, V> actor = PAActiveObject.newActive(ActiveActor.class, new Object[] { action,
                    parameter }, node);
            actors.add(actor);
        }
        this.knownActors.addAll(actors);

        logger.trace("Executing the same action " + action + " on the same parameter " + parameter);
        for (ActiveActor<T, V> actor : actors) {
            actor.doAction();
        }
        logger.trace("Done!");
    }

    public void cleanup() {
        // kill all the known Actors
        for (ActiveActor actor : this.knownActors) {
            actor.cleanup();
        }
        // "undeploy" the infrastructure
        this.gcmad.kill();
        this.nodes = null;
        PALifeCycle.exitSuccess();
    }

}