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
package org.objectweb.proactive.examples.pi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;


/**
 * This class contains method that help to prepare the pi computation
 * @author ProActive team
 *
 */
public class PiUtil {

    /**
     * Determines the intervals to send to diffent workers depending on the number of "pi engines" and the number of decimals to compute
     * @param length The number "pi engines"
     * @param scale The number of decimals
     * @return A group of intervals which will be sent to workers
     * @throws ClassNotReifiableException
     * @throws ClassNotFoundException
     */
    static public Interval dividePI(int length, int scale)
        throws ClassNotReifiableException, ClassNotFoundException {
        int intervalSize = scale / length;
        Interval intervals = (Interval) ProGroup.newGroup(Interval.class.getName());
        Group intervals_group = ProGroup.getGroup(intervals);
        for (int i = 0; i < length; i++) {
            int beginning = i * intervalSize;
            int end = ((i == (length - 1)) ? scale
                                           : ((beginning + intervalSize) - 1));
            intervals_group.add(new Interval(beginning, end));
        }
        return intervals;
    }

    /**
     * Determines the value of pi by gathering results of pi computation on several intervals
     * @param results A group of results, coming from workers which have finished computation  on their intervals
     * @return the pi value
     */
    static public Result conquerPI(Result results) {
        // get a group view on the results
        Group resultsGroup = ProGroup.getGroup(results);

        // sum the results
        Result total = new Result(new BigDecimal(0), 0);

        for (int i = 0; i < resultsGroup.size(); i++) {
            total.addNumericalResult(((Result) resultsGroup.get(i)).getNumericalResult());
            total.addComputationTime(((Result) resultsGroup.get(i)).getComputationTime());
        }

        return total;
    }

    /**
     * Determines the intervals to send to diffent workers depending on the number of "pi engines" and the number of decimals to compute
     * @param length The number "pi engines"
     * @param scale The number of decimals
     * @return A list of intervals which will be sent to workers
     * @throws ClassNotReifiableException
     * @throws ClassNotFoundException
     */
    static public List<Interval> dividePIList(int length, int scale)
        throws ClassNotReifiableException, ClassNotFoundException {
        int intervalSize = scale / length;

        List<Interval> intervals = new ArrayList<Interval>();

        for (int i = 0; i < length; i++) {
            int beginning = i * intervalSize;
            int end = ((i == (length - 1)) ? scale
                                           : ((beginning + intervalSize) - 1));
            intervals.add(new Interval(beginning, end));
        }
        return intervals;
    }

    /**
     * Determines the value of pi by gathering results of pi computation on several intervals
     * @param results A list of results, coming from workers which have finished computation  on their intervals
     * @return the pi value
     */
    static public Result conquerPIList(List<Result> results) {
        Result total = new Result(new BigDecimal(0), 0);

        for (int i = 0; i < results.size(); i++) {
            total.addNumericalResult(((Result) results.get(i)).getNumericalResult());
            total.addComputationTime(((Result) results.get(i)).getComputationTime());
        }

        return total;
    }
}
