package test.getactiveobjects;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import test.getactiveobjects.Agent;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Test
{

	public static void main(String[] args)
	{ try{
		//first launch the node on sea...
		Node node1 = NodeFactory.createNode("//palliata/bob");
		Node node2 = NodeFactory.getNode("//sea.inria.fr/tata");
		Node node3 = NodeFactory.createNode("//palliata/kit");
		
		//create two active Agent of different type.
		Agent agent = (Agent)org.objectweb.proactive.ProActive.newActive(Agent.class.getName(), new Object[]{"local"}, node1);
		org.objectweb.proactive.examples.migration.Agent agent5 = (org.objectweb.proactive.examples.migration.Agent)org.objectweb.proactive.ProActive.newActive(org.objectweb.proactive.examples.migration.Agent.class.getName(), new Object[]{"local"}, node1);
		
		//call method on this agent
		System.out.println("hostname: "+ agent.getName());
		System.out.println("nodename: "+ agent.getNodeName());
		agent.setMyName("toto");
		System.out.println("myname: "+ agent.getMyName());
		agent.moveTo("//sea.inria.fr/tata");
		Thread.sleep(5000);
		node1.getActiveObjects();
		//call getActiveObject method
		Agent agent2 = (Agent)((node2.getActiveObject("test.getactiveobjects.Agent"))[0]);
		System.out.println("hostname: "+ agent2.getName());
		System.out.println("nodename: "+ agent2.getNodeName());
		System.out.println("myname: "+ agent2.getMyName());
		agent.setMyName("titi");
		agent.moveTo("//palliata/kit");
		Thread.sleep(10000);
		
		
		
		Agent agent3 = (Agent)((node3.getActiveObjects())[0]);
		System.out.println("hostname: "+ agent3.getName());
		System.out.println("nodename: "+ agent3.getNodeName());
		System.out.println("myname: "+ agent3.getMyName());
		
		//following lines are useful to activate tensionning after body migration
		agent.setMyName("roqui");
		System.out.println("myname: "+agent3.getMyName());
		System.out.println("myname: "+agent2.getMyName());
		System.out.println("myname: "+agent.getMyName());
		
		//should throw an exception
		node2.getActiveObjects();
		}catch(Exception e){
			e.printStackTrace();
	}
	}
}
