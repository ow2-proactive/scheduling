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
package org.objectweb.proactive.core.component.controller;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.Type;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;


/**
 * This interface defines some basic services offered by a component controller in the ProActive implementation of the Fractal model.
 *
 * @author The ProActive Team
 *
 */
@PublicAPI
public interface ProActiveController extends Interface {

    /*
     * see {@link org.objectweb.fractal.api.Interface#getFcItfOwner()}
     */
    public abstract Component getFcItfOwner();

    /*
     * see {@link org.objectweb.fractal.api.Interface#isFcInternalItf()}
     */
    public abstract boolean isFcInternalItf();

    /*
     * see {@link org.objectweb.fractal.api.Interface#getFcItfName()}
     */
    public abstract String getFcItfName();

    /*
     * see {@link org.objectweb.fractal.api.Interface#getFcItfType()}
     */
    public abstract Type getFcItfType();

    /**
     * Called after creation of all controllers and interfaces
     */
    public void init();

    /**
     * If a controller holds references to active objects which are dependent on it, it needs to
     * trigger the migration of these active objects. This is done by overriding this method.
     * @param node
     * @throws MigrationException
     */
    public void migrateDependentActiveObjectsTo(Node node) throws MigrationException;
}
