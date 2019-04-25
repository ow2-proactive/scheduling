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

import java.io.Serializable;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.*;

import org.ow2.proactive.authentication.principals.*;


/**
 *
 * PrincipalPermission represents an access limitation to the
 * private resource for a particular principal. This permission
 * cannot be granted in java policy file and mostly is used in runtime.
 * <p>
 * For example this permission is checked in the scheduler when the user
 * tries to remove the job to allow users remove only their own jobs.
 * <p>
 * Then user is authenticated in JAAS it automatically has all PrincipalPermissions
 * of those principal it associated with. For example when a user named "Bob"
 * is authenticated and it is a member of group "users" it will have 2
 * PrincipalPermission: PrincipalPermission(UserPrincipal("Bob")) and
 * PrincipalPermission(GroupPrincipal("users")).
 * <p>
 * Then in the code if an action is limited to PrincipalPermission(UserPrincipal("Jon"))
 * Bob cannot access it. If it's limited to PrincipalPermission(UserPrincipal("Bob")) or
 * PrincipalPermission(GroupPrincipal("users")) or
 * PrincipalPermission(none) it is authorized to execute it.
 *
 */
public class PrincipalPermission extends ClientPermission {

    // This serial version uid is meant to prevent issues when restoring Resource Manager database from a previous version.
    // any addition to this class (new method, field, etc) should imply to change this uid.
    private static final long serialVersionUID = 1L;

    protected List<IdentityPrincipal> principals = new LinkedList<>();

    public PrincipalPermission(IdentityPrincipal principal) {
        super(principal.getName());
        this.principals.add(principal);
    }

    public PrincipalPermission(String name, Set<? extends IdentityPrincipal> principals) {
        super("Identities collection");
        this.principals.addAll(principals);
    }

    @Override
    public boolean implies(Permission permission) {

        if (!(permission instanceof PrincipalPermission)) {
            return false;
        }

        PrincipalPermission permissionToRespect = (PrincipalPermission) permission;
        if (permissionToRespect.principals.isEmpty()) {
            return true;
        }

        int nbPrincipals = permissionToRespect.principals.size();
        long nbExcludedPrincipals = permissionToRespect.principals.stream()
                                                                  .filter(c -> c instanceof NotIdentityPrincipal)
                                                                  .count();

        if (nbExcludedPrincipals > 0 && nbExcludedPrincipals < nbPrincipals) {
            throw new IllegalArgumentException("Not supposed to find mixed included/excluded principals in permission");
        }

        // Separate included/excluded principals in this
        Set<IdentityPrincipal> includedThisUserNamePrincipals = new HashSet<>();
        Set<IdentityPrincipal> includedThisGroupNamePrincipals = new HashSet<>();
        Set<IdentityPrincipal> thisTokenPrincipals = new HashSet<>();
        for (IdentityPrincipal instance : this.principals) {
            if (instance instanceof NotIdentityPrincipal) {
                throw new IllegalArgumentException("Not supposed to find excluded principals in this");
            } else if (instance instanceof UserNamePrincipal) {
                includedThisUserNamePrincipals.add(instance);
            } else if (instance instanceof GroupNamePrincipal) {
                includedThisGroupNamePrincipals.add(instance);
            } else if (instance instanceof TokenPrincipal) {
                thisTokenPrincipals.add(instance);
            }
        }

        // Separate included/excluded principals in permissionToRespect
        Set<IdentityPrincipal> includedUserNamePrincipals = new HashSet<>();
        Set<IdentityPrincipal> includedGroupNamePrincipals = new HashSet<>();
        Set<IdentityPrincipal> excludedUserNamePrincipals = new HashSet<>();
        Set<IdentityPrincipal> excludedGroupNamePrincipals = new HashSet<>();
        Set<IdentityPrincipal> tokenPrincipals = new HashSet<>();
        for (IdentityPrincipal instance : permissionToRespect.principals) {
            if (instance instanceof NotIdentityPrincipal && instance instanceof UserNamePrincipal) {
                excludedUserNamePrincipals.add(new UserNamePrincipal(instance.getName()));
            } else if (instance instanceof NotIdentityPrincipal && instance instanceof GroupNamePrincipal) {
                excludedGroupNamePrincipals.add(new GroupNamePrincipal(instance.getName()));
            } else if (instance instanceof UserNamePrincipal) {
                includedUserNamePrincipals.add(instance);
            } else if (instance instanceof GroupNamePrincipal) {
                includedGroupNamePrincipals.add(instance);
            } else if (instance instanceof TokenPrincipal) {
                tokenPrincipals.add(instance);
            }
        }

        boolean userAuthorized, groupAuthorized, tokenAuthorized;

        if (nbExcludedPrincipals == 0) {
            // To access to a resource , a principal (this) must be present
            // in the included principal list of the resource permission
            userAuthorized = includedUserNamePrincipals.containsAll(includedThisUserNamePrincipals);
            groupAuthorized = includedGroupNamePrincipals.containsAll(includedThisGroupNamePrincipals);
            tokenAuthorized = tokenPrincipals.containsAll(thisTokenPrincipals);

        } else {

            // To access to a resource , a principal (this) must not be present
            // in the excluded principal list of the resource permission
            userAuthorized = !excludedUserNamePrincipals.stream().anyMatch(includedThisUserNamePrincipals::contains);
            groupAuthorized = !excludedGroupNamePrincipals.stream().anyMatch(includedThisGroupNamePrincipals::contains);
            tokenAuthorized = true;
        }

        // Authorization must be given at user, group and token level
        return userAuthorized && groupAuthorized && tokenAuthorized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        PrincipalPermission that = (PrincipalPermission) o;

        return !(principals != null ? !principals.equals(that.principals) : that.principals != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (principals != null ? principals.hashCode() : 0);
        return result;
    }

    public String toString() {
        return this.getClass().getName() + " of " + principals;
    }

    public PermissionCollection newPermissionCollection() {
        return new PrincipalPermissionCollection();
    }

    public boolean hasPrincipal(IdentityPrincipal principal) {
        return principals.contains(principal);
    }
}

final class PrincipalPermissionCollection extends PermissionCollection implements Serializable {

    // This serial version uid is meant to prevent issues when restoring Resource Manager database from a previous version.
    // any addition to this class (new method, field, etc) should imply to change this uid.
    private static final long serialVersionUID = 1L;

    private transient List<Permission> permissions;

    public PrincipalPermissionCollection() {
        permissions = new LinkedList<>();
    }

    @Override
    public void add(Permission permission) {
        if (!(permission instanceof PrincipalPermission))
            throw new IllegalArgumentException("invalid permission: " + permission);

        permissions.add(permission);
    }

    @Override
    public Enumeration<Permission> elements() {
        return Collections.enumeration(permissions);
    }

    @Override
    public boolean implies(Permission permission) {

        if (!(permission instanceof PrincipalPermission)) {
            return false;
        }

        PrincipalPermission permissionToRespect = (PrincipalPermission) permission;
        if (permissionToRespect.principals.isEmpty()) {
            return true;
        }

        int nbPrincipals = permissionToRespect.principals.size();
        long nbExcludedPrincipals = permissionToRespect.principals.stream()
                                                                  .filter(c -> c instanceof NotIdentityPrincipal)
                                                                  .count();

        if (nbExcludedPrincipals == 0) {
            // Give access if INCLUSION permissions to respect include/implie one of the permission requiring access
            // ex: "Bob" or "GroupBob" get access if "Bob" or "GroupBob" is part of permissions to respect

            if (permissions.stream().anyMatch(p -> p.implies(permission))) {
                return true;
            } else {
                return false;
            }

        } else if (nbExcludedPrincipals == nbPrincipals) {
            // Do not give access if EXCLUSION rules include/implie one of the permission requiring access
            // ex: "Bob" or "GroupBob" do not get access if "Bob" or "GroupBob" is part of permissions to respect
            for (Permission p : permissions) {
                if (!p.implies(permission)) {
                    return false;
                }
            }
            return true;
        } else {
            throw new IllegalArgumentException("Not supposed to find mixed included/excluded principals in permission");
        }
    }
}
