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
package org.ow2.proactive.scheduler.core;

import java.security.KeyException;
import java.security.PrivateKey;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.api.UserCredentials;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 *  Provides support for global and user spaces
 */
public class SchedulerSpacesSupport {
    public static final Logger logger = Logger.getLogger(SchedulingService.class);

    /** Scheduler's Global Space */
    private DataSpacesFileObject globalSpace;

    private static volatile Set<String> internalUsers;

    /** Map that link uniqueID to user global spaces */
    private final Map<String, DataSpacesFileObject> userGlobalSpaces;

    private static PrivateKey corePrivateKey;

    public SchedulerSpacesSupport() {
        this.userGlobalSpaces = new ConcurrentHashMap<>();

        // get Global Space
        try {
            this.globalSpace = PADataSpaces.resolveOutput(SchedulerConstants.GLOBALSPACE_NAME);
        } catch (Exception e) {
            logger.error("", e);
        }

        initPrivateKey();
        initInternalAccounts();
    }

    private static synchronized void initPrivateKey() {
        if (corePrivateKey == null) {
            try {
                corePrivateKey = Credentials.getPrivateKey(PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_AUTH_PRIVKEY_PATH.getValueAsString()));
            } catch (KeyException e) {
                logger.error("Could not initialize private key", e);
            }
        }
    }

    private static synchronized void initInternalAccounts() {
        if (internalUsers == null) {
            try {
                internalUsers = new HashSet<>(PASchedulerProperties.DATASPACE_DEFAULTUSER_INTERNAL_ACCOUNTS.getValueAsList(","));
            } catch (Exception e) {
                logger.error("Invalid value of " +
                             PASchedulerProperties.DATASPACE_DEFAULTUSER_INTERNAL_ACCOUNTS.getKey() + " " +
                             PASchedulerProperties.DATASPACE_DEFAULTUSER_INTERNAL_ACCOUNTS.getValueAsStringOrNull());
            }
        }
    }

    /**
     * Returns a list of uris of the userspace that can be used by the specified user.
     * @param username the user's name
     * @return the list of uris of the userspace
     */
    public List<String> getUserSpaceURIs(String username, Credentials credentials) {
        DataSpacesFileObject o = getUserSpace(username, credentials);
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
    public DataSpacesFileObject getUserSpace(String username, Credentials userCredentials) {
        Validate.notBlank(username);
        registerUserSpace(username, userCredentials);
        if (this.userGlobalSpaces.containsKey(username)) {
            return this.userGlobalSpaces.get(username);
        } else {
            return this.globalSpace;
        }

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
     * @param credentials credentials of the user
     */
    public void registerUserSpace(String username, Credentials credentials) {
        if (this.userGlobalSpaces.get(username) == null && shouldRegisterUserSpace(username)) {
            synchronized (this) {
                DataSpacesFileObject userSpace;

                String userSpaceName = SchedulerConstants.USERSPACE_NAME + "_" + username;

                if (!PASchedulerProperties.DATASPACE_DEFAULTUSER_URL.isSet()) {
                    logger.warn("URL of the root USER space is not set, cannot create a USER space for " + username);
                    return;
                }

                String localpath = PASchedulerProperties.DATASPACE_DEFAULTUSER_LOCALPATH.getValueAsStringOrNull();
                String hostname = PASchedulerProperties.DATASPACE_DEFAULTUSER_HOSTNAME.getValueAsStringOrNull();

                try {
                    UserCredentials userCredentials = getUserCredentials(username, credentials);

                    DataSpaceServiceStarter.getDataSpaceServiceStarter()
                                           .createSpaceWithUserNameSubfolder(username,
                                                                             userCredentials,
                                                                             SchedulerConstants.SCHEDULER_DATASPACE_APPLICATION_ID,
                                                                             userSpaceName,
                                                                             PASchedulerProperties.DATASPACE_DEFAULTUSER_URL.getValueAsString(),
                                                                             localpath,
                                                                             hostname,
                                                                             false,
                                                                             true);

                    // immediately retrieve the User Space
                    userSpace = PADataSpaces.resolveOutput(userSpaceName, userCredentials);
                    logger.info("USER space for user " + username + " is at " + userSpace.getAllRealURIs());
                    // register the user GlobalSpace to the frontend state
                    this.userGlobalSpaces.put(username, userSpace);
                } catch (Exception e) {
                    logger.error("", e);
                    return;
                }
            }
        }
    }

    private UserCredentials getUserCredentials(String username, Credentials credentials) {
        UserCredentials userCredentials = new UserCredentials();

        if (!PASchedulerProperties.DATASPACE_DEFAULTUSER_IMPERSONATION.getValueAsBoolean() ||
            isInternalUser(username)) {
            return userCredentials;
        }

        try {
            CredData decryptedUserCredentials = credentials.decrypt(corePrivateKey);
            if (PASchedulerProperties.SCHEDULER_AUTH_GLOBAL_DOMAIN.isSet() &&
                decryptedUserCredentials.getDomain() == null) {
                decryptedUserCredentials.setDomain(PASchedulerProperties.SCHEDULER_AUTH_GLOBAL_DOMAIN.getValueAsString());
            }
            userCredentials = new UserCredentials(decryptedUserCredentials.getLogin(),
                                                  decryptedUserCredentials.getPassword(),
                                                  decryptedUserCredentials.getDomain(),
                                                  decryptedUserCredentials.getKey());
        } catch (Exception e) {
            logger.error("Could not decrypt user credentials", e);
        }

        return userCredentials;
    }

    /**
     * Get the file object of a file path in the specified dataspace
     * @param dataspace the target DataSpace name. It has two possible values, 'USERSPACE' or 'GLOBALSPACE'.
     * @param username the user's name, it can be null when the dataspace is 'GLOBALSPACE'.
     * @param pathname the relative file path under the specified dataspace
     * @return the file object
     * @throws FileSystemException On error parsing the file path.
     */
    public DataSpacesFileObject resolveFile(String dataspace, String username, Credentials credentials, String pathname)
            throws FileSystemException {
        switch (dataspace) {
            case SchedulerConstants.GLOBALSPACE_NAME:
                return this.getGlobalSpace().resolveFile(pathname);
            case SchedulerConstants.USERSPACE_NAME:
                return this.getUserSpace(username, credentials).resolveFile(pathname);
            default:
                throw new IllegalArgumentException("Invalid dataspace name: " + dataspace);
        }
    }

    private boolean isInternalUser(String username) {
        return internalUsers.contains(username);
    }

    private boolean shouldRegisterUserSpace(String username) {
        return !PASchedulerProperties.DATASPACE_DEFAULTUSER_IMPERSONATION.getValueAsBoolean() ||
               !isInternalUser(username);
    }
}
