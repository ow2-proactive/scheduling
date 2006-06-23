/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.ext.security.domain;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.ProActiveSecurity;


/**
 * @author acontes
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class StartDomain {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SECURITY_DOMAIN);
    private String securityFileLocation = null;
    private String domainName = null;
    private SecurityDomain domain = null;

    protected StartDomain() {
    }

    private StartDomain(String[] args) {
        domainName = args[0];
        securityFileLocation = args[1];
    }

    public static void main(String[] args) {
        ProActiveSecurity.loadProvider();

        if (args.length != 2) {
            System.out.println(
                "Usage StartDomain <domain name> <path to security file>");
            System.exit(-1);
        }

        ProActiveConfiguration.load();

        new StartDomain(args).run();
    }

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
     * Runs the complete creation and registration of a ProActiveRuntime and creates a
     * node once the creation is completed.
     */
    private void run() {
        //Domain domain = ProActive.newActive
    }
}
