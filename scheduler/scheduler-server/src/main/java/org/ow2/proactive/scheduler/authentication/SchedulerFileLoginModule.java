/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.authentication;

import java.io.File;
import java.security.KeyException;
import java.security.PrivateKey;

import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.FileLoginModule;
import org.ow2.proactive.authentication.crypto.Credentials;
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
            loginFile = PASchedulerProperties.getAbsolutePath(loginFile);
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
            groupFile = PASchedulerProperties.getAbsolutePath(groupFile);
        }

        return groupFile;
    }

    @Override
    protected PrivateKey getPrivateKey() throws KeyException {
        return Credentials.getPrivateKey(PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_AUTH_PRIVKEY_PATH.getValueAsString()));
    }

    /**
     * Returns logger used for authentication
     *
     * @return logger used for authentication
     */
    public Logger getLogger() {
        return Logger.getLogger(SchedulerFileLoginModule.class);
    }

}
