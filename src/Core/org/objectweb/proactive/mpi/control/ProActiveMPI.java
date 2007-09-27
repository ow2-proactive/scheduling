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
package org.objectweb.proactive.mpi.control;

import java.util.ArrayList;
import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.NodeException;


public class ProActiveMPI {
    private static ProActiveMPIManager manager;

    public static Vector deploy(ArrayList spmdList) {
        if (manager == null) {
            // create manager
            try {
                manager = (ProActiveMPIManager) ProActiveObject.newActive(ProActiveMPIManager.class.getName(),
                        new Object[] {  });
                //  VectorResult vres = 
                manager.deploy(spmdList);
                return null;
                // get a future and wait on future
                // System.out.println("[PROACTIVEMPI] RETURNS VECTOR OF FUTURES ");
                //  return vres.getVectorResult();
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            } catch (NodeException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException(
                " ERROR: Application has already been deployed once !!!!!!!");
        }

        return null;
    }
}
