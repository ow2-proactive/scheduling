package org.objectweb.proactive.ic2d.infrastructuremanager;

import java.rmi.AlreadyBoundException;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

public class IMConstants {
	
	public static final String STATUS_AVAILABLE = "Available";
	public static final String STATUS_BUSY = "Busy";
	public static final String STATUS_DOWN = "Down";
	
	public static final Color DEFAULT_BORDER_COLOR;
	public static final Color HOST_COLOR;
	public static final Color JVM_COLOR;
	public static final Color AVAILABLE_COLOR;
	public static final Color BUSY_COLOR;
	public static final Color DOWN_COLOR;
	public static final Color WHITE_COLOR;
	
	public static Node nodeTransfert;
	
	static {
		Display device = Display.getCurrent();
		DEFAULT_BORDER_COLOR = new Color(device, 0, 0, 128);
		HOST_COLOR = new Color(device, 208, 208, 208);
		JVM_COLOR = new Color(device, 240, 240, 240);
		AVAILABLE_COLOR = new Color(device, 208, 208, 224);
		BUSY_COLOR = new Color(device, 255, 190, 0);
		DOWN_COLOR = new Color(device, 255, 0, 0);
		WHITE_COLOR = new Color(device, 255, 255, 255);

		try {
			nodeTransfert = NodeFactory.createNode("NODE_TRANSFERT");
		} 
		catch (NodeException e) {
			e.printStackTrace();
		} 
		catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
		
	}
	
}
