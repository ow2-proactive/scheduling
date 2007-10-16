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
package org.objectweb.proactive.core.security.securityentity;

import java.io.Serializable;
import java.security.cert.X509Certificate;


/**
 * @author acontes
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class EntityVirtualNode extends Entity implements Serializable {
    protected String virtualNodeName;

    public EntityVirtualNode() {
    }

    /**
     * @param name
     * @param application
     * @param certificate
     */

    /**
     * represent
     */
    public EntityVirtualNode(String name, X509Certificate application,
        X509Certificate certificate) {
        super();
        virtualNodeName = name;
        if (application == null) {
            System.out.println("APPPLICATION CERTIFICATE IS NULLL");
            throw new RuntimeException();
        }
        if (name == null) {
            System.out.println("APPPLICATION Virtual Node IS NULLL");
            throw new RuntimeException();
        }

        //  System.out.println("VIRTUAL NODE ENTITY CONSTRUCTOR " +
        //     application.getSubjectDN());
        this.applicationCertificate = application;
        //this.certificate = certificate;
    }

    //public EntityVirtualNode(String name) {
    //   super();
    //   virtualNodeName = name;
    //  }
    @Override
    public String getName() {
        return virtualNodeName;
    }

    @Override
    public String toString() {
        String s = null;
        s = virtualNodeName;
        s += (applicationCertificate.getSubjectDN().getName() + " |||| ");
        //s+= certificate.getSubjectDN().getName();
        return s;
    }

    @Override
    public boolean equals(Entity e) {
        if (e instanceof EntityVirtualNode) {
            if (applicationCertificate == null) {
                logger.debug("applicationCErtificate null");
                return false;
            }

            //		logger.debug (" OP " + virtualNodeName + " OP C " + applicationCertificate.getSubjectDN().getName());	
            if (((EntityVirtualNode) e).getApplicationCertificate() == null) {
                logger.debug("distant applicationCErtificate null");
                return false;
            }

            //		logger.debug (" DP " + ((EntityVirtualNode) e).virtualNodeName + "  DC " + ((EntityVirtualNode) e).applicationCertificate.getSubjectDN().getName());
            return this.virtualNodeName.equals(((EntityVirtualNode) e).virtualNodeName) &&
            e.getApplicationCertificate().equals(applicationCertificate);
        }
        return false;
    }
}
