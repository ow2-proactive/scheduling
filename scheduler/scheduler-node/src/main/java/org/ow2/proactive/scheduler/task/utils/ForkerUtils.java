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
package org.ow2.proactive.scheduler.task.utils;

import java.io.File;
import java.security.KeyException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.processbuilder.OSUser;
import org.objectweb.proactive.extensions.processbuilder.PAOSProcessBuilderFactory;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.scheduler.task.context.TaskContext;

import com.google.common.base.Strings;


/**
 * ForkerUtils is a helper to cache OSBuilder factory and provide
 * some helping methods to launcher and executable.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public final class ForkerUtils {

    private static final Logger logger = Logger.getLogger(ForkerUtils.class);

    /** System property Key of the fork method */
    public static final String FORK_METHOD_KEY = "pas.launcher.forkas.method";

    // allows to configure the runasme fork method for a workflow task
    public static final String RUNAS_METHOD_GENERIC_INFO = "RUNAS_METHOD";

    // allows to configure the runasme user for a workflow task
    public static final String RUNAS_USER_GENERIC_INFO = "RUNAS_USER";

    // allows to configure the runasme user domain for a workflow task. User domain is a Windows feature,
    // default domain name on Windows is the machine name.
    public static final String RUNAS_DOMAIN_GENERIC_INFO = "RUNAS_DOMAIN";

    // allows to configure the runasme password for a workflow task (if runasme method is PWD)
    public static final String RUNAS_PWD_GENERIC_INFO = "RUNAS_PWD";

    // allows to configure the runasme password as third-party credentials for a workflow task (if runasme method is PWD)
    public static final String RUNAS_PWD_CRED_GENERIC_INFO = "RUNAS_PWD_CRED";

    // allows to configure the runasme ssh key for a workflow task (if runasme method is KEY)
    public static final String RUNAS_SSH_KEY_GENERIC_INFO = "RUNAS_SSH_KEY";

    // allows to configure the runasme ssh key as third-party credentials for a workflow task (if runasme method is KEY)
    // note that by default the 3rd-part credential SSH_PRIVATE_KEY can be used to store the current user private key (works if the same key is used across all machines)
    public static final String RUNAS_SSH_KEY_CRED_GENERIC_INFO = "RUNAS_SSH_KEY_CRED";

    private static ForkerUtils instance = null;

    /** System property value of the fork method */
    private static final ForkMethod FORK_METHOD_VALUE;

    /** OS Process builder factory */
    private static PAOSProcessBuilderFactory OSBuilderFactory = null;

    static {
        //initialize node configuration
        String forkMethod = System.getProperty(FORK_METHOD_KEY);
        if (ForkMethod.NONE.matches(forkMethod)) {
            FORK_METHOD_VALUE = ForkMethod.NONE;
        } else if (ForkMethod.PWD.matches(forkMethod)) {
            FORK_METHOD_VALUE = ForkMethod.PWD;
        } else if (ForkMethod.KEY.matches(forkMethod)) {
            FORK_METHOD_VALUE = ForkMethod.KEY;
        } else {
            FORK_METHOD_VALUE = ForkMethod.PWD;
            logger.debug("Java Property " + FORK_METHOD_KEY +
                         " is not set or uses invalid value. Fallback to method password");
        }
    }

    private ForkerUtils() {

    }

    public static ForkerUtils getInstance() {
        if (instance == null) {
            instance = new ForkerUtils();
        }
        return instance;
    }

    public enum ForkMethod {
        NONE("none"),
        PWD("pwd"),
        KEY("key");
        private String value;

        ForkMethod(String value) {
            this.value = value;
        }

        boolean matches(String val) {
            return this.value.equalsIgnoreCase(val);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Get the singleton instance of PAOSProcessBuilderFactory
     *
     * @return the singleton instance of PAOSProcessBuilderFactory
     */
    public PAOSProcessBuilderFactory getOSProcessBuilderFactory(String nativeScriptPath) {
        if (OSBuilderFactory == null) {
            OSBuilderFactory = new PAOSProcessBuilderFactory(nativeScriptPath);
        }
        return OSBuilderFactory;
    }

    /**
     * If the process must be run under a specific user,
     * check the configuration of '{@value #FORK_METHOD_KEY}' property and proceed as follow:
     * <ul>
     * 	<li><b>if {@value #FORK_METHOD_KEY}=none :</b> throws IllegalAccessException</li>
     * 	<li><b>if {@value #FORK_METHOD_KEY}=pwd :</b> return the user using its login and password</li>
     * 	<li><b>if {@value #FORK_METHOD_KEY}=key :</b> return the user using its ssh key</li>
     * </ul>
     *
     * @param taskContext the task context.
     * @return the OSUser to be passed to the OSPRocess if node fork method is configured.
     * @throws IllegalAccessException if the node configuration method is not compatible with incoming credentials
     * @throws KeyException decryption failure, malformed data
     * @throws IllegalArgumentException if decrypter is null
     * @throws IllegalAccessException if node fork method is not set
     */
    public OSUser checkConfigAndGetUser(TaskContext taskContext) throws IllegalAccessException, KeyException {
        Decrypter decrypter = taskContext.getDecrypter();
        Map<String, String> genericInformation = taskContext.getInitializer().getGenericInformation();

        if (decrypter != null) {
            CredData data = decrypter.decrypt();
            OSUser u;
            switch (getForkMethod(genericInformation)) {
                case NONE:
                    u = new OSUser(getLogin(data, genericInformation));
                    u.setDomain(getDomain(data, genericInformation));
                    return u;
                case PWD:
                    String password = getPassword(data, genericInformation, data.getThirdPartyCredentials());
                    if (password == null) {
                        throw new IllegalAccessException("Password not found in Credentials, cannot fork using password");
                    }
                    u = new OSUser(getLogin(data, genericInformation), password);
                    u.setDomain(getDomain(data, genericInformation));
                    return u;
                case KEY:
                    byte[] key = getKey(data, genericInformation, data.getThirdPartyCredentials());
                    if (key == null) {
                        throw new IllegalAccessException("SSH key not found in Credentials, cannot fork using ssh Key");
                    }
                    u = new OSUser(getLogin(data, genericInformation), key);
                    u.setDomain(getDomain(data, genericInformation));
                    return u;
                default:
                    throw new IllegalAccessException("Cannot fork under " + data.getLogin() + ", Property " +
                                                     FORK_METHOD_KEY + " is not configured.");
            }

        } else {
            throw new IllegalArgumentException("Decrypter cannot be null");
        }
    }

    private ForkMethod getForkMethod(Map<String, String> genericInformation) {
        if (genericInformation != null && genericInformation.get(RUNAS_METHOD_GENERIC_INFO) != null) {
            return ForkMethod.valueOf(genericInformation.get(RUNAS_METHOD_GENERIC_INFO));
        }
        return FORK_METHOD_VALUE;
    }

    private String getLogin(CredData data, Map<String, String> genericInformation) {
        if (genericInformation != null && genericInformation.get(RUNAS_USER_GENERIC_INFO) != null) {
            return genericInformation.get(RUNAS_USER_GENERIC_INFO);
        } else {
            return data.getLogin();
        }
    }

    private String getDomain(CredData data, Map<String, String> genericInformation) {
        if (genericInformation != null && genericInformation.get(RUNAS_DOMAIN_GENERIC_INFO) != null) {
            return genericInformation.get(RUNAS_DOMAIN_GENERIC_INFO);
        } else {
            return data.getDomain();
        }
    }

    private String getPassword(CredData data, Map<String, String> genericInformation,
            Map<String, String> thirdPartyCredentials) throws IllegalAccessException {
        if (genericInformation != null && genericInformation.get(RUNAS_PWD_GENERIC_INFO) != null) {
            return genericInformation.get(RUNAS_PWD_GENERIC_INFO);
        } else if (genericInformation != null && genericInformation.get(RUNAS_PWD_CRED_GENERIC_INFO) != null) {
            String password = thirdPartyCredentials.get(genericInformation.get(RUNAS_PWD_CRED_GENERIC_INFO));
            if (password == null) {
                throw new IllegalAccessException("Password not found in third-party credentials entry " +
                                                 genericInformation.get(RUNAS_PWD_CRED_GENERIC_INFO) +
                                                 ". cannot fork using password.");
            }
            return password;
        } else {
            return data.getPassword();
        }
    }

    private byte[] getKey(CredData data, Map<String, String> genericInformation,
            Map<String, String> thirdPartyCredentials) throws IllegalAccessException {
        if (genericInformation.get(RUNAS_SSH_KEY_GENERIC_INFO) != null) {
            return genericInformation.get(RUNAS_SSH_KEY_GENERIC_INFO).getBytes();
        } else if (genericInformation.get(RUNAS_SSH_KEY_CRED_GENERIC_INFO) != null) {
            String keyString = thirdPartyCredentials.get(genericInformation.get(RUNAS_SSH_KEY_CRED_GENERIC_INFO));
            if (Strings.isNullOrEmpty(keyString)) {
                throw new IllegalAccessException("SSH Key not found in third-Party credentials entry " +
                                                 genericInformation.get(RUNAS_SSH_KEY_CRED_GENERIC_INFO) +
                                                 ". cannot fork using ssh key");
            }
            return thirdPartyCredentials.get(genericInformation.get(RUNAS_SSH_KEY_CRED_GENERIC_INFO)).getBytes();
        } else {
            return data.getKey();
        }
    }

    public void setSharedExecutablePermissions(File file) {
        setSharedPermissions(file, true);
    }

    public void setSharedPermissions(File file) {
        setSharedPermissions(file, false);
    }

    private void setSharedPermissions(File file, boolean setExecutable) {
        if (!file.setReadable(true, false))
            logger.warn("Failed to set read permission on : " + file);
        if (!file.setWritable(true, false))
            logger.warn("Failed to set write permission on : " + file);
        if (setExecutable) {
            if (!file.setExecutable(true, false))
                logger.warn("Failed to set executable permission on : " + file);
        }
    }

}
