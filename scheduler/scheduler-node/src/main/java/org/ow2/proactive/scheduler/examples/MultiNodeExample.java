/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.examples;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * Multi-nodes Example.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 *
 */
public class MultiNodeExample extends JavaExecutable {
    //default value : 5003
    //if argument is set in descriptor, automatic assignment will be perform
    //as it is a primitive type and the name of the field is the same as the argument
    private int numberToFind = 5003;

    @Override
    public Serializable execute(TaskResult... results) {
        System.out.println("Multi-node started !!");

        ArrayList<Node> nodes = getNodes();

        // create workers (on local node)
        Vector<Worker> workers = new Vector<Worker>();

        for (Node node : nodes) {
            try {
                Worker w = (Worker) PAActiveObject.newActive(Worker.class.getName(), new Object[] {}, node);
                workers.add(w);
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            } catch (NodeException e) {
                e.printStackTrace();
            }
        }

        // create controller
        Controller controller = new Controller(workers);
        int result = controller.findNthPrimeNumber(numberToFind);

        System.out.println("last prime : " + result);

        return result;
    }

    private class Controller {
        // Managed workers
        private Vector<Worker> workers;

        /**
         * Create a new instance of Controller.
         *
         * @param workers
         */
        public Controller(Vector<Worker> workers) {
            this.workers = workers;
        }

        // start computation
        /**
         * Find the Nth prime number.
         *
         * @param nth the prime number to find
         * @return the Nth prime number.
         */
        public int findNthPrimeNumber(int nth) {
            long startTime = System.currentTimeMillis();
            BooleanWrapper flase = new BooleanWrapper(false);
            int found = 0;
            int n = 2;

            while (found < nth) {
                Vector<BooleanWrapper> answers = new Vector<BooleanWrapper>();

                // send requests
                for (Worker worker : workers) {
                    BooleanWrapper resp = worker.isPrime(n);
                    answers.add(resp);
                }

                PAFuture.waitForAll(answers);

                if (!answers.contains(flase)) {
                    workers.get(found % workers.size()).addPrimeNumber(n);
                    System.out.println("--->" + n);
                    found++;
                }

                n++;
            }

            long stopTime = System.currentTimeMillis();
            System.out.println("Total time (ms) " + (stopTime - startTime));

            return n - 1;
        }
    }
}
