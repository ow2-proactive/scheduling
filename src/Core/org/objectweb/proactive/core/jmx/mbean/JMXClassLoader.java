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
package org.objectweb.proactive.core.jmx.mbean;

/**
 * This class is used to add a class loader to the MBean Server repository.
 * See JMX Specification, version 1.4 ; Chap 8.4.1 : 'A class loader is added to the repository if it is registered as an MBean'.
 * @author ProActive Team
 */
public class JMXClassLoader extends ClassLoader implements JMXClassLoaderMBean {
    public JMXClassLoader() {

        /* Empty Constructor required by JMX */
    }

    /**
     * Creates a new IC2DClassLoader.
     * @param parent
     */
    public JMXClassLoader(ClassLoader parent) {
        super(parent);
    }
}
