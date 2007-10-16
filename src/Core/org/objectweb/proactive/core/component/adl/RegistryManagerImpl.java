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
 * The implementation of a primitive component proposing the server interface {@link org.objectweb.proactive.core.component.adl.RegistryManager}.
 * It offers facilities for accessing a shared static registry for storing component instances according to
 * their name.
 *
 *
 * @author Matthieu Morel
 */
public class RegistryManagerImpl implements RegistryManager {
    Registry registry;

    public RegistryManagerImpl() {
        registry = Registry.instance();
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.adl.RegistryManager#addComponent(org.objectweb.fractal.api.Component)
     */
    public void addComponent(Component component) throws ADLException {
        registry.addComponent(component);
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.adl.RegistryManager#getComponent(java.lang.String)
     */
    public Component getComponent(String name) {
        return registry.getComponent(name);
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.adl.RegistryManager#clear()
     */
    public void clear() {
        registry.clear();
    }
}
