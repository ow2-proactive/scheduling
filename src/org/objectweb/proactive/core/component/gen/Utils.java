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
package org.objectweb.proactive.core.component.gen;


/**
 * Utility class for bytecode generation operations.
 *
 * @author Matthieu Morel
 *
 */
public class Utils {
    public static final String GENERATED_DEFAULT_PREFIX = "Generated_";
    public static final String REPRESENTATIVE_DEFAULT_SUFFIX = "_representative";
    public static final String COMPOSITE_REPRESENTATIVE_SUFFIX = "_composite";
    public static final String OUTPUT_INTERCEPTOR_SUFFIX = "_outputInterceptor";
    public static final String STUB_DEFAULT_PACKAGE = null;

    public static String convertClassNameToRepresentativeClassName(
        String classname) {
        if (classname.length() == 0) {
            return classname;
        }

        int n = classname.lastIndexOf('.');
        if (n == -1) {
            // no package
            return STUB_DEFAULT_PACKAGE + GENERATED_DEFAULT_PREFIX + classname;
        } else {
            return STUB_DEFAULT_PACKAGE + classname.substring(0, n + 1) +
            GENERATED_DEFAULT_PREFIX + classname.substring(n + 1);
        }
    }

    public static String getMetaObjectClassName(
        String functionalInterfaceName, String javaInterfaceName) {
        // just a way to have an identifier (possibly not unique ? ... but readable)
        return (GENERATED_DEFAULT_PREFIX + javaInterfaceName.replace('.', '_') +
        "_" + functionalInterfaceName.replace('.', '/').replace('-', '_'));
    }

    public static String getMetaObjectComponentRepresentativeClassName(
        String functionalInterfaceName, String javaInterfaceName) {
        // just a way to have an identifier (possibly not unique ... but readable)
        return (getMetaObjectClassName(functionalInterfaceName,
            javaInterfaceName) + REPRESENTATIVE_DEFAULT_SUFFIX);
    }

    public static String getOutputInterceptorClassName(
        String functionalInterfaceName, String javaInterfaceName) {
        // just a way to have an identifier (possibly not unique ... but readable)
        return (getMetaObjectClassName(functionalInterfaceName,
            javaInterfaceName) + OUTPUT_INTERCEPTOR_SUFFIX);
    }
}
