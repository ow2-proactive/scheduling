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
package org.objectweb.proactive.p2p.core.solver;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.p2p.core.problem.Problem;
import org.objectweb.proactive.p2p.core.problem.Result;
import org.objectweb.proactive.p2p.core.worker.Worker;


/**
 * @author Alexandre di Costanzo
 *
 */
public abstract class Solver {
    private Worker root = null;

    public Solver() {
        try {
            this.root = (Worker) ProActive.newActive(Worker.class.getName(),
                    null);
        } catch (ActiveObjectCreationException e) {
            throw new RuntimeException(e);
        } catch (NodeException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Problem[] init(Object[] params);

    public Result solve(Object[] params) {
        System.out.println("Solve Started");
        Problem[] problems = init(params);
        try {
            System.out.println("getPeers(" + problems.length + ")");
            root.getPeers(problems.length);
            Object[][] daugtherPbs = new Object[problems.length][2];
            for (int i = 0; i < problems.length; i++){
                daugtherPbs[i][0] = this.root;
                daugtherPbs[i][1] = problems[i];
            }
            System.out.println("createDaughtrt(" + daugtherPbs.length + ")");
            root.createDaughter(daugtherPbs);
        } catch (ProActiveException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        Result results = root.executeDaughter(params);
        ProActive.waitFor(results);
        System.out.println("Solve finished");
        return end(results);
    }

    public abstract Result end(Result result);
}
