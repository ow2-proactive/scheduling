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
package functionalTests.group.dynamicdispatch;

import junit.framework.Assert;

import org.objectweb.proactive.core.group.DispatchMode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTest;
import functionalTests.descriptor.defaultnodes.TestNodes;


// dispatch n tasks between 2 workers with n>2
// task 1 on worker 1 sleeps for a while
// --> check that worker 2 processed n-1 tasks
public class Test extends FunctionalTest {
    int nbTasks = 10;

    @org.junit.Test
    public void action() throws Exception {
        TestNodes tn = new TestNodes();
        tn.action();

        Object[][] params = { { 0 }, { 1 } };

        Node[] nodes = { TestNodes.getSameVMNode(), TestNodes.getLocalVMNode(), TestNodes.getRemoteVMNode() };

        Task tasks = (Task) ProActiveGroup.newGroup(Task.class.getName());
        Group<Task> taskGroup = ProActiveGroup.getGroup(tasks);
        for (int i = 0; i < nbTasks; i++) {
            taskGroup.add(new Task(i));
        }

        Worker workers = (Worker) ProActiveGroup.newGroup(Worker.class.getName(), params, nodes);

        //ProActiveGroup.setScatterGroup(taskGroup); // grrr...: this simply does NOT work! 
        ProActiveGroup.setScatterGroup(tasks); // we have to use the TYPED group...
        ProActiveGroup.setDispatchMode(workers, DispatchMode.DYNAMIC, 1);

        Task results = workers.executeTask(tasks);
        validateDynamicallyDispatchedTasks(results);
        //		

        // test with annotation
        Worker workers2 = (Worker) ProActiveGroup.newGroup(Worker.class.getName(), params, nodes);

        // do not use api for setting balancing mode

        tasks = (Task) ProActiveGroup.newGroup(Task.class.getName());
        taskGroup = ProActiveGroup.getGroup(tasks);
        for (int i = 0; i < nbTasks; i++) {
            taskGroup.add(new Task(i));
        }
        ProActiveGroup.setScatterGroup(tasks); // we have to use the TYPED group...
        results = workers2.executeDynamically(tasks);
        validateDynamicallyDispatchedTasks(results);

    }

    private void validateDynamicallyDispatchedTasks(Task results) {
        Group<Task> resultGroup = ProActiveGroup.getGroup(results);

        Assert.assertTrue(resultGroup.size() == nbTasks);

        ProActiveGroup.waitAll(results);
        int nbTasksForWorker0 = 0;
        int nbTasksForWorker1 = 0;
        for (int i = 0; i < nbTasks; i++) {
            if (resultGroup.get(i).getExecutionWorker() == 0) {
                nbTasksForWorker0++;
            } else if (resultGroup.get(i).getExecutionWorker() == 1) {
                nbTasksForWorker1++;
            }
        }
        System.out.println("worker 0: " + nbTasksForWorker0);
        System.out.println("worker 1: " + nbTasksForWorker1);
        Assert.assertTrue(nbTasksForWorker0 == 1);
        Assert.assertTrue(nbTasksForWorker1 == (nbTasks - 1));
    }

}
