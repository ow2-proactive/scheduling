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
package org.ow2.proactive.resourcemanager.nodesource.policy;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;


/**
 *
 * Provides a generic way to create a node source policy.
 * Loads all supported policies names from a config file. Checks
 * that required policy is supported at creation time and also
 * that it is compatible with specified infrastructure.
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
        supportedPolicies = new ArrayList<>();
        Properties properties = new Properties();
        try {
            String propFileName = PAResourceManagerProperties.RM_NODESOURCE_POLICY_FILE.getValueAsString();
            if (!(new File(propFileName).isAbsolute())) {
                //file path is relative, so we complete the path with the prefix RM_Home constant
                propFileName = PAResourceManagerProperties.RM_HOME.getValueAsString() + File.separator + propFileName;
            }

            FileInputStream stream = new FileInputStream(propFileName);
            properties.load(stream);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Object className : properties.keySet()) {
            try {
                Class<?> cls = Class.forName(className.toString());
                supportedPolicies.add(cls);
            } catch (ClassNotFoundException e) {
                Logger.getLogger(NodeSourcePolicyFactory.class).warn("Cannot find class " + className.toString());
            }
        }
        return supportedPolicies;
    }
}
