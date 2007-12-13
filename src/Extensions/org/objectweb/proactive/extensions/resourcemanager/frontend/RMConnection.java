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
package org.objectweb.proactive.extensions.resourcemanager.frontend;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.exception.RMException;


/**
 * Class that implements static methods to execute lookups on
 * Resource manager (RM) active objects.
 * This class provide a way to connect to an existing RM and get
 * stubs of front-end objects :<BR>
 * - user interface of the RM : {@link RMUser}.<BR>
 * - administrator interface of the RM : {@link RMAdmin}.<BR>
 * - Monitoring interface of the RM : {@link RMMonitoring}.<BR>
 *
 * @author ProActive team.
 * @version 3.9
 * @since ProActive 3.9
 *
 */
@PublicAPI
public class RMConnection implements RMConstants {

    /**
     * Gives the RM's Administrator interface.
     * @param url host name URL of the RM.
     * @return RMAdmin Stub of RMAdmin active object.
     * @throws RMException if the lookup fails.
     */
    public static RMAdmin connectAsAdmin(String url) throws RMException {
        if (url == null) {
            url = "//localhost/" + NAME_ACTIVE_OBJECT_RMADMIN;
        }

        try {
            RMAdmin admin = (RMAdmin) PAActiveObject.lookupActive(RMAdmin.class.getName(), url);
            return admin;
        } catch (ActiveObjectCreationException e) {
            throw new RMException(e);
        } catch (IOException e) {
            throw new RMException(e);
        }
    }

    /**
     * Gives the RM's user interface.
     * @param url host name URL of the RM.
     * @return User Stub of User active object.
     * @throws RMException if the lookup fails.
     */
    public static RMUser connectAsUser(String url) throws RMException {
        if (url == null) {
            url = "//localhost/" + NAME_ACTIVE_OBJECT_RMUSER;
        }
        try {
            RMUser user = (RMUser) PAActiveObject.lookupActive(RMUser.class.getName(), url);
            return user;
        } catch (ActiveObjectCreationException e) {
            throw new RMException(e);
        } catch (IOException e) {
            throw new RMException(e);
        }
    }

    /**
     * Gives the RM's  monitoring interface.
     * @param url host name URL of the RM.
     * @return RMMonitoring Stub of monitoring active object.
     * @throws RMException if the lookup fails.
     */
    public static RMMonitoring connectAsMonitor(String url) throws RMException {
        if (url == null) {
            url = "//localhost/" + NAME_ACTIVE_OBJECT_RMMONITORING;
        }
        try {
            RMMonitoring mon = (RMMonitoring) PAActiveObject.lookupActive(RMMonitoring.class.getName(), url);
            return mon;
        } catch (ActiveObjectCreationException e) {
            throw new RMException(e);
        } catch (IOException e) {
            throw new RMException(e);
        }
    }
}
