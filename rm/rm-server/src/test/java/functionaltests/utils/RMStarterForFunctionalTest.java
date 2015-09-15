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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.utils;

import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;


/**
 * Locally launches Resource Manager. 
 *
 */
public class RMStarterForFunctionalTest {

    /**
     * Start a Resource Manager.
     * <p/>
     * Must be called with one parameter: path to a RM Properties file.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException(
                "RMTStarter must be started with one parameter: path to a RM Properties file");
        }

        String RMPropPath = args[0];
        PAResourceManagerProperties.updateProperties(RMPropPath);

        //Starting a local RM
        RMFactory.setOsJavaProperty();
        RMFactory.startLocal();

        // waiting the initialization
        RMConnection.waitAndJoin(null);

        System.out.println("Resource Manager successfully created !");
    }
}
