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
package org.objectweb.proactive.core.util;

import java.util.Vector;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.exceptions.body.BodyNonFunctionalException;
import org.objectweb.proactive.core.exceptions.body.NewActiveFailedNFE;
import org.objectweb.proactive.core.exceptions.manager.NFEManager;
import org.objectweb.proactive.core.group.AbstractProcessForGroup;
import org.objectweb.proactive.core.node.Node;


/**
 *
 * This class provides multithreading for the creation of active objects.
 *
 * @author Alexandre di Costanzo
 *
 * Created on Nov 8, 2005
 */
public class ProcessForAoCreation extends AbstractProcessForGroup
    implements Runnable {
    private Vector result;
    private String className;
    private Object[] param;
    private Node node;

    public ProcessForAoCreation(Vector result, String className,
        Object[] param, Node node) {
        this.result = result;
        this.className = className;
        this.param = param;
        this.node = node;
    }

    public void run() {
        try {
            this.result.add(ProActive.newActive(this.className, this.param,
                    this.node));
        } catch (Exception e) {
            BodyNonFunctionalException bnfe = new NewActiveFailedNFE("The activation failed",
                    e);
            NFEManager.fireNFE(bnfe, null);
        }
    }

    public int getMemberListSize() {
        return 1;
    }
}
