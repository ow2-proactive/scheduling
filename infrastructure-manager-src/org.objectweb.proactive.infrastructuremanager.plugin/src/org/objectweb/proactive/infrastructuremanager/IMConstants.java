/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.infrastructuremanager;

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
    public static final Color RED_COLOR;
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
        RED_COLOR = new Color(device, 255, 0, 0);

        try {
            nodeTransfert = NodeFactory.createNode("NODE_TRANSFERT");
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}
