/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.utils.console;

import java.util.ArrayList;
import java.util.List;


/**
 * JVMPropertiesPreloader is used to parse arguments and set JVM properties.
 * Use this class if your arguments line contains java properties (ie : -Dname=value)
 * This properties will be set to JVM (this cause the existing one to be overridden.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public final class JVMPropertiesPreloader {

    /**
     * Get the argument line, parse it and override JVM properties with the specified one.
     * This method returns a copy of the args line without the JVM properties notation arguments.
     *
     * @param args the arguments line to be parsed (containing JVM properties notation arguments)
     */
    public static String[] overrideJVMProperties(String[] args) {
        List<String> argsToReturn = new ArrayList<String>();
        for (String arg : args) {
            if (arg.matches("^-D.+=.+$")) {
                setPropertyWithValue(arg);
            } else if (arg.matches("^-D.+$")) {
                setEmptyProperty(arg);
            } else {
                argsToReturn.add(arg);
            }
        }
        return argsToReturn.toArray(new String[] {});
    }

    /**
     * Set the parsed argument and value to the JVM properties.
     *
     * @param argument the argument to add (must be -Dname=value)
     */
    private static void setPropertyWithValue(String argument) {
        String[] split = argument.substring(2).split("=");
        System.setProperty(split[0], split[1]);
    }

    /**
     * Set the parsed argument with an empty string value to the JVM properties.
     *
     * @param argument the argument to add (must be -Dname)
     */
    private static void setEmptyProperty(String argument) {
        System.setProperty(argument.substring(2), "");
    }

}
