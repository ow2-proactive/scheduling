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
package org.objectweb.proactive.core.component.adl;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;


/**
 * This interface defines facilities for using a shared static registry storing component
 * instances according to their name.
 *
 * @author Matthieu Morel
 */
public interface RegistryManager {

    /**
     * Adds a component instance.
     * (The name is retreived automatically from the NameController)
     *
     * @param component the instance of the component
     * @throws ADLException
     */
    public void addComponent(Component component) throws ADLException;

    /**
     * Retreives an instance of a  component according to the name (from its NameController controller)
     * @param name the name of the instance of the component
     * @return the selected component
     */
    public Component getComponent(String name);

    /**
     * Empties the registry
     *
     */
    public void clear();
}
