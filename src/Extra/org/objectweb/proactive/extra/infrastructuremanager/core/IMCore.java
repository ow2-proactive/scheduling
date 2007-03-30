package org.objectweb.proactive.extra.infrastructuremanager.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMDataResource;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.database.IMDataResourceImpl;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdminImpl;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoringImpl;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUser;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUserImpl;


public class IMCore implements InitActive, IMConstants, Serializable {

	private final static Logger logger = ProActiveLogger
			.getLogger(Loggers.IM_CORE);

	// Attributes
	private Node nodeIM;

	private IMAdmin admin;

	private IMMonitoring monitoring;

	private IMUser user;

	private IMDataResource dataresource;


	// ----------------------------------------------------------------------//
	// CONSTRUCTORS

	/** ProActive compulsory no-args constructor */
	public IMCore() {
	}

	public IMCore(Node nodeIM) throws ActiveObjectCreationException,
			NodeException {
		if (logger.isInfoEnabled()) {
			logger.info("IMCore constructor");
		}
		this.nodeIM = nodeIM;
		if (logger.isInfoEnabled()) {
			logger.info("instanciation IMDataResourceImpl");
		}
		this.dataresource = new IMDataResourceImpl();
	}

	// ----------------------------------------------------------------------//
	// INIT ACTIVE FRONT-END
	
	/**
	 * Initialize the actif object : IMAdmin, IMMonitoring, IMUser
	 * @param body
	 */
	public void initActivity(Body body) {
		if (logger.isInfoEnabled()) {
			logger.info("IMCore start : initActivity");
		}
		try {
			if (logger.isInfoEnabled()) {
				logger.info("active object IMAdmin");
			}
			admin = (IMAdminImpl) ProActive.newActive(IMAdminImpl.class
					.getName(), new Object[] { ProActive.getStubOnThis() },
					nodeIM);

			if (logger.isInfoEnabled()) {
				logger.info("active object IMMonitoring");
			}
			monitoring = (IMMonitoringImpl) ProActive.newActive(
					IMMonitoringImpl.class.getName(), new Object[] { ProActive
							.getStubOnThis() }, nodeIM);

			if (logger.isInfoEnabled()) {
				logger.info("active object IMUser");
			}
			user = (IMUserImpl) ProActive.newActive(IMUserImpl.class.getName(),
					new Object[] { ProActive.getStubOnThis() }, nodeIM);
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}
		if (logger.isInfoEnabled()) {
			logger.info("IMCore end : initActivity");
		}
	}

	// ----------------------------------------------------------------------//
	// TEST
	
	public String echo() {
		return "Je suis le IMCore";
	}

	
	
	// ----------------------------------------------------------------------//
	// ACCESSORS
	
	public Node getNodeIM() {
		return this.nodeIM;
	}

	public IMAdmin getAdmin() {
		return this.admin;
	}

	public IMMonitoring getMonitoring() {
		return this.monitoring;
	}

	public IMUser getUser() {
		return this.user;
	}

	

	// ----------------------------------------------------------------------//
	// ADMIN

	/**
	 * Add the new deployed node in the dataresource
	 * @param node    : the new deployed node 
	 * @param vnName  : the name of the virtual node
	 * @param padName : the name of the proactive descriptor
	 */
	public void addNode(Node node, String vnName, String padName) {
		if (logger.isInfoEnabled()) {
			logger.info("IMCore - addNode : node=" + node.getNodeInformation().getName() +
					"\t\t vnName=" + vnName + 
					"\t\t padName="+ padName);
		}
		this.dataresource.addNewDeployedNode(node, vnName, padName);
	}
	
	/**
	 * Add the new proactive descriptor in the dataresource
	 * @param padName : the name of the proactive descriptor
	 * @param pad     : the proactive descriptor
	 */
	public void addPAD(String padName, ProActiveDescriptor pad) {
		this.dataresource.putPAD(padName, pad);
	}
	


	// ----------------------------------------------------------------------//	
	// REDEPLOY
	// FIXME The redeploy (kill+activate) isn't support in the actualy 
	// version of ProActive

	private void redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad) throws RuntimeException {
		if( vnode.isActivated() ) {
			vnode.killAll(false);
			this.dataresource.removeNode(padName, vnode.getName());	
		}
		// FIXME uncomment this line below when the problem of redeploy will be fix 
		//IMDeploymentFactory.deployVirtualNode(this, padName, pad, vnode.getName());
		throw new RuntimeException("The redeploy (kill+activate) isn't support in the actualy version of ProActive");
	}

	
	/**
	 * Redeploy not supported by the current version of ProActive
	 * @param padName : the name of the proactive descriptor to redeploy
	 * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
	 */
	public void redeploy(String padName) {
		if( this.dataresource.isDeployedPad(padName) ) {
			ProActiveDescriptor pad = this.dataresource.getDeployedPad(padName);
			VirtualNode[] vnodes = pad.getVirtualNodes();
			for(VirtualNode vnode : vnodes) {
				redeployVNode(vnode, padName, pad);
			}
		}
	}


	/**
	 * Redeploy not supported by the current version of ProActive
	 * @param padName : the name of the proactive descriptor
	 * @param vnName  : the name of the virtual node of this pad to redeploy
	 * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
	 */
	public void redeploy(String padName, String vnName) {
		if( this.dataresource.isDeployedPad(padName) ) {
			ProActiveDescriptor pad = this.dataresource.getDeployedPad(padName);
			VirtualNode vnode = pad.getVirtualNode(vnName);
			redeployVNode(vnode, padName, pad);
		}
	}


	/**
	 * Redeploy not supported by the current version of ProActive
	 * @param padName : the name of the proactive descriptor
	 * @param vnNames : the name of the virtual nodes of this pad to redeploy
	 * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
	 */
	public void redeploy(String padName, String[] vnNames) {
		if( this.dataresource.isDeployedPad(padName) ) {
			ProActiveDescriptor pad = this.dataresource.getDeployedPad(padName);
			VirtualNode vnode;
			for(String vnName : vnNames) {
				vnode = pad.getVirtualNode(vnName);
				redeployVNode(vnode, padName, pad);
			}
		}
	}
	
	
	
	// ----------------------------------------------------------------------//	
	// KILL
	
	/**
	 * Kill all virtual nodes of them proactive descriptors
	 * TODO delete the pad file
	 * @param padName :  the name of the proactive descriptor
	 * @exception ProActiveException
	 */
	public void killPAD(String padName) throws ProActiveException {
		if( this.dataresource.isDeployedPad(padName) ) {
			ProActiveDescriptor pad = this.dataresource.getDeployedPad(padName);
			pad.killall(false);
			this.dataresource.removeNode(padName);
			this.dataresource.removePad(padName);
			// FIXME : delete the pad file
			// find the temp directory but how ????
			// File tempPAD = new File( tempDir + padName) 
			// if ( tempPAD.exists() ) tempPAD.delete();
		}
	}

	
	/**
     * Kill the virtual nodes of the proactive descriptors <I>padName>/I>
     * @param padName : the name of the Proactive Descriptor 
     * @exception ProActiveException
     */
	public void killPAD(String padName, String vnName) {
		if( this.dataresource.isDeployedPad(padName) ) {
			ProActiveDescriptor pad = this.dataresource.getDeployedPad(padName);
			VirtualNode vnode = pad.getVirtualNode(vnName);
			vnode.killAll(false);
			this.dataresource.removeNode(padName, vnName);
		}
	}
	
	/**
     * Kill the virtual node <I>vnName</I> of the proactive descriptor <I>padName</I> 
     * @param padName : the name of the Proactive Descriptor
     * @param vnName  : the name of the virtual node for killing
     * @see  killPAD(String padName)
     * @exception ProActiveException
     */
	public void killPAD(String padName, String[] vnNames) {
		if( this.dataresource.isDeployedPad(padName) ) {
			ProActiveDescriptor pad = this.dataresource.getDeployedPad(padName);
			VirtualNode[] vnodes = pad.getVirtualNodes();
			for(VirtualNode vnode : vnodes) {
				vnode.killAll(false);
			}
			this.dataresource.removeNode(padName, vnNames);
		}
	}
	
	
	/**
     * Kill the virtual nodes <I>vnNames</I>
     * of the proactive descriptor <I>padName</I>
     * @param padName : the name of the Proactive Descriptor
     * @param vnNames : the name of the virtual nodes for killing
     * @see  killPAD(String padName) 
     * @exception ProActiveException
     */
	public void killAll() throws ProActiveException {
		for(String padName : this.dataresource.getListPad().keySet()) {
			killPAD(padName);
		}
	}
	
	
	
	// ----------------------------------------------------------------------//
	// MONITORING

	public int getSizeListFreeIMNode() { return dataresource.getSizeListFreeIMNode(); }
	public int getSizeListBusyIMNode() { return dataresource.getSizeListBusyIMNode(); }
	public int getSizeListDownIMNode() { return dataresource.getSizeListDownIMNode(); }
	public int getNbAllIMNode() 	   { return dataresource.getNbAllIMNode(); }
	public int getSizeListPad()        { return dataresource.getSizeListPad(); }
	
	public HashMap<String,ProActiveDescriptor> getListPAD() {
		return this.dataresource.getListPad();
	}
	
	public HashMap<String,ArrayList<VirtualNode>> getDeployedVirtualNodeByPad() {
		return this.dataresource.getDeployedVirtualNodeByPad();
	}
	
	public ArrayList<IMNode> getListFreeIMNode() {
		return this.dataresource.getListFreeIMNode();
	}
	
	public ArrayList<IMNode> getListBusyIMNode() {
		return this.dataresource.getListBusyIMNode();
	}
	
	public ArrayList<IMNode> getListAllNodes() {
		return this.dataresource.getListAllIMNode();
	}
	
	
	
	// ----------------------------------------------------------------------//
	// USER

	 /**
     * Method to get only one node
     * @return node  
     * @throws NodeException 
     */
	public Node getNode() throws NodeException  {
		return this.dataresource.getNode();
	}
	
	
	/**
     * Reserves nb nodes, if the infrastructure manager (IM) don't have nb free nodes 
     * then it returns the max of free nodes
     * @param nb the number of nodes 
     * @return an arraylist of nodes
     * @throws NodeException
     */
	public Node[] getAtLeastNNodes(int nb) throws NodeException  {
		return this.dataresource.getAtLeastNNodes(nb);
	}

	
	/**
     * Release the node reserve by the user
     * @param node : the node to release
     * @throws NodeException
     */
	public void freeNode(Node node) throws NodeException {
		this.dataresource.freeNode(node);
	}
	
	
	/**
     * Release the nodes reserve by the user
     * @param nodes : a table of nodes to release 
     * @throws NodeException
     */
	public void freeNodes(Node[] nodes) throws NodeException {
		this.dataresource.freeNodes(nodes);
	}
	
	public void nodeIsDown(IMNode imNode) {
		this.dataresource.nodeIsDown(imNode);
		
	}

	// ----------------------------------------------------------------------//
	// SHUTDOWN
	
	/**
	 * Kill all the proactive descriptor and quit the application
	 * @see killall()
	 * @exception ProActiveException
	 */
	public void shutdown() throws ProActiveException {
		killAll();
		ProActive.exitSuccess();
	}
	

	// ----------------------------------------------------------------------//

}