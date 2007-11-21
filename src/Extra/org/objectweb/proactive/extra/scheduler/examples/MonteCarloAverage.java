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
package org.objectweb.proactive.extra.scheduler.examples;

import org.objectweb.proactive.extra.scheduler.common.task.JavaExecutable;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


public class MonteCarloAverage extends JavaExecutable {

    /** Serial version UID */
    private static final long serialVersionUID = -2762210298670871929L;

    public Object execute(TaskResult... results) throws Throwable {
        double avrg = 0;
        int count = 0;
        System.out.print("Parameters are : ");

        for (TaskResult res : results) {
            if (!res.hadException()) {
                System.out.print(res.value() + " ");
                avrg += ((Double) (res.value())).doubleValue();
                count++;
            }
        }

        Double result = new Double(avrg / count);
        System.out.println("Average is : " + result);

        return result;
    }
}
