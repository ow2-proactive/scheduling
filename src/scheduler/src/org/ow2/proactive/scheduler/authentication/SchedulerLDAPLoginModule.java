/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.authentication;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.LDAPLoginModule;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * 
 * LDAP login module implementation for scheduling. Extracts LDAP configurations file from
 * resource manager configuration and uses it to authenticate users.
 *
 */
public class SchedulerLDAPLoginModule extends LDAPLoginModule {

    /**
     * Returns LDAP configuration file name defined in scheduler configuration file
     */
    protected String getLDAPConfigFileName() {
        String ldapFile = PASchedulerProperties.SCHEDULER_LDAP_CONFIG_FILE_PATH.getValueAsString();
        //test that ldap file path is an absolute path or not
        if (!(new File(ldapFile).isAbsolute())) {
            //file path is relative, so we complete the path with the scheduler home
            ldapFile = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + File.separator + ldapFile;
        }
        return ldapFile;
    }

    /**
     * Returns logger for authentication
     */
    public Logger getLogger() {
        return ProActiveLogger.getLogger(SchedulerLoggers.CONNECTION);
    }
}
