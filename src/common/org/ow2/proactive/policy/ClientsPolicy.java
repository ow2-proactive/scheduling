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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.policy;

import java.lang.reflect.Constructor;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.security.UnresolvedPermission;
import java.util.Enumeration;

import javax.management.MBeanPermission;
import javax.security.auth.AuthPermission;

import org.ow2.proactive.authentication.principals.IdentityPrincipal;
import org.ow2.proactive.permissions.ClientPermission;
import org.ow2.proactive.permissions.PrincipalPermission;


/**
 * The java security policy which is used in the scheduler and the resource manager and
 * is indented to filter out permissions for their clients.
 * <p>
 * The policy checks if the current context contains one of {@link IdentityPrincipal}
 * (all subjects authenticated through JAAS service have them). In this case
 * the policy leave any subclass of {@link ClientPermission}, {@link MBeanPermission} or {@link AuthPermission}.
 * Everything else will be filtered out.
 * <p>
 * If the security context does not includes {@link IdentityPrincipal}, the policy delegate
 * request to the original one.
 */
public class ClientsPolicy extends Policy {

    private static ClientsPolicy instance;
    // WARNING debug trace should be done only to system.out (instead of log4j)
    // to avoid recursive permission check
    private boolean debug = false;

    private Policy original;

    private ClientsPolicy(Policy original) {
        this.original = original;

        String debugProperty = System.getProperty("java.security.debug");
        if (debugProperty != null) {
            // if the property is set to "clients" it won't affect the rest of security system
            // so no other traces except from this policy will appear
            if (debugProperty.contains("all") || debugProperty.contains("policy") ||
                debugProperty.contains("clients")) {
                debug = true;
            }
        }

        if (debug) {
            System.out.println("Security policy file " + System.getProperty("java.security.policy"));
        }
    }

    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        return original.getPermissions(codesource);
    }

    @Override
    public PermissionCollection getPermissions(final ProtectionDomain domain) {
        PermissionCollection permissions = new Permissions();
        // Look up permissions
        Principal[] principals = domain.getPrincipals();
        boolean identityPrincipal = false;

        if (principals != null) {
            for (Principal principal : principals) {
                if (principal instanceof IdentityPrincipal) {
                    identityPrincipal = true;
                    PermissionCollection pc = original.getPermissions(domain);
                    if (pc != null) {
                        Permission permission = new PrincipalPermission((IdentityPrincipal) principal);
                        // always adding identity permission
                        permissions.add(permission);
                        if (debug) {
                            // WARNING cannot use log4j as it may lead to recursive permission check
                            System.out.println(principal + " has " + permission);
                        }

                        for (Enumeration<Permission> en = pc.elements(); en.hasMoreElements();) {
                            permission = en.nextElement();

                            // all "non standard" permissions like ClientPermissions are not presented in
                            // boot class path, so they were not correctly resolved at JVM start up time
                            if (permission instanceof UnresolvedPermission) {
                                permission = resolvePermission((UnresolvedPermission) permission);
                                if (permission == null)
                                    continue;
                            }

                            // we grant java.security.AllPermissions to everyone in the security.java.policy
                            // here we exclude it from IdentityPrincipal
                            //
                            // For IdentityPrincipal org.ow2.proactive.permissions.AllPermissions must be used
                            if (!permission.getClass().isAssignableFrom(AllPermission.class)) {
                                if (debug) {
                                    // WARNING cannot use log4j as it may lead to recursive permission check
                                    System.out.println(principal + " has " + permission);
                                }
                                permissions.add(permission);
                            }
                        }
                    }
                }
            }
        }

        if (!identityPrincipal) {
            return original.getPermissions(domain);
        }

        return permissions;
    }

    /**
     * Construct an instance of the real permission represented by UnresolvedPermission
     */
    private Permission resolvePermission(UnresolvedPermission permission) {
        try {
            Class<?> cls = Class.forName(permission.getUnresolvedType());

            String name = permission.getUnresolvedName();
            String actions = permission.getUnresolvedActions();
            Object instance = null;

            if (actions != null && actions.length() > 0) {
                Constructor<?> constr = cls.getDeclaredConstructor(String.class, String.class);
                instance = constr.newInstance(name, actions);
            } else if (name != null && name.length() > 0) {
                Constructor<?> constr = cls.getDeclaredConstructor(String.class);
                instance = constr.newInstance(name);
            } else {
                instance = cls.newInstance();
            }

            if (instance instanceof Permission) {
                return (Permission) instance;
            }
        } catch (Exception ex) {
            if (debug) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void refresh() {
        //original.refresh();
        if (debug) {
            System.out.println("Reloading policy file " + System.getProperty("java.security.policy"));
        }
        Policy.setPolicy(null);
        // force file reloading
        original = Policy.getPolicy();
        Policy.setPolicy(this);
    }

    /**
     * Initialize the policy in the system
     */
    public static void init() {
        if (instance == null) {
            instance = new ClientsPolicy(Policy.getPolicy());
            Policy.setPolicy(instance);
        }
    }
}
