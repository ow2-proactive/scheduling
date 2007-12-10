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

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;


/**
 *
 * This class provides multithreading for the creation of active objects.
 *
 * @author Alexandre di Costanzo
 *
 * Created on Nov 8, 2005
 */
public class ProcessForAoCreation implements Runnable {
    private Vector result;
    private String className;
    private Class<?>[] genericParameters;
    private Object[] param;
    private Node node;

    public ProcessForAoCreation(Vector result, String className,
        Class<?>[] genericParameters, Object[] param, Node node) {
        this.result = result;
        this.className = className;
        this.genericParameters = genericParameters;
        this.param = param;
        this.node = node;
    }

    public void run() {
        try {
            this.result.add(PAActiveObject.newActive(this.className,
                    this.genericParameters, this.param, this.node));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
