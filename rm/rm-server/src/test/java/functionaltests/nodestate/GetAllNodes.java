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
package functionaltests.nodestate;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMTHelper;
import functionaltests.utils.TestUsers;


public class GetAllNodes {
    public static void main(String[] args) {
        RMAuthentication auth;
        try {
            auth = RMConnection.join(RMTHelper.getLocalUrl());
            Credentials cred = Credentials.createCredentials(new CredData(TestUsers.DEMO.username,
                                                                          TestUsers.DEMO.password),
                                                             auth.getPublicKey());
            ResourceManager rm = auth.login(cred);
            NodeSet nodes = rm.getNodes(new Criteria(rm.getState().getFreeNodesNumber()));
            // use nodes to block until the future is available
            System.out.println("Got " + nodes.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}
