/*
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 * 
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 * 
 * ################################################################
 */
package org.objectweb.proactive.examples.pi;

import java.math.BigDecimal;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;

public class PiUtil {

	static public Interval dividePI(int length, int scale) throws ClassNotReifiableException, ClassNotFoundException{

        int intervalSize = scale / length;
        Interval intervals = (Interval) ProActiveGroup.newGroup(Interval.class.getName());
        Group intervals_group = ProActiveGroup.getGroup(intervals);
        for (int i = 0; i < length; i++) {
            int beginning = i * intervalSize;
            int end = ((i == (length - 1)) ? scale
                                           : ((beginning + intervalSize) - 1));
            intervals_group.add(new Interval(beginning, end));
        }
        return intervals;
    }
	
	static public Result conquerPI(Result results){
		// get a group view on the results
        Group resultsGroup = ProActiveGroup.getGroup(results);
        
        // sum the results
        Result total = new Result(new BigDecimal(0), 0);
        
        for (int i = 0; i < resultsGroup.size(); i++) {
            total.addNumericalResult(((Result) resultsGroup.get(i)).getNumericalResult());
            total.addComputationTime(((Result) resultsGroup.get(i)).getComputationTime());
        }
		
        return total;
	}
}
