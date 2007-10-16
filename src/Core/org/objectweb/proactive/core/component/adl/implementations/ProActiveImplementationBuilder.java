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
package org.objectweb.proactive.core.component.adl.implementations;

import java.util.Map;

import org.objectweb.fractal.adl.implementations.ImplementationBuilder;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;


/**
 * @author Matthieu Morel
 *
 */
public interface ProActiveImplementationBuilder extends ImplementationBuilder {

    /**
     * Allows the creation of a ProActive component on a given virtual node
     * @param type the type of the component
     * @param name the name of the component
     * @param definition the definition of the component
     * @param controllerDesc the description of the controller
     * @param contentDesc the description of the content
     * @param adlVN the virtual node where the component should be deployed
     * @param context context
     * @return an instance of the specified component (or a group of instances if the virtual node is
     * a multiple one)
     * @throws Exception if the creation of the component failed
     */
    public Object createComponent(Object type, String name, String definition,
        ControllerDescription controllerDesc, ContentDescription contentDesc,
        VirtualNode adlVN, Map context) throws Exception;
}
