/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;


/**
 * Active object that launches Resource Manager on a forked JVM.
 *
 * @author ProActive team
 *
 */
public class RMLauncherAO {

    /**
     * ProActive empty constructor
     */
    public RMLauncherAO() {
    }

    /**
     * Start a Resource Manager in Active's Object's JVM
     * @param RMPropPath  path to a RM Properties file, or null if not needed
     * @return SchedulerAuthenticationInteface of created Scheduler
     * @throws Exception if any error occurs.
     */
    public RMAuthentication createAndJoinForkedRM(String RMPropPath) throws Exception {

        RMAuthentication auth = null;

        if (RMPropPath != null) {
            PAResourceManagerProperties.updateProperties(RMPropPath);
        }

        //Starting a local RM
        RMFactory.setOsJavaProperty();
        RMFactory.startLocal();

        // waiting the initialization
        auth = RMConnection.waitAndJoin(null);

        System.out.println("Resource Manager successfully created !");
        return auth;
    }
}
