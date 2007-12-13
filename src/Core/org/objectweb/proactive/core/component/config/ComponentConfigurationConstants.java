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
package org.objectweb.proactive.core.component.config;

/**
 * This interface defines String constants used in the xml component configuration file.
 *
 * @author Matthieu Morel
 *
 */
public interface ComponentConfigurationConstants {
    public static final String COMPONENT_CONFIGURATION_ELEMENT = "componentConfiguration";
    public static final String CONTROLLERS_ELEMENT = "controllers";
    public static final String CONTROLLER_ELEMENT = "controller";
    public static final String INTERFACE_ELEMENT = "interface";
    public static final String IMPLEMENTATION_ELEMENT = "implementation";
    public static final String INPUT_INTERCEPTOR_ATTRIBUTE = "input-interceptor";
    public static final String OUTPUT_INTERCEPTOR_ATTRIBUTE = "output-interceptor";
}
