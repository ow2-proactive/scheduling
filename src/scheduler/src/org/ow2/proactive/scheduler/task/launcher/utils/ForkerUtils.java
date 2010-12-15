/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.task.launcher.utils;

import java.security.KeyException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.processbuilder.OSUser;
import org.objectweb.proactive.extensions.processbuilder.PAOSProcessBuilderFactory;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher.OneShotDecrypter;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * ForkerUtils is a helper to cache OSBuilder factory and provide
 * some helping methods to launcher and executable
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public final class ForkerUtils {

    private static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.LAUNCHER);

    /** System property Key of the fork method */
    private static final String FORK_METHOD_KEY = "pas.launcher.forkas.method";
    /** System property value of the fork method */
    private static final ForkMethod FORK_METHOD_VALUE;
    /** OS Process builder factory */
    private static PAOSProcessBuilderFactory OSBuilderFactory = null;

    static {
        //initialize node configuration
        String forkMethod = System.getProperty(FORK_METHOD_KEY);
        if (forkMethod == null) {
            FORK_METHOD_VALUE = null;
            logger_dev.info("Java Property " + FORK_METHOD_KEY + " is not set.");
        } else if (ForkMethod.NONE.matches(forkMethod)) {
            FORK_METHOD_VALUE = ForkMethod.NONE;
        } else if (ForkMethod.PWD.matches(forkMethod)) {
            FORK_METHOD_VALUE = ForkMethod.PWD;
        } else if (ForkMethod.KEY.matches(forkMethod)) {
            FORK_METHOD_VALUE = ForkMethod.KEY;
        } else {
            FORK_METHOD_VALUE = null;
            logger_dev.warn("WARNING : Java Property " + FORK_METHOD_KEY + " is not configured properly :");
            logger_dev.warn("\t Must be one of : [" + ForkMethod.NONE + ", " + ForkMethod.PWD + ", " +
                ForkMethod.KEY + "]");
            logger_dev.warn("\t Currently set  : [" + forkMethod + "]");
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
     * @throws ProActiveException if something wrong append when creation Process builder factory.
     */
    public static PAOSProcessBuilderFactory getOSProcessBuilderFactory() throws ProActiveException {
        if (OSBuilderFactory == null) {
            OSBuilderFactory = new PAOSProcessBuilderFactory();
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
    public static OSUser checkConfigAndGetUser(OneShotDecrypter decrypter) throws IllegalAccessException,
            KeyException {
        if (decrypter != null) {
            CredData data = decrypter.decrypt();
            if (ForkMethod.PWD == FORK_METHOD_VALUE) {
                if (data.getPassword() == null) {
                    throw new IllegalAccessException(
                        "Password not found in Credentials, cannot fork using password");
                }
                String[] lp = data.getLoginPassword();
                return new OSUser(lp[0], lp[1]);
            }
            if (ForkMethod.KEY == FORK_METHOD_VALUE) {
                if (data.getKey() == null) {
                    throw new IllegalAccessException(
                        "SSH key not found in Credentials, cannot fork using ssh Key");
                }
                return new OSUser(data.getLogin(), data.getKey());
            }
            throw new IllegalAccessException("Cannot fork under " + data.getLogin() + ", Property " +
                FORK_METHOD_KEY + " is not configured.");
        } else {
            throw new IllegalArgumentException("Decrypter could not be null");
        }
    }
}
