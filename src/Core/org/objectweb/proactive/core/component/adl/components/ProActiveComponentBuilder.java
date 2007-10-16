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
package org.objectweb.proactive.core.component.adl.components;

import org.objectweb.fractal.adl.components.ComponentBuilder;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;


/**
 * A ProActive based implementation of the {@link ComponentBuilder} interface.
 * This implementation uses the Fractal API to add and start components.
 * It slightly differs from the FractalComponentBuilder class : the name of the component
 * is not specified in this addition operation, but when the component is instantiated.
 *
 */
public class ProActiveComponentBuilder implements ComponentBuilder {
    // --------------------------------------------------------------------------
    // Implementation of the ComponentBuilder interface
    // --------------------------------------------------------------------------
    public void addComponent(final Object superComponent,
        final Object subComponent, final String name, final Object context)
        throws Exception {
        Fractal.getContentController((Component) superComponent)
               .addFcSubComponent((Component) subComponent);
        // as opposed  to the standard fractal implementation, we do not set
        // the name of the component here because :
        // 1. it is already name at instantiation time
        // 2. it could be a group of components, and we do not want to give the 
        // same name to all the elements of the group
        //    try {
        //      Fractal.getNameController((Component)subComponent).setFcName(name);
        //    } catch (NoSuchInterfaceException ignored) {
        //    }
    }

    public void startComponent(final Object component, final Object context)
        throws Exception {
    }
}
