/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 *  Provides support for global and user spaces
 */
public class SchedulerSpacesSupport {
    public static final Logger logger = Logger.getLogger(SchedulingService.class);

    /** Scheduler's Global Space */
    private DataSpacesFileObject globalSpace;

    /** Map that link uniqueID to user global spaces */
    private final Map<String, DataSpacesFileObject> userGlobalSpaces;

    public SchedulerSpacesSupport() {
        this.userGlobalSpaces = new ConcurrentHashMap<>();

        // get Global Space
        try {
            this.globalSpace = PADataSpaces.resolveOutput(SchedulerConstants.GLOBALSPACE_NAME);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    /**
     * Returns a list of uris of the userspace that can be used by the specified user.
     * @param username the user's name
     * @return the list of uris of the userspace
     */
    public List<String> getUserSpaceURIs(String username) {
        DataSpacesFileObject o = this.userGlobalSpaces.get(username);
        if (o != null) {
            return o.getAllRealURIs();
        }
        return null;
    }

    /**
     * @return the list of uris of the globalspace
     */
    public List<String> getGlobalSpaceURIs() {
        return this.globalSpace.getAllRealURIs();
    }

    /**
     * @param username the user's name
     * @return the default userspace of an identified user
     */
    public DataSpacesFileObject getUserSpace(String username) {
        return this.userGlobalSpaces.get(username);
    }

    /**
     * @return the default globalspace
     */
    public DataSpacesFileObject getGlobalSpace() {
        return this.globalSpace;
    }

    /**
     * This method creates a dedicated USER space for the user which successfully connected
     * This USER space is a subspace of the scheduler default USER space,
     * A sub-folder named with the username is created to contain the USER space
     *
     * @param username the username of an identified user
     */
    public void registerUserSpace(String username) {
        if (this.userGlobalSpaces.get(username) == null) {
            DataSpacesFileObject userSpace;

            String userSpaceName = SchedulerConstants.USERSPACE_NAME + "_" + username;

            if (!PASchedulerProperties.DATASPACE_DEFAULTUSER_URL.isSet()) {
                logger.warn("URL of the root USER space is not set, cannot create a USER space for " +
                    username);
                return;
            }

            String localpath = PASchedulerProperties.DATASPACE_DEFAULTUSER_LOCALPATH.getValueAsStringOrNull();
            String hostname = PASchedulerProperties.DATASPACE_DEFAULTUSER_HOSTNAME.getValueAsStringOrNull();

            try {
                DataSpaceServiceStarter.getDataSpaceServiceStarter().createSpaceWithUserNameSubfolder(
                        username, SchedulerConstants.SCHEDULER_DATASPACE_APPLICATION_ID, userSpaceName,
                        PASchedulerProperties.DATASPACE_DEFAULTUSER_URL.getValueAsString(), localpath,
                        hostname, false, true);

                // immediately retrieve the User Space
                userSpace = PADataSpaces.resolveOutput(userSpaceName);
                logger.info("USER space for user " + username + " is at " + userSpace.getAllRealURIs());
            } catch (Exception e) {
                logger.warn("", e);
                return;
            }
            // register the user GlobalSpace to the frontend state
            this.userGlobalSpaces.put(username, userSpace);
        }
    }
}
