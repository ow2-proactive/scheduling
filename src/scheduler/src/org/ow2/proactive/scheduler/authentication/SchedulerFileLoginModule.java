/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
import org.ow2.proactive.authentication.FileLoginModule;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * 
 * File login module implementation for scheduler. Extracts "login" and "group" files names from
 * scheduler configuration and uses them to authenticate users.
 *
 */
public class SchedulerFileLoginModule extends FileLoginModule {

    /**
     * Returns login file name from scheduler configuration file
     *
     * @return login file name from scheduler configuration file
     */
    @Override
    protected String getLoginFileName() {
        String loginFile = PASchedulerProperties.SCHEDULER_LOGIN_FILENAME.getValueAsString();
        //test that login file path is an absolute path or not
        if (!(new File(loginFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix RM_Home constant
            loginFile = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + File.separator + loginFile;
        }

        return loginFile;
    }

    /**
     * Returns group file name from scheduler configuration file
     *
     * @return group file name from scheduler configuration file
     */
    @Override
    protected String getGroupFileName() {
        String groupFile = PASchedulerProperties.SCHEDULER_GROUP_FILENAME.getValueAsString();
        //test that group file path is an absolute path or not
        if (!(new File(groupFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix RM_Home constant
            groupFile = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + File.separator + groupFile;
        }

        return groupFile;
    }

    /**
     * Returns logger used for authentication
     *
     * @return logger used for authentication
     */
    public Logger getLogger() {
        return ProActiveLogger.getLogger(SchedulerLoggers.CONNECTION);
    }

}