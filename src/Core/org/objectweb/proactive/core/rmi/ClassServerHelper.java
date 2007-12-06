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
package org.objectweb.proactive.core.rmi;

import org.objectweb.proactive.core.config.ProProperties;
import org.objectweb.proactive.core.ssh.SshParameters;


public class ClassServerHelper {

    /**
     * settings of the ClassServer
     */
    protected ClassServer currentClassServer;
    protected String classpath;
    protected boolean shouldCreateClassServer = true;

    //
    // -- Constructors -----------------------------------------------
    //
    public ClassServerHelper() {
        if ((System.getSecurityManager() == null) &&
                ProProperties.PA_SECURITYMANAGER.isTrue()) {
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        }
        if (ProProperties.PA_HTTP_SERVLET.isTrue()) {
            this.shouldCreateClassServer = false;
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String v) {
        classpath = v;
    }

    public int getClassServerPortNumber() {
        if (currentClassServer == null) {
            return -1;
        }
        return ClassServer.getServerSocketPort();
    }

    public boolean shouldCreateClassServer() {
        return shouldCreateClassServer;
    }

    public void setShouldCreateClassServer(boolean v) {
        shouldCreateClassServer = v;
    }

    public synchronized String initializeClassServer()
        throws java.io.IOException {
        if (ProProperties.PA_HTTP_SERVLET.isTrue()) {
            return this.getCodebase();
        }

        if (!shouldCreateClassServer) {
            return null; // don't bother
        }
        if (currentClassServer != null) {
            return this.getCodebase(); // already created for this VM
        }
        if (classpath == null) {
            currentClassServer = new ClassServer();
        } else {
            currentClassServer = new ClassServer(classpath);
        }
        String codebase = this.getCodebase();

        //        System.setProperty("java.rmi.server.codebase", codebase);
        return codebase;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    // 
    private String getCodebase() {
        String codebase;
        if (SshParameters.getSshTunneling()) {
            codebase = "httpssh://" + currentClassServer.getHostname() + ":" +
                ClassServer.getServerSocketPort() + "/";
        } else if (ProProperties.PA_HTTP_SERVLET.isTrue()) {
            codebase = ClassServerServlet.getUrl() + "doc";
        } else {
            codebase = "http://" + currentClassServer.getHostname() + ":" +
                ClassServer.getServerSocketPort() + "/";
        }

        return codebase;
    }
}
