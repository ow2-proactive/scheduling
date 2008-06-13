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
package org.objectweb.proactive.core.group;

import java.util.concurrent.CountDownLatch;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;


/**
 * This class provides multithreading for the creation of active objects.
 *
 * @author The ProActive Team
 */
public class ProcessForGroupCreation extends AbstractProcessForGroup implements Runnable {
    private ProxyForGroup proxyGroup;
    private String className;
    private Class<?>[] genericParameters;
    private Object[] param;
    private Node node;
    private int index;
    private CountDownLatch doneSignal;

    public ProcessForGroupCreation(ProxyForGroup proxyGroup, String className, Class<?>[] genericParameters,
            Object[] param, Node node, int index, CountDownLatch doneSignal) {
        this.proxyGroup = proxyGroup;
        this.className = className;
        this.genericParameters = genericParameters;
        this.param = param;
        this.node = node;
        this.index = index;
        this.doneSignal = doneSignal;
    }

    public void run() {
        try {
            this.proxyGroup.set(this.index, PAActiveObject.newActive(className, genericParameters, param,
                    node));
        } catch (Exception e) {
            e.printStackTrace();
            // FIXME throw exception (using Callable task)
        }
        doneSignal.countDown();
    }

    @Override
    public int getMemberListSize() {
        return 1;
    }
}
