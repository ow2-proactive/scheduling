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
package org.objectweb.proactive.core.util;

import java.util.Vector;
import java.util.concurrent.ExecutorService;

import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;


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
    private Class<?>[] genericParameters;
    private Object[] constructorParameters;
    private ExecutorService threadpool;

    public NodeCreationListenerForAoCreation(Vector result, String className,
        Class<?>[] genericParameters, Object[] constructorParameters,
        ExecutorService threadpool) {
        this.result = result;
        this.className = className;
        this.genericParameters = genericParameters;
        this.constructorParameters = constructorParameters;
        this.threadpool = threadpool;
    }

    public void nodeCreated(NodeCreationEvent event) {
        threadpool.execute(new ProcessForAoCreation(this.result,
                this.className, this.genericParameters,
                this.constructorParameters, event.getNode()));
    }
}
