/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component;


/**
 * Constant strings used throughout the components implementation
 *
 * @author Matthieu Morel
 */
public interface Constants {
    public final static String CONTENT_CONTROLLER = "content-controller";
    public final static String LIFECYCLE_CONTROLLER = "lifecycle-controller";
    public final static String BINDING_CONTROLLER = "binding-controller";
    public final static String COMPONENT_PARAMETERS_CONTROLLER = "component-parameters-controller";
    public final static String NAME_CONTROLLER = "name-controller";
    public final static String ATTRIBUTE_CONTROLLER = "attribute-controller";
    public final static String SUPER_CONTROLLER = "super-controller";
    public final static String COMPONENT = "component";
    public final static String CYCLIC_NODE_SUFFIX = "-cyclicInstanceNumber-";

    // hierarchical types of component
    public final static String COMPOSITE = "composite";
    public final static String PRIMITIVE = "primitive";
    public final static String PARALLEL = "parallel";

    public final static boolean SYNCHRONOUS = true;
}
