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
package functionalTests.masterworker;

import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;


public class A implements Task<Integer> {

    /**
     *
     */
    private int taskcounter;
    private long delay;
    private boolean exception;

    public A(int counter) {
        this(counter, 0, false);
    }

    public A(int counter, long delay, boolean exception) {
        this.taskcounter = counter;
        this.delay = delay;
        this.exception = exception;
    }

    public Integer run(WorkerMemory memory) throws Exception {
        if (exception) {
            int a = 1 / 0;
            System.out.println(a);
        }

        if (delay > 0) {
            Thread.sleep(delay);
        }

        return taskcounter;
    }
}
