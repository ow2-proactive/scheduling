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
package org.objectweb.proactive.core.descriptor.services;

import java.util.Map;

import org.objectweb.proactive.core.node.Node;


/**
 * <p>Interface to implement for defining  a Technical Service.</p>
 * <b>Definition of Technical Service:</b>
 * <p>A Technical Service is a non-functional requirement that may be dynamically
 * fulfilled at runtime by updating the configuration of selected resources (here a
 * ProActive Node).</p>
 * @author The ProActive Team
 *
 */
public interface TechnicalService {

    /**
     * Initialize the Technical Service with its argument values.
     * @param argValues values of the Technical Service arguments.
     */
    public abstract void init(Map argValues);

    /**
     * Initialize the given node with the Technical Service.
     * @param node the node where to apply the Technical Service.
     */
    public abstract void apply(Node node);
}
