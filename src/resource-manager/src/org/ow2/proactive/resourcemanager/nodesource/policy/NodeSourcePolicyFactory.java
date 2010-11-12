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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.policy;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 *
 * Provides a generic way to create a node source policy.
 * Loads all supported policies names from a config file. Checks
 * that required policy is supported at creation time and also
 * that it is compatible with specified infrastructure (see {@link PolicyRestriction}).
 *
 */
public class NodeSourcePolicyFactory {

    /** list of supported policies */
    private static Collection<Class<?>> supportedPolicies;

    /**
     * Creates a new node source policy using reflection mechanism.
     *
     * @param policyClassName full class name of the policy
     * @param infrastructureType infrastructure class name (for compatibility check)
     * @param policyParameters policy parameters
     * @return new instance of the node source policy
     * @throws RMException if any problems occurred
     */
    public static NodeSourcePolicy create(String policyClassName, String infrastructureType,
            Object[] policyParameters) {

        NodeSourcePolicy policy;
        try {
            boolean supported = false;
            for (Class<?> cls : getSupportedPolicies()) {
                if (cls.getName().equals(policyClassName)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                throw new IllegalArgumentException(policyClassName + " is not supported");
            }

            Class<?> policyClass = Class.forName(policyClassName);
            policy = (NodeSourcePolicy) policyClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // turning policy into an active object
        NodeSourcePolicy stub;
        try {
            stub = (NodeSourcePolicy) PAActiveObject.turnActive(policy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // initializing parameters
        BooleanWrapper result = stub.configure(policyParameters);
        if (!result.getBooleanValue()) {
            throw new RuntimeException("Cannot configure the policy " + policyClassName);
        }

        return stub;
    }

    /**
     * Loads a list of supported policies from a configuration file
     * @return list of supported infrastructures
     */
    public static Collection<Class<?>> getSupportedPolicies() {
        // reload file each time as it can be updated while the rm is running
        supportedPolicies = new ArrayList<Class<?>>();
        Properties properties = new Properties();
        try {
            String propFileName = PAResourceManagerProperties.RM_NODESOURCE_POLICY_FILE.getValueAsString();
            if (!(new File(propFileName).isAbsolute())) {
                //file path is relative, so we complete the path with the prefix RM_Home constant
                propFileName = PAResourceManagerProperties.RM_HOME.getValueAsString() + File.separator +
                    propFileName;
            }

            properties.load(new FileInputStream(propFileName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Object className : properties.keySet()) {
            try {
                Class<?> cls = Class.forName(className.toString());
                supportedPolicies.add(cls);
            } catch (ClassNotFoundException e) {
                ProActiveLogger.getLogger(RMLoggers.NODESOURCE).warn(
                        "Cannot find class " + className.toString());
            }
        }
        return supportedPolicies;
    }
}
