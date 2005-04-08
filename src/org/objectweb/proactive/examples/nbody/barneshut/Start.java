/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.nbody.barneshut;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.nbody.common.Displayer;

import java.util.Vector;


/**
 * <P>
 * This starts the Barnes-Hut example. Domains are synchronized through an OO-spmd scheme,
 * and their relations are defined through a tree representing spacial extent. This relation
 * is built on the distance from one Planet to a Domain.
 * </P>
 *
 * @author  ProActive Team
 * @version 1.0,  2005/04
 * @since   ProActive 2.2
 */
public class Start {
    public static void main(String[] args) {
        org.objectweb.proactive.examples.nbody.common.Start.main(args);
    }

    public static void main(int totalNbBodies, int maxIter,
        Displayer displayer, Node[] nodes,
        org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        System.out.println("RUNNING Barnes-Hut VERSION");

        // Create all the Domains, based on the tree structure
        QuadTree tree = new QuadTree(totalNbBodies);
        Vector info = tree.getInfo();
        int size = tree.size();

        Object[][] constructorParams = new Object[size][3]; // the parameters passed to the Domain constructors
        for (int i = 0; i < size; i++) {
            constructorParams[i][0] = new Integer(i);
            constructorParams[i][1] = (Info) info.get(i);
            constructorParams[i][2] = killsupport;
        }
        Domain domainGroup = null;
        try {
            // Create all the Domains, inside an SPMD Group, to enable barriers
            domainGroup = (Domain) ProSPMD.newSPMDGroup(Domain.class.getName(),
                    constructorParams, nodes);
        } catch (ClassNotReifiableException e) {
            killsupport.abort(e);
        } catch (ClassNotFoundException e) {
            killsupport.abort(e);
        } catch (ActiveObjectCreationException e) {
            killsupport.abort(e);
        } catch (NodeException e) {
            killsupport.abort(e);
        }

        System.out.println("[NBODY] " + size + " domains are deployed");

        // init Domains, with their possible neighbours, display, and quadtree
        Group group = ProActiveGroup.getGroup(domainGroup);
        Domain[] domainArray = (Domain[]) group.toArray(new Domain[] {  });

        domainGroup.init(domainArray, displayer, tree, maxIter);
        // Within init, there are enough instructions to start computing the movement. 
    }
}
