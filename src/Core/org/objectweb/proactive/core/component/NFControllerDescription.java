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
package org.objectweb.proactive.core.component;

import org.objectweb.proactive.core.component.identity.ProActiveComponentImpl;


/**
 * Fractal implementation-specific description of the controllers of components.
 * It is currently used to specify the hierarchical type and the name of the
 * components. <br>
 * <p>
 * It is also a place to specify custom controllers for a given component ; the
 * configuration of the controllers is described in a properties file whose
 * location can be given as a parameter. <br>
 * The controllers configuration file is simple : it associates the signature of
 * a controller interface with the implementation that has to be used. <br>
 * During the construction of the component, the membrane is automatically
 * constructed with these controllers. The controllers are linked together, and
 * requests targetting a control interface visit the different controllers until
 * they find the suitable controller, and then the request is executed on this
 * controller.
 *
 * @author Paul Naoumenko
 */
public class NFControllerDescription extends ControllerDescription {
    public static final String DEFAULT_NFCOMPONENT_CONFIG_FILE_LOCATION = "/org/objectweb/proactive/core/component/config/default-nfcomponent-config.xml";

    /**
     * a no-arg constructor (used in the ProActive parser)
     *
     */
    public NFControllerDescription() {
        this((String) null, (String) null, (String) null, false);
    }

    /**
     * constructor
     * @param name the name of the component
     * @param hierarchicalType the hierachical type of the component. See {@link Constants}
     */
    public NFControllerDescription(String name, String hierarchicalType) {
        this(name, hierarchicalType, null, false);
    }

    /**
     * Constructor for ControllerDescription.
     * @param name String
     * @param hierarchicalType String
     * @param synchronous boolean
     */
    public NFControllerDescription(String name, String hierarchicalType,
        boolean synchronous) {
        this(name, hierarchicalType, null, synchronous);
    }

    /**
     * Constructor for ControllerDescription.
     * @param name String
     * @param hierarchicalType String
     * @param controllersConfigFileLocation String
     */
    public NFControllerDescription(String name, String hierarchicalType,
        String controllersConfigFileLocation) {
        this(name, hierarchicalType, controllersConfigFileLocation, false);
    }

    /**
     * Constructor for ControllerDescription.
     * @param name String
     * @param hierarchicalType String
     * @param controllersConfigFileLocation String
     * @param interceptorsConfigFileLocation String
     */
    public NFControllerDescription(String name, String hierarchicalType,
        String controllersConfigFileLocation,
        String interceptorsConfigFileLocation) {
        this(name, hierarchicalType, controllersConfigFileLocation, false);
    }

    /**
     * Constructor for ControllerDescription.
     * @param name String
     * @param hierarchicalType String
     * @param controllersConfigFileLocation String
     * @param synchronous boolean
     */
    public NFControllerDescription(String name, String hierarchicalType,
        String controllersConfigFileLocation, boolean synchronous) {
        this.hierarchicalType = hierarchicalType;
        this.name = name;
        if (!Constants.PRIMITIVE.equals(hierarchicalType)) {
            this.synchronous = synchronous;
        }
        if (controllersConfigFileLocation == null) {
            this.controllersConfigFileLocation = DEFAULT_NFCOMPONENT_CONFIG_FILE_LOCATION;
        } else {
            this.controllersConfigFileLocation = controllersConfigFileLocation;
        }
        controllersSignatures = ProActiveComponentImpl.loadControllerConfiguration(this.controllersConfigFileLocation)
                                                      .getControllers();
    }

    /**
     * copy constructor (clones the object)
     * @param controllerDesc the ControllerDescription to copy.
     */
    public NFControllerDescription(NFControllerDescription controllerDesc) {
        this(controllerDesc.name, controllerDesc.hierarchicalType,
            controllerDesc.controllersConfigFileLocation,
            controllerDesc.synchronous);
    }
}
