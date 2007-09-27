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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ext.locationserver;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.PAProperties;


public class LocationServerFactory {
    //
    // -- PUBLIC MEMBERS -----------------------------------------------
    //
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public LocationServerFactory() {
    }

    //
    // -- PUBLIC METHOD -----------------------------------------------
    //
    //  public static String getLocationServerClassName() {
    //       return  ProActiveProperties.getLocationServerClass();
    //     }
    //     public static String getLocationServerClassName(UniqueID id) {
    //       return  LocationServerFactory.getLocationServerClassName();
    //     }
    //     public static String getLocationServerName() {
    // 	return ProActiveProperties.getLocationServerRmi();
    //     }
    //     public static String getLocationServerName(UniqueID unique) {
    // 	return LocationServerFactory.getLocationServerName();
    //     }
    public static LocationServer getLocationServer() {
        LocationServer server = null;
        try {
            server = (LocationServer) ProActiveObject.lookupActive(PAProperties.PA_LOCATION_SERVER.getValue(),
                    PAProperties.PA_LOCATION_SERVER_RMI.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return server;
        //	try {
        //		Object t = null;
        //		t.toString();
        //	} catch (Exception e) {
        //		System.out.println("-----------------------");
        //		e.printStackTrace();
        //			System.out.println("-----------------------");
        //	}
        //
        //	return null;
    }

    /**
     * Return the location server associated with the
     * <code>id</code>
     *
     */
    public static LocationServer getLocationServer(UniqueID id) {
        return LocationServerFactory.getLocationServer();
    }
}
