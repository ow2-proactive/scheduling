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
package org.objectweb.proactive.loadbalancing;

import java.util.ArrayList;
import java.util.Iterator;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.loadbalancing.metrics.MetricFactory;


public class LoadBalancing {
    private static ArrayList<LoadBalancer> loadBalancers = new ArrayList<LoadBalancer>();
    private static MetricFactory mf;
    private static boolean activated = false;
    private static InformationRecover ir;

    public static void activateOn(Node[] nodes, MetricFactory mf) {
        LoadBalancing.mf = mf;
        activated = true;
        LoadBalancer lb = null;

        try {
            ir = (InformationRecover) PAActiveObject.newActive(InformationRecover.class.getName(),
                    null);

            for (int i = 0; i < nodes.length; i++) {
                lb = (LoadBalancer) PAActiveObject.newActive(LoadBalancer.class.getName(),
                        new Object[] { mf }, nodes[i]);
                loadBalancers.add(lb);

                lb = null;
            }

            Iterator<LoadBalancer> it = loadBalancers.iterator();
            while (it.hasNext()) {
                lb = it.next();
                ArrayList<LoadBalancer> individualLB = new ArrayList<LoadBalancer>(loadBalancers);
                individualLB.remove(lb);
                lb.init(individualLB, ir);
                lb = null;
            }
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    public static void activate(MetricFactory mf) {
        LoadBalancing.mf = mf;
        activated = true;

        try {
            ir = (InformationRecover) PAActiveObject.newActive(InformationRecover.class.getName(),
                    null);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    public static void kill() {
        activated = false;

        PAActiveObject.terminateActiveObject(ir, true);
        LoadBalancer lb;
        Iterator<LoadBalancer> it = loadBalancers.iterator();
        while (it.hasNext()) {
            lb = it.next();
            PAActiveObject.terminateActiveObject(lb, true);
        }
    }

    public static void addNode(Node node) {
        if (!activated) {
            return; // TO DO : it's better to throw an exception
        }

        try {
            LoadBalancer lb = (LoadBalancer) PAActiveObject.newActive(LoadBalancer.class.getName(),
                    new Object[] { mf }, node);
            loadBalancers.add(lb);
            lb.init(loadBalancers, ir);
            lb.notifyLoadBalancers();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }
}
