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

import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;
import org.objectweb.proactive.core.group.threadpool.ThreadPool;


/**
 * Creates an Active Object by the multi-tread pool when a node is created.
 *
 * @author Alexandre di Costanzo
 *
 * Created on Nov 8, 2005
 */
public class NodeCreationListenerForAoCreation
    implements NodeCreationEventListener {
    private Vector result;
    private String className;
    private Object[] constructorParameters;
    private ThreadPool threadpool;

    public NodeCreationListenerForAoCreation(Vector result, String className,
        Object[] constructorParameters, ThreadPool threadpool) {
        this.result = result;
        this.className = className;
        this.constructorParameters = constructorParameters;
        this.threadpool = threadpool;
    }

    public void nodeCreated(NodeCreationEvent event) {
        threadpool.addAJob(new ProcessForAoCreation(this.result,
                this.className, this.constructorParameters, event.getNode()));
    }
}
