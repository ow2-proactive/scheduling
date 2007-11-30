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
package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMConstants;
import org.objectweb.proactive.extra.infrastructuremanager.exception.IMException;


public class IMConnection implements IMConstants {
    public static IMAdmin connectAsAdmin(String url) throws IMException {
        if (url == null) {
            url = "//localhost/" + NAME_ACTIVE_OBJECT_IMADMIN;
        }

        try {
            IMAdmin admin = (IMAdmin) ProActiveObject.lookupActive(IMAdmin.class.getName(),
                    url);
            return admin;
        } catch (ActiveObjectCreationException e) {
            throw new IMException(e);
        } catch (IOException e) {
            throw new IMException(e);
        }
    }

    public static IMUser connectAsUser(String url) throws IMException {
        if (url == null) {
            url = "//localhost/" + NAME_ACTIVE_OBJECT_IMUSER;
        }
        System.out.println("IMConnection.connectAsUser() " + url);

        try {
            IMUser user = (IMUser) ProActiveObject.lookupActive(IMUser.class.getName(),
                    url);
            return user;
        } catch (ActiveObjectCreationException e) {
            throw new IMException(e);
        } catch (IOException e) {
            throw new IMException(e);
        }
    }

    public static IMMonitoring connectAsMonitor(String url)
        throws IMException {
        if (url == null) {
            url = "//localhost/" + NAME_ACTIVE_OBJECT_IMMONITORING;
        }
        try {
            IMMonitoring mon = (IMMonitoring) ProActiveObject.lookupActive(IMMonitoring.class.getName(),
                    url);
            return mon;
        } catch (ActiveObjectCreationException e) {
            throw new IMException(e);
        } catch (IOException e) {
            throw new IMException(e);
        }
    }
}
