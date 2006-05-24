package nonregressiontest.stub.abstractclass;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

public class Factory {
	
	public Factory(){}

	public AbstractClass getWidget(Node node) {
		try {
			return (AbstractClass) ProActive.newActive(ImplClass.class.getName(), new Object[] {},
					node);
		} catch (ActiveObjectCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
