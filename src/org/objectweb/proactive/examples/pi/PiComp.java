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

import org.objectweb.proactive.examples.pi.Interval;
import org.objectweb.proactive.examples.pi.Result;

/**
 * This interface has to be implemented by all workers who will perform pi computation
 * @author ProActive team
 *
 */
public interface PiComp {
    /**
     * Computes a part of the final pi value
     * @param msg The interval on which the computation has to be done
     * @return the result for the computation of his interval
     */
    public Result compute(Interval msg);
    /**
     * Sets the scale for BigDecimal numbers involved in the computation
     * @param scale The scale to set
     */
    public void setScale(Integer scale);
}
