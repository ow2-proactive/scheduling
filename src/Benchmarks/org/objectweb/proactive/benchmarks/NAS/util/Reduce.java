/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.benchmarks.NAS.util;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.benchmarks.NAS.FT.WorkerFT;


public class Reduce implements Serializable {

    private Body body;
    private Complex sum_Complex, result_Complex;
    private int nbReceived;
    private int groupSize;

    public Reduce() {
    }

    public void init(WorkerFT workers) {
        body = PAActiveObject.getBodyOnThis();
        nbReceived = 0;
        this.groupSize = PAGroup.size(workers);
    }

    public Complex sumC(Complex value) {
        nbReceived++;

        if (sum_Complex == null) {
            sum_Complex = value;
        } else {
            sum_Complex.plusMe(value);
        }

        while (nbReceived < groupSize) {
            blockingServe();
            if (sum_Complex == null)
                break;
        }

        if (sum_Complex != null) {
            nbReceived = 0;
            result_Complex = sum_Complex;
            sum_Complex = null;
        }

        return result_Complex;
    }

    private final void blockingServe() {
        body.serve(body.getRequestQueue().blockingRemoveOldest());
    }

    public void msg(String str) {
        System.out.println("\t *R* --------> " + str);
    }
}