package org.ow2.proactive.forecaster;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;

/**
 * To be used by the resource Manager Simulator 
 * @author esalagea
 *
 */
public class FakeNode implements org.objectweb.proactive.core.node.Node{

	@Override
	public Object[] getActiveObjects() throws NodeException,
			ActiveObjectCreationException {
		return null;
	}

	@Override
	public Object[] getActiveObjects(String arg0) throws NodeException,
			ActiveObjectCreationException {
		return null;
	}

	@Override
	public NodeInformation getNodeInformation() {
		return null;
	}

	@Override
	public int getNumberOfActiveObjects() throws NodeException {
		return 0;
	}

	@Override
	public ProActiveRuntime getProActiveRuntime() {
		return null;
	}

	@Override
	public String getProperty(String arg0) throws ProActiveException {
		return null;
	}

	@Override
	public VMInformation getVMInformation() {
		return null;
	}

	@Override
	public void killAllActiveObjects() throws NodeException, IOException {
	
	}

	@Override
	public Object setProperty(String arg0, String arg1)
			throws ProActiveException {
		return null;
	}

}
