package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


/**
 * An interface Front-End for the Admin to communicate with
 * the Infrastructure Manager 
 */
public interface IMAdmin extends Serializable {
	
	
    // FOR TESTING
    //-------------------------
    public StringWrapper echo();
    //-------------------------
    
    
    //----------------------------------------------------------------------//	
    // DEPLOY	
    
    /**
     * Deploy all vnode of the proactive descriptor giving by the paramter
     * 	<I>xmlDescriptor</I>.<BR/>
     * This function need a node in your local machine for deploying the file.
     * You can create the node with this instruction :<BR/>
     * <code>Node node = NodeFactory.createNode("nodeName");</code>
     * @param xmlDescriptor : the file proactive descriptor
     * @param remoteNode    : the node in your local host for transfering 
     * 	the file descriptor
     * @exception Exception : Cannot pull the remote file 
     */
    public void deployAllVirtualNodes(File xmlDescriptor, Node remoteNode) throws Exception;
    
    /**
     * Deploy the virtual node <I>vnName</I> of the proactive descriptor giving by the paramter
     * 	<I>xmlDescriptor</I>.<BR/>
     * This function need a node in your local machine for deploying the file.
     * You can create the node with this instruction :<BR/>
     * <code>Node node = NodeFactory.createNode("nodeName");</code>
     * @param xmlDescriptor : the file proactive descriptor
     * @param remoteNode    : the node in your local host for transfering
     * @param vnName 		: the name of virtual node that you want to deploy 
     * 	the file descriptor
     * @exception Exception : Cannot pull the remote file
     */
    public void deployVirtualNode(File xmlDescriptor, Node remoteNode, String vnName) throws Exception;
    
    /**
     * Deploy the virtual nodes giving by hte table<I>vnNames</I> of the proactive 
     * descriptor giving by the paramter <I>xmlDescriptor</I>.<BR/>
     * This function need a node in your local machine for deploying the file.
     * You can create the node with this instruction :<BR/>
     * <code>Node node = NodeFactory.createNode("nodeName");</code>
     * @param xmlDescriptor : the file proactive descriptor
     * @param remoteNode    : the node in your local host for transfering
     * @param vnNames 		: a table of the name of virtual nodes that you want to deploy 
     * 	the file descriptor
     * @exception Exception : Cannot pull the remote file  
     */
    public void deployVirtualNodes(File xmlDescriptor, Node remoteNode, String[] vnNames) throws Exception;
    
    
    //----------------------------------------------------------------------//	
    // GET THE DEPLOYED VNODES BY PAD
    
    /**
     * This method serve to get all deployed virtualnodes by proactive 
     * descriptor.<BR/>
     * It's used before calling methods <I>kill</I> or <I>redeploy</I> for 
     * getting the name of the pad and the names of deployed virtualnodes.
     * @return hashmap < String padName, ArrayList<VirtualNode> list of deployed virtualnodes >
     */
    public HashMap<String,ArrayList<VirtualNode>> getDeployedVirtualNodeByPad();
    


    //----------------------------------------------------------------------//	
    // REDEPLOY
    
    /**
     * Redeploy can't run because the actualy verion of ProActive don't support 
     * the redeploy (kill a vnode and after activate the same vnode).<BR/>
     * When this bug will be correct, see the comment FIXME of the method
     * redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
     * of the classe org.objectweb.proactive.extra.infrastructuremanager.core.
  	 *
     * @param padName : the name of the proactive descriptor that you want redeploy
     */
    public void redeploy(String padName);
    
    /**
     * 
     * @param padName : the name of the proactive descriptor
     * @param vnName  : the name of the virtual node of this proactive descriptor
     * that you want redeploy
     * @see redeploy(String padName);
     */
    public void redeploy(String padName, String vnName);
    
    
    /**
     * 
     * @param padName : the name of the proactive descriptor
     * @param vnNames : the names of the virtual node of this proactive descriptor
     * that you want redeploy
     * @see redeploy(String padName);
     */
    public void redeploy(String padName, String[] vnNames);
    
    
    //----------------------------------------------------------------------//	
    // KILL
    
    /**
     * Kill all virtual nodes of them proactive descriptors 
     * @exception ProActiveException
     */
    public void killAll() throws ProActiveException;
    
    /**
     * Kill the virtual nodes of the proactive descriptors <I>padName>/I>
     * @param padName : the name of the Proactive Descriptor
     * @see getDeployedVirtualNodeByPad() : for having the name of the deployed pad 
     * @exception ProActiveException
     */
    public void killPAD(String padName) throws ProActiveException;
    
    /**
     * Kill the virtual node <I>vnName</I> of the proactive descriptor <I>padName</I> 
     * @param padName : the name of the Proactive Descriptor
     * @param vnName  : the name of the virtual node for killing
     * @see  killPAD(String padName)
     * @see getDeployedVirtualNodeByPad() : for having the name of the deployed pad
     * and the name of deployed virtual node(s)
     * @exception ProActiveException
     */
    public void killPAD(String padName, String vnName);   
    
    /**
     * Kill the virtual nodes <I>vnNames</I>
     * of the proactive descriptor <I>padName</I>
     * @param padName : the name of the Proactive Descriptor
     * @param vnNames : the name of the virtual nodes for killing
     * @see  killPAD(String padName)
     * @see getDeployedVirtualNodeByPad() : for having the name of the deployed pad
     * and the name of deployed virtual node(s) 
     * @exception ProActiveException
     */
    public void killPAD(String padName, String[] vnNames); 
	

	
	//----------------------------------------------------------------------//
    // SHUTDOWN

    /**
     * Kill all ProActiveDescriptor and Infrastructure Manager
     * @exception ProActiveException
     */
    public void shutdown() throws ProActiveException;
}