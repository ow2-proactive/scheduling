/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.frontend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.RMCoreInterface;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManagerFactory;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicyFactory;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * Implementation of the {@link RMAdmin} active object.
 * the RMAdmin active object object is designed to receive and perform
 * administrator commands :<BR>
 * -initiate creation and removal of {@link org.ow2.proactive.resourcemanager.nodesource.deprecated.NodeSource} active objects.<BR>
 * -add nodes to static nodes sources ({@link org.ow2.proactive.resourcemanager.nodesource.deprecated.GCMNodeSource}), by
 * a ProActive descriptor.<BR>
 * -remove nodes from the RM.<BR>
 * -shutdown the RM.<BR>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
public class RMAdminImpl extends RMUserImpl implements RMAdmin, Serializable, InitActive {

    /**  */
    private static final long serialVersionUID = 200;

    /** RMCore active object of the RM */
    private RMCoreInterface rmcore;

    private RMAuthentication authentication;

    /** Log4J logger name for RMCore */
    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.ADMIN);

    /**
     * ProActive Empty constructor
     */
    public RMAdminImpl() {
    }

    /**
     * Creates the RMAdmin object
     * @param rmcore Stub of the {@link RMCore} active object of the RM.
     */
    public RMAdminImpl(RMCoreInterface rmcore, RMAuthentication authentication) {
        super(rmcore, authentication);
        this.rmcore = rmcore;
        this.authentication = authentication;
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    @Override
    public void initActivity(Body body) {
        try {
            thisStub = (RMUserImpl) PAActiveObject.getStubOnThis();

            PAActiveObject.registerByName(PAActiveObject.getStubOnThis(),
                    RMConstants.NAME_ACTIVE_OBJECT_RMADMIN);

            registerTrustedService(authentication);
            registerTrustedService(rmcore);

            userNodes = new Hashtable<Node, UniversalBody>();
            pinger = new Pinger("Admins pinger");

        } catch (ProActiveException e) {
            logger.debug("Cannot register RMAdmin. Aborting...", e);
            PAActiveObject.terminateActiveObject(true);
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#setAllNodeSourcesPingFrequency(int)
     */
    public void setAllNodeSourcesPingFrequency(int frequency) {
        this.rmcore.setAllPingFrequency(frequency);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#setDefaultNodeSourcePingFrequency(int)
     */
    public void setDefaultNodeSourcePingFrequency(int frequency) throws RMException {
        this.rmcore.setPingFrequency(frequency);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#setNodeSourcePingFrequency(int, java.lang.String)
     */
    public void setNodeSourcePingFrequency(int frequency, String sourceName) throws RMException {
        this.rmcore.setPingFrequency(frequency, sourceName);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#getNodeSourcePingFrequency(java.lang.String)
     */
    public IntWrapper getNodeSourcePingFrequency(String sourceName) throws RMException {
        return rmcore.getPingFrequency(sourceName);
    }

    /**
     * Creates a new node source with specified name, infrastructure manages {@link InfrastructureManager}
     * and acquisition policy {@link NodeSourcePolicy}.
     *
     * @param nodeSourceName the name of the node source
     * @param infrastructureType type of the underlying infrastructure {@link InfrastructureType}
     * @param infrastructureParameters parameters for infrastructure creation
     * @param policyType name of the policy type. It passed as a string due to pluggable approach {@link NodeSourcePolicyFactory}
     * @param policyParameters parameters for policy creation
     * @return constructed NodeSource
     * @throws RMException if any problems occurred
     */
    public void createNodesource(String nodeSourceName, String infrastructureType,
            Object[] infrastructureParameters, String policyType, Object[] policyParameters)
            throws RMException {
        this.rmcore.createNodesource(nodeSourceName, infrastructureType, infrastructureParameters,
                policyType, policyParameters);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper addNode(String nodeUrl) {
        return rmcore.addNode(nodeUrl);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper addNode(String nodeUrl, String sourceName) {
        return rmcore.addNode(nodeUrl, sourceName);
    }

    /**
     * Removes a node from the RM.
     *
     * @param nodeUrl URL of the node to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     */
    public void removeNode(String nodeUrl, boolean preempt) {
        this.rmcore.removeNode(nodeUrl, preempt);
    }

    /**
     * Remove a node source from the RM.
     * All nodes handled by the node source are removed.
     *
     * @param sourceName name (id) of the source to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     * @throws RMException if the node source doesn't exists
     */
    public void removeSource(String sourceName, boolean preempt) throws RMException {
        this.rmcore.removeNodeSource(sourceName, preempt);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#shutdown(boolean)
     */
    public void shutdown(boolean preempt) throws ProActiveException {
        pinger.stopThread();
        rmcore.shutdown(preempt);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#getNodesList()
     */
    public List<RMNodeEvent> getNodesList() {
        RMInitialState state = this.rmcore.getRMInitialState();
        return state.getNodesEvents();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#getNodeSourcesList()
     */
    public List<RMNodeSourceEvent> getNodeSourcesList() {
        return rmcore.getRMInitialState().getNodeSource();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public boolean connect(UniqueID id) {
        return registerTrustedService(id);
    }

    private Collection<PluginDescriptor> getPluginsDescriptor(Collection<Class<?>> plugins) {

        Collection<PluginDescriptor> descriptors = new ArrayList<PluginDescriptor>();

        for (Class<?> cls : plugins) {
            descriptors.add(new PluginDescriptor(cls));
        }
        return descriptors;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures() {
        return getPluginsDescriptor(InfrastructureManagerFactory.getSupportedInfrastructures());
    }

    /**
     * {@inheritDoc}
     */
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies() {
        return getPluginsDescriptor(NodeSourcePolicyFactory.getSupportedPolicies());
    }

}
