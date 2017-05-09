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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.ow2.proactive.authentication.principals.GroupNamePrincipal;
import org.ow2.proactive.authentication.principals.IdentityPrincipal;
import org.ow2.proactive.authentication.principals.TokenPrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.resourcemanager.authentication.Client;


/**
 * 
 * Class defines an access type to the node source for the administration
 * and utilization. When a node source is created user specifies this access level.
 * 
 * Supported levels
 * 
 * ME designates the creator identity
 * ALL designates all users identity
 * users=user1,use2;groups=group1,group2 designates specific users and groups
 * 
 * Deprecated levels
 * MY_GROUPS designates all groups of creator
 * PROVIDER designate the identity of node provider (the one who added the node to the node source can use it) 
 * PROVIDER_GROUPS designate all groups of node provider (only people from these groups will have an access to the node)
 * 
 */
public class AccessType implements Serializable {

    private static Set<String> types = new HashSet<>();

    public static final AccessType ME = new AccessType("ME");

    @Deprecated
    public static final AccessType MY_GROUPS = new AccessType("MY_GROUPS");

    @Deprecated
    public static final AccessType PROVIDER = new AccessType("PROVIDER");

    @Deprecated
    public static final AccessType PROVIDER_GROUPS = new AccessType("PROVIDER_GROUPS");

    public static final AccessType ALL = new AccessType("ALL");

    private String type;

    private String[] users;

    private String[] groups;

    private String[] tokens;

    private AccessType() {
    }

    private AccessType(String type) {
        types.add(type);
        this.type = type;
    }

    /**
     * Parses a string representation of access type.
     * 
     * @param type to parse
     * @return an instance of the AccessType object
     * @throws IllegalArgumentException if the syntax is incorrect 
     */
    public static AccessType valueOf(String type) {

        AccessType accessType = null;
        if (types.contains(type)) {
            accessType = new AccessType(type);
        } else {
            accessType = new AccessType();
            accessType.type = type;

            // parsing the string in the following format
            // users=name1,name2;groups=group1,group2
            HashMap<String, String[]> values = new HashMap<>();
            String[] semicolon = type.split(";");
            if (semicolon.length == 0) {
                throw new IllegalArgumentException("Incorrect parameter value " + type);
            }

            for (String s : semicolon) {
                String[] eq = s.split("=");
                if (eq.length == 0) {
                    throw new IllegalArgumentException("Incorrect parameter value " + type);
                }
                values.put(eq[0], eq[1].split(","));
            }

            accessType.users = values.get("users");
            accessType.groups = values.get("groups");
            accessType.tokens = values.get("tokens");

            if (accessType.users == null && accessType.groups == null && accessType.tokens == null) {
                throw new IllegalArgumentException("Incorrect parameter value " + type);
            }
        }

        return accessType;
    }

    public String toString() {
        return type;
    }

    /**
     * Gets the list of users if the following syntax was used users=user1,user2.
     */
    public String[] getUsers() {
        return users;
    }

    /**
     * Gets the list of groups if the following syntax was used groups=group1,group2.
     */
    public String[] getGroups() {
        return groups;
    }

    /**
     * Gets the list of tokens if the following syntax was used tokens=t1,t2.
     */
    public String[] getTokens() {
        return tokens;
    }

    /**
     * Returns a set of identity principals for the specified user depending
     * on the node source access type.
     */
    public Set<? extends IdentityPrincipal> getIdentityPrincipals(Client client) {

        if (this.equals(AccessType.ME) || this.equals(AccessType.PROVIDER)) {
            // USER
            return client.getSubject().getPrincipals(UserNamePrincipal.class);
        } else if (this.equals(AccessType.MY_GROUPS) || this.equals(AccessType.PROVIDER_GROUPS)) {
            // GROUP
            return client.getSubject().getPrincipals(GroupNamePrincipal.class);
        }

        Set<IdentityPrincipal> identities = new HashSet<>();
        if (users != null) {
            for (String user : users) {
                identities.add(new UserNamePrincipal(user));
            }
        }
        if (groups != null) {
            for (String group : groups) {
                identities.add(new GroupNamePrincipal(group));
            }
        }
        if (tokens != null) {
            for (String token : tokens) {
                identities.add(new TokenPrincipal(token));
            }
        }
        return identities;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /**
     * Compares two AccessType objects
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AccessType))
            return false;
        AccessType other = (AccessType) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
