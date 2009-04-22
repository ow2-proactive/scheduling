/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.resourcemanager.nodesource.policy;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;


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
    private static ArrayList<String> supportedPolicies;

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
            Object[] policyParameters) throws RMException {

        NodeSourcePolicy policy;
        try {
            if (!getSupportedPolicies().contains(policyClassName)) {
                throw new RMException(policyClassName + " is not supported");
            }

            Class<?> policyClass = Class.forName(policyClassName);
            PolicyRestriction policyAnnotation = policyClass.getAnnotation(PolicyRestriction.class);
            if (policyAnnotation != null) {
                // checking policy restrictions
                boolean supported = false;
                for (String supportedInfrastructure : policyAnnotation.supportedInfrastructures()) {
                    if (supportedInfrastructure.equals(infrastructureType)) {
                        supported = true;
                        break;
                    }
                }
                if (!supported) {
                    throw new RMException(policyClass.getSimpleName() + " cannot be used with " +
                        infrastructureType);
                }
            }
            policy = (NodeSourcePolicy) policyClass.newInstance();
            policy.configure(policyParameters);
        } catch (Exception e) {
            throw new RMException(e);
        }

        // activating the policy
        NodeSourcePolicy stub;
        try {
            stub = (NodeSourcePolicy) PAActiveObject.turnActive(policy);
        } catch (Exception e) {
            throw new RMException(e);
        }
        return stub;
    }

    /**
     * Loads a list of supported policies from a configuration file
     * @return list of supported infrastructures
     */
    public static ArrayList<String> getSupportedPolicies() {
        if (supportedPolicies == null) {
            supportedPolicies = new ArrayList<String>();
            Properties properties = new Properties();
            try {
                String propFileName = PAResourceManagerProperties.RM_NODESOURCE_POLICY_FILE
                        .getValueAsString();
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
                    supportedPolicies.add(cls.getName());
                } catch (ClassNotFoundException e) {
                }
            }
        }
        return supportedPolicies;
    }
}
