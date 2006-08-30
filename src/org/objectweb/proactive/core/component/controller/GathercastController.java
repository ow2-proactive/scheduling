package org.objectweb.proactive.core.component.controller;

import java.util.List;

import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.request.ComponentRequest;


/**
 *
 *
 * @author Matthieu Morel
 *
 */
public interface GathercastController extends CollectiveInterfaceController {
	
	/**
	 * Notifies this component that a binding has been performed to one of its gathercast interfaces 
	 * @param serverItfName the name of the gathercast interface
	 * @param owner a reference on the component connecting to the gathercast interface
	 * @param clientItfName the name of the interface connecting to the gathercast interface
	 */
    public void addedBindingOnServerItf(String serverItfName,
        ProActiveComponent sender, String clientItfName);

    /**
     * Notifies this component that a binding has been removed from one of its gathercast interfaces 
	 * @param serverItfName the name of the gathercast interface
	 * @param owner a reference on the component connected to the gathercast interface
	 * @param clientItfName the name of the interface connected to the gathercast interface
     */
    public void removedBindingOnServerItf(String serverItfName,
        ProActiveComponent owner, String clientItfName);

    /**
     * Returns a list of references to the interfaces connected to a given gathercast interface of this component
     * @param serverItfName name of a gathercast interface
     * @return the list of interfaces connected to this gathercast interface
     */
    public List<ItfID> getConnectedClientItfs(String serverItfName);

    /**
     * Delegates a request to be processed by the gathercast controller
     * @param r a request on a gathercast interface
     * @return the result of the gathercast invocation
     * @throws ServeException if the request handling failed
     */
    public Object handleRequestOnGatherItf(ComponentRequest r)
        throws ServeException;
}
