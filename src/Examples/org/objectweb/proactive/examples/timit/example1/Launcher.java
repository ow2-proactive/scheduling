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
package org.objectweb.proactive.examples.timit.example1;

import java.io.File;
import java.util.List;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAException;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.timit.util.BenchmarkStatistics;
import org.objectweb.proactive.benchmarks.timit.util.Startable;
import org.objectweb.proactive.benchmarks.timit.util.TimItManager;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * A simple distributed application that use TimIt.<br>
 * The application have two classes : Launcher, Worker<br>
 * Launcher will deploy some Workers to do a job.
 *
 * Notice that TimIt will automatically invoke the start and kill methods<br>
 *
 * See the source code of these classes to know how use TimIt.
 *
 * @author The ProActive Team
 *
 */
public class Launcher implements Startable {

    /** The typed group of workers */
    private Worker workers;
    private GCMApplication pad;

    /** TimIt needs an noarg constructor (can be implicit) */
    public Launcher() {
    }

    /** The main method, not used by TimIt */
    public static void main(String[] args) {
        new Launcher().start(args);
    }

    /**
     * Part of Startable implementation. TimIt will invoke this method with
     * arguments provided by the xml deployement descriptor.
     *
     * @params The array of parameters.
     * @see org.objectweb.proactive.benchmarks.timit.util.Startable
     */
    public void start(String[] args) {
        try {
            // Common stuff about ProActive deployement
            this.pad = PAGCMDeployment.loadApplicationDescriptor(new File(args[0]));
            int np = Integer.valueOf(args[1]).intValue();

            this.pad.startDeployment();
            GCMVirtualNode vnode = this.pad.getVirtualNode("Workers");

            vnode.waitReady();

            List<Node> nodes = vnode.getCurrentNodes();
            System.out.println(nodes.size() + " nodes found, " + np + " wanted. ");

            Object[] param = new Object[] {};
            Object[][] params = new Object[np][];
            for (int i = 0; i < np; i++) {
                params[i] = param;
            }

            Node[] nodeArray = (Node[]) nodes.toArray(new Node[] {});

            this.workers = (Worker) PASPMD.newSPMDGroup(Worker.class.getName(), params, nodeArray);

            // You must create a TimItManager instance and give it
            // a typed group of Timed workers.
            // Remember that there must be 1 typed group per 1
            // TimeIt instance.
            TimItManager tManager = TimItManager.getInstance();
            tManager.setTimedObjects(this.workers);

            // Workers starts their job
            this.workers.start();

            // ... and finalize the TimIt.
            // Notice that you don't have to wait for the end of your workers
            BenchmarkStatistics bstats = tManager.getBenchmarkStatistics();
            System.out.println(bstats);

            System.out.println(tManager.getBenchmarkStatistics());
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

    /**
     * Part of the Startable implementation. TimIt will invoke this method
     * between each run.
     *
     * @see org.objectweb.proactive.benchmarks.timit.util.Startable
     */
    public void kill() {
        this.workers.terminate();
        PAException.waitForPotentialException();
    }

    public void masterKill() {
        this.pad.kill();
    }
}
