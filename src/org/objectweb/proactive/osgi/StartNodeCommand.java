package org.objectweb.proactive.osgi;

import java.io.PrintStream;
import java.rmi.AlreadyBoundException;
import java.util.StringTokenizer;

import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ungoverned.osgi.service.shell.Command;

public class StartNodeCommand implements Command{

	public String getName() {
		return "startNode";
	}

	public String getUsage() {
		return "startNode";
	}

	public String getShortDescription() {
		return "Starts a ProActive Node";
	}

	public void execute(String arg0, PrintStream arg1, PrintStream arg2) {
		System.out.println("Starting a ProActive Node ...");
		StringTokenizer st = new StringTokenizer(arg0);
		st.nextToken();
		
		
		String nodeName = st.nextToken();
		
		try {
			NodeFactory.createNode(nodeName, false, null, null);
		} catch (NodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
