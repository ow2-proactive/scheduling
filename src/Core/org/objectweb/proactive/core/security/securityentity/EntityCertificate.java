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
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EntityCertificate extends Entity implements Serializable {

    /**
     *
     */
    public EntityCertificate(X509Certificate applicationCertificate,
        X509Certificate certificate) {
        super();
        this.applicationCertificate = applicationCertificate;
        this.certificate = certificate;
    }

    @Override
    public String getName() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.ext.security.Entity#equals(org.objectweb.proactive.ext.security.Entity)
     */
    @Override
    public boolean equals(Entity e) {
        return e.getCertificate().equals(certificate);
    }
}
