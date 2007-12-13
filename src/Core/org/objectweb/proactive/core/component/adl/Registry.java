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

import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is a static registry used for storing component instances according to their name.
 *
 * @author Matthieu Morel
 */
public class Registry {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);
    static private Registry instance = null;
    private Hashtable table;

    private Registry() {
        table = new Hashtable();
    }

    /**
     * Returns a single instance
     * @return the unique instance in the vm
     */
    static public Registry instance() {
        if (instance == null) {
            instance = new Registry();
        }
        return instance;
    }

    /**
     * see @link org.objectweb.fractal.adl.RegistryManager#addComponent(org.objectweb.fractal.api.Component)
     */
    public void addComponent(Component component) throws ADLException {
        if (PAGroup.isGroup(component)) {
            Group group = PAGroup.getGroup(component);
            Iterator it = group.iterator();
            while (it.hasNext()) {
                addComponent((Component) it.next());
            }
        } else {
            try {
                String name = Fractal.getNameController(component).getFcName();
                if (table.containsKey(name)) {
                    throw new ADLException("A component with the name " + name +
                        " is already stored in the registry", null);
                }
                table.put(name, component);
                if (logger.isDebugEnabled()) {
                    logger.debug("added component " + name + " to the local registry");
                }
            } catch (NoSuchInterfaceException e) {
                throw new ADLException(
                    "It is not possible to register a component without a NameController controller", null);
            }
        }
    }

    /**
     * see @link org.objectweb.fractal.adl.RegistryManager#getComponent(java.lang.String)
     */
    public Component getComponent(String name) {
        return (Component) table.get(name);
    }

    /**
     * empties the registry
     *
     */
    public void clear() {
        table.clear();
    }
}
