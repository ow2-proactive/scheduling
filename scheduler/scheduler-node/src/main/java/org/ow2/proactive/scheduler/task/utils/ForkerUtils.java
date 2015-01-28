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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task.utils;

import java.security.KeyException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.processbuilder.OSUser;
import org.objectweb.proactive.extensions.processbuilder.PAOSProcessBuilderFactory;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.scheduler.common.task.Decrypter;
import org.apache.log4j.Logger;


/**
 * ForkerUtils is a helper to cache OSBuilder factory and provide
 * some helping methods to launcher and executable
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public final class ForkerUtils {

    private static final Logger logger = Logger.getLogger(ForkerUtils.class);

    /** System property Key of the fork method */
    private static final String FORK_METHOD_KEY = "pas.launcher.forkas.method";
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

    private enum ForkMethod {
        NONE("none"), PWD("pwd"), KEY("key");
        private String value;

        private ForkMethod(String value) {
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
    public static PAOSProcessBuilderFactory getOSProcessBuilderFactory(String nativeScriptPath) {
        if (OSBuilderFactory == null) {
            try {
                OSBuilderFactory = new PAOSProcessBuilderFactory(nativeScriptPath);
            } catch (ProActiveException e) {
                e.printStackTrace();
            }
        }
        return OSBuilderFactory;
    }

    /**
     * If the process must be run under a specific user,
     * check the configuration of '{@value #FORK_METHOD_KEY}' property and proceed as follow :
     * <ul>
     * 	<li><b>if {@value #FORK_METHOD_KEY}=none :</b> throws IllegalAccessException</li>
     * 	<li><b>if {@value #FORK_METHOD_KEY}=pwd :</b> return the user using its login and password</li>
     * 	<li><b>if {@value #FORK_METHOD_KEY}=key :</b> return the user using its ssh key</li>
     * </ul>
     *
     * @param decrypter the decrypter that should be a self decryption one.
     * @return the OSUser to be passed to the OSPRocess if node fork method is configured.
     * @throws IllegalAccessException if the node configuration method is not compatible with incoming credentials
     * @throws KeyException decryption failure, malformed data
     * @throws IllegalArgumentException if decrypter is null
     * @throws IllegalAccessException if node fork method is not set
     */
    public static OSUser checkConfigAndGetUser(Decrypter decrypter) throws IllegalAccessException,
            KeyException {
        if (decrypter != null) {
            CredData data = decrypter.decrypt();
            if (ForkMethod.PWD == FORK_METHOD_VALUE) {
                if (data.getPassword() == null) {
                    throw new IllegalAccessException(
                        "Password not found in Credentials, cannot fork using password");
                }
                OSUser u = new OSUser(data.getLogin(), data.getPassword());
                if (data.getDomain() != null) {
                    u.setDomain(data.getDomain());
                }
                return u;
            }
            if (ForkMethod.KEY == FORK_METHOD_VALUE) {
                if (data.getKey() == null) {
                    throw new IllegalAccessException(
                        "SSH key not found in Credentials, cannot fork using ssh Key");
                }
                OSUser u = new OSUser(data.getLogin(), data.getKey());
                if (data.getDomain() != null) {
                    u.setDomain(data.getDomain());
                }
                return u;
            }
            throw new IllegalAccessException("Cannot fork under " + data.getLogin() + ", Property " +
                FORK_METHOD_KEY + " is not configured.");
        } else {
            throw new IllegalArgumentException("Decrypter could not be null");
        }
    }
}
