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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;


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
 * @author Matthieu Morel
 */
public class ControllerDescription implements Serializable {
    private String hierarchicalType;
    private String name;
    private boolean synchronous = false;
    public static final String DEFAULT_COMPONENT_CONFIG_FILE_LOCATION = "/org/objectweb/proactive/core/component/config/default-component-config.xml";
    private File controllersConfigFile;

    /**
     * a no-arg constructor (used in the ProActive parser)
     *
     */
    public ControllerDescription() {
        this((String) null, (String) null, (String) null, false);
    }

    /**
     * constructor
     * @param name the name of the component
     * @param hierarchicalType the hierachical type of the component. See {@link Constants}
     */
    public ControllerDescription(String name, String hierarchicalType) {
        this(name, hierarchicalType, null, false);
    }

    public ControllerDescription(String name, String hierarchicalType,
        boolean synchronous) {
        this(name, hierarchicalType, null, synchronous);
    }

    public ControllerDescription(String name, String hierarchicalType,
        String controllersConfigFileLocation) {
        this(name, hierarchicalType, controllersConfigFileLocation, false);
    }

    public ControllerDescription(String name, String hierarchicalType,
        String controllersConfigFileLocation,
        String interceptorsConfigFileLocation) {
        this(name, hierarchicalType, controllersConfigFileLocation, false);
    }

    public ControllerDescription(String name, String hierarchicalType,
        String controllersConfigFileLocation, boolean synchronous) {
        this.hierarchicalType = hierarchicalType;
        this.name = name;
        if (!Constants.PRIMITIVE.equals(hierarchicalType)) {
            this.synchronous = synchronous;
        }
        if (controllersConfigFileLocation != null) {
            controllersConfigFile = new File(controllersConfigFileLocation);
        } else {
            controllersConfigFile = new File(DEFAULT_COMPONENT_CONFIG_FILE_LOCATION);
        }
    }

    /**
     * copy constructor (clones the object)
     * @param controllerDesc the ControllerDescription to copy.
     */
    public ControllerDescription(ControllerDescription controllerDesc) {
        hierarchicalType = new String(controllerDesc.getHierarchicalType());
        name = new String(controllerDesc.getName());
    }

    /**
     * Returns the hierarchicalType.
     * @return String
     */
    public String getHierarchicalType() {
        return hierarchicalType;
    }

    /**
     * setter for hierarchical type
     * @param string hierarchical type. See {@link Constants}
     */
    public void setHierarchicalType(String string) {
        hierarchicalType = string;
    }

    /**
     * getter for the name
     * @return the name of the component
     */
    public String getName() {
        return name;
    }

    /**
     * setter for the name
     * @param name name of the component
     */
    public void setName(String name) {
        this.name = name;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public File getControllersConfigFile() {
        return controllersConfigFile;
    }
}
