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
package org.ow2.proactive.permissions;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;


public class RoleUtils {

    private static final Logger logger = Logger.getLogger(RoleUtils.class);

    public enum Role {
        basic,
        read,
        write,
        provider,
        nsadmin,
        admin,
        none;
    }

    public static String findRole(Method method) {
        if (method.getAnnotation(RoleBasic.class) != null) {
            return Role.basic.name();
        }
        if (method.getAnnotation(RoleRead.class) != null) {
            return Role.read.name();
        }
        if (method.getAnnotation(RoleWrite.class) != null) {
            return Role.write.name();
        }
        if (method.getAnnotation(RoleProvider.class) != null) {
            return Role.provider.name();
        }
        if (method.getAnnotation(RoleNSAdmin.class) != null) {
            return Role.nsadmin.name();
        }
        if (method.getAnnotation(RoleAdmin.class) != null) {
            return Role.admin.name();
        }
        return Role.none.name();
    }

    public static Method findMethod(Class clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        logger.warn("Method " + methodName + " does not exist in " + clazz.getName());
        throw new SecurityException("Method " + methodName + " does not exist in " + clazz.getName());
    }
}
