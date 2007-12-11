/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.security;

import java.util.Map;

import javax.security.auth.login.LoginException;


public abstract class FileLogin {

    /**
     * File implementation of the Login standard interface. Uses the
     * <code>FileLoginMethod</code> from the configuration file designated by
     * the "java.security.auth.login.config" system property, which must be set
     * to use the <code>FileLoginModule</code> in this package. It will only
     * work if the file designated by path contains pairs in the form :
     * username1:password1<br>
     * username2:password2<br>
     * ...
     *
     * @param params
     *            It must contain 3 <code>String</code>s : "username"
     *            corresponding to the "inriaLocalLogin" in the directory, "pw"
     *            being the password, and "path" the path to the file with the
     *            pairs login/pw. It can also contain groups informations : a
     *            hierarchy of groups, a file that defines which user belongs to
     *            which group, and the lowest group required to successfully log
     *            in. The group required is the "group" entry, the group
     *            hierarchy is the entry "groupsHierarchy" and is a
     *            <code>String[]</code> starting by the group with the least
     *            rights, and the file is the "groupsFilePath" entry. The
     *            "groupsFilePath" and "groupsHierarchy" entries must be defined
     *            only if the "group" one is defined too.
     * @return always true
     * @throws LoginException
     *             If the authentication fails (incorrect credentials) or if
     *             there is an error during the attempt to verify them.
     */
    public static Boolean login(Map<String, Object> params)
        throws LoginException {
        return Login.login(params, "FileLoginMethod");
    }
}
