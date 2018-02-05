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
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ow2.proactive.authentication.principals.IdentityPrincipal;


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
 * Bob cannot access it. If it's limited to PrincipalPermission(GroupPrincipal("Bob")) or
 * PrincipalPermission(GroupPrincipal("users")) or
 * PrincipalPermission(none) it is authorized to execute it.
 *
 */
public class PrincipalPermission extends ClientPermission {

    // This serial version uid is meant to prevent issues when restoring Resource Manager database from a previous version.
    // any addition to this class (new method, field, etc) should imply to change this uid.
    private static final long serialVersionUID = 1L;

    private List<IdentityPrincipal> principals = new LinkedList<>();

    public PrincipalPermission(IdentityPrincipal principal) {
        super(principal.getName());
        this.principals.add(principal);
    }

    public PrincipalPermission(String name, Set<? extends IdentityPrincipal> principals) {
        super("Identities collection");
        this.principals.addAll(principals);
    }

    @Override
    public boolean implies(Permission p) {
        if (!(p instanceof PrincipalPermission)) {
            return false;
        }
        PrincipalPermission pp = (PrincipalPermission) p;
        // checking that all this.principals are presented in
        // p.principals or pp.principals is empty
        if (pp.principals.size() == 0 || pp.principals.containsAll(principals)) {
            return true;
        }
        return false;
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
        for (Permission p : permissions) {
            if (p.implies(permission)) {
                return true;
            }
        }
        return false;
    }
}
