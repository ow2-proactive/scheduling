package org.objectweb.proactive.examples.userguide.cmagent.migration;

import java.io.Serializable;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.examples.userguide.cmagent.initialized.CMAgentInitialized;

public class CMAgentMigrator extends CMAgentInitialized implements Serializable{
	public void migrateTo(Node whereTo){
		try {
			//should be the last call in the method
			PAMobileAgent.migrateTo(whereTo);
		}
		catch (ProActiveException moveExcep)	{
			System.err.println(moveExcep.getMessage());
		}
	}
}
