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
package org.ow2.proactive.policy;

import java.lang.reflect.Constructor;
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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
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

    /**
     * These are the list of permission super classes which are
     * possible to grant to IdentityPrincipals in the java policy file
     */
    private static final Class<?>[] ALLOWED_PERMISSION_TYPES = new Class<?>[] { ClientPermission.class,
            MBeanPermission.class, AuthPermission.class };

    private final static Logger logger = ProActiveLogger.getLogger("proactive.security");
    private static ClientsPolicy instance;

    private Policy original;

    private ClientsPolicy(Policy original) {
        logger.info("Security policy file " + System.getProperty("java.security.policy"));
        this.original = original;
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
                        if (logger.isDebugEnabled()) {
                            logger.debug(principal + " has " + permission);
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

                            // checking is the permission we're going to add to the principal
                            // is among allowed permissions
                            for (Class<?> permType : ALLOWED_PERMISSION_TYPES) {
                                if (permType.isAssignableFrom(permission.getClass())) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(principal + " has " + permission);
                                    }
                                    permissions.add(permission);
                                    break;
                                }
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
            logger.debug(ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public void refresh() {
        //original.refresh();
        logger.info("Reloading policy file " + System.getProperty("java.security.policy"));
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
