package org.ow2.proactive_grid_cloud_portal;

import java.security.KeyException;
import java.util.Collection;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;

public class RMProxy implements ResourceManager{

    private ResourceManager target;

    private String rmUrl;
    private String userName;
    private String password;
    
    
    public boolean init(String url, String user, String pwd) throws RMException, KeyException, LoginException  {
        this.rmUrl = url;
        this.userName = user;
        this.password = pwd;

            RMAuthentication rmAuth = RMConnection.join("rmi://dalek.activeeon.com:1099/");
            Credentials cred = Credentials.createCredentials("admin", "admin", rmAuth.getPublicKey());
            target = rmAuth.login(cred);
     
        return true;
    }
    
    
    @Override
    public BooleanWrapper addNode(String arg0) {
        return target.addNode(arg0);
    }

    @Override
    public BooleanWrapper addNode(String arg0, String arg1) {
        return target.addNode(arg0, arg1);
    }

    @Override
    public BooleanWrapper createNodeSource(String arg0, String arg1, Object[] arg2, String arg3, Object[] arg4) {
        return target.createNodeSource(arg0, arg1, arg2, arg3, arg4);
    }

    @Override
    public BooleanWrapper disconnect() {
        return target.disconnect();
    }

    @Override
    public NodeSet getAtMostNodes(int arg0, SelectionScript arg1) {
        return target.getAtMostNodes(arg0, arg1);
    }

    @Override
    public NodeSet getAtMostNodes(int arg0, SelectionScript arg1, NodeSet arg2) {
        return target.getAtMostNodes(arg0, arg1, arg2);
    }

    @Override
    public NodeSet getAtMostNodes(int arg0, List<SelectionScript> arg1, NodeSet arg2) {
        return target.getAtMostNodes(arg0, arg1, arg2);
    }

    @Override
    public RMMonitoring getMonitoring() {
        return target.getMonitoring();
    }

    @Override
    public IntWrapper getNodeSourcePingFrequency(String arg0) {
        return target.getNodeSourcePingFrequency(arg0);
    }

    @Override
    public RMState getState() {
        return target.getState();
    }

    @Override
    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures() {
        return target.getSupportedNodeSourceInfrastructures();
    }

    @Override
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies() {
        return target.getSupportedNodeSourcePolicies();
    }

    @Override
    public BooleanWrapper isActive() {
        return target.isActive();
    }

    @Override
    public BooleanWrapper nodeIsAvailable(String arg0) {
        return target.nodeIsAvailable(arg0);
    }

    @Override
    public BooleanWrapper releaseNode(Node arg0) {
        return target.releaseNode(arg0);
    }

    @Override
    public BooleanWrapper releaseNodes(NodeSet arg0) {
        return target.releaseNodes(arg0);
    }

    @Override
    public BooleanWrapper removeNode(String arg0, boolean arg1) {
        return target.removeNode(arg0, arg1);
    }

    @Override
    public BooleanWrapper removeNodeSource(String arg0, boolean arg1) {
        return target.removeNodeSource(arg0, arg1);
    }

    @Override
    public BooleanWrapper setNodeSourcePingFrequency(int arg0, String arg1) {
        return target.setNodeSourcePingFrequency(arg0, arg1);
    }

    @Override
    public BooleanWrapper shutdown(boolean arg0) {
        return target.shutdown(arg0);
    }

}
