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
package org.objectweb.proactive.examples.timit.example2;

import java.util.Iterator;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.api.ProException;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.benchmarks.timit.util.BenchmarkStatistics;
import org.objectweb.proactive.benchmarks.timit.util.Startable;
import org.objectweb.proactive.benchmarks.timit.util.TimItManager;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * A simple distributed application that use TimIt.<br>
 * The application have three classes : Launcher, Worker and Root<br>
 * Launcher will deploy some Workers to do a job. Theses Workers will use a Root
 * instance to do it.
 *
 * See the source code of these classes to know how use TimIt
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */

// You have to implements Startable implements
public class Launcher implements Startable {
    private Worker workers; // Our typed group of workers
    private ProActiveDescriptor pad; // A reference to the descriptor

    // TimIt needs an noarg constructor (can be implicit)
    public Launcher() {
    }

    // If you need a main method
    public static void main(String[] args) {
        new Launcher().start(args);
    }

    // Entry point to implements called by TimIt
    public void start(String[] args) {
        try {
            // Common stuff about ProActive deployement
            this.pad = ProDeployment.getProactiveDescriptor(args[0]);
            int np = Integer.valueOf(args[1]).intValue();

            this.pad.activateMappings();
            VirtualNode vnode = this.pad.getVirtualNode("Workers");

            Node[] nodes = vnode.getNodes();
            System.out.println(nodes.length + " nodes found, " + np +
                " wanted. ");

            Object[] param = new Object[] {  };
            Object[][] params = new Object[np][];
            for (int i = 0; i < np; i++) {
                params[i] = param;
            }

            this.workers = (Worker) ProSPMD.newSPMDGroup(Worker.class.getName(),
                    params, nodes);

            // You must create a TimItManager instance and give to it
            // typed group of Timed workers
            TimItManager tManager = TimItManager.getInstance();
            tManager.setTimedObjects(this.workers);
            // Just start your workers...
            this.workers.start();

            BenchmarkStatistics bstats = tManager.getBenchmarkStatistics();
            System.out.println(bstats);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (ProActiveException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // You have to implement a kill() method called by TimIt between each run.
    // Here you can terminate all your workers as here
    public void kill() {
        Group<Worker> gWorkers = ProGroup.getGroup(workers);
        Iterator<Worker> it = gWorkers.iterator();

        while (it.hasNext()) {
            ProActiveObject.terminateActiveObject(it.next(), true);
        }
        ProException.waitForPotentialException();

        try {
            this.pad.killall(false);
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    public void masterKill() {
    }
}
