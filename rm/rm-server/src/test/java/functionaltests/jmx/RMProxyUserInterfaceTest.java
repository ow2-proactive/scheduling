/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests.jmx;

import java.security.PublicKey;
import java.util.Collections;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.util.RMListenerProxy;
import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;


public class RMProxyUserInterfaceTest extends RMConsecutive {

    private static final String EXISTING_OBJECT_NAME = "java.lang:type=Runtime";

    // SCHEDULING-1850
    @Test
    public void testGetNodeMBeanInfo_DisconnectionOfRemovedNodeSource() throws Exception {
        RMTHelper helper = RMTHelper.getDefaultInstance();
        RMListenerProxy proxyUserInterface = createRMCachingProxyUserInterface();

        helper.createNodeSource("NodeSource1", 1);

        RMInitialState state = helper.getResourceManager().getMonitoring().getState();
        String nodeSource1_NodeJmxUrl = state.getNodesEvents().get(0).getDefaultJMXUrl();

        Object mBeanFromNodeSource1 = proxyUserInterface.getNodeMBeanInfo(nodeSource1_NodeJmxUrl,
                EXISTING_OBJECT_NAME, Collections.<String> emptyList());

        assertNotNull(mBeanFromNodeSource1);

        // remove nodesource and try another jmx connection
        helper.getResourceManager().removeNodeSource("NodeSource1", true);

        helper.createNodeSource("NodeSource2", 1);

        state = helper.getResourceManager().getMonitoring().getState();
        String nodeSource2_NodeJmxUrl = state.getNodesEvents().get(0).getDefaultJMXUrl();

        Object mBeanFromNodeSource2 = proxyUserInterface.getNodeMBeanInfo(nodeSource2_NodeJmxUrl,
                EXISTING_OBJECT_NAME, Collections.<String> emptyList());

        assertNotNull(mBeanFromNodeSource2);

        // cleanup
        helper.getResourceManager().removeNodeSource("NodeSource2", true);
    }

    private RMListenerProxy createRMCachingProxyUserInterface() throws Exception {
        RMListenerProxy proxyUserInterface = PAActiveObject.newActive(RMListenerProxy.class, new Object[] {});
        final RMAuthentication auth = RMTHelper.getDefaultInstance().getRMAuth();
        final PublicKey pubKey = auth.getPublicKey();
        final Credentials adminCreds = Credentials.createCredentials(new CredData(RMTHelper.defaultUserName,
            RMTHelper.defaultUserPassword), pubKey);
        proxyUserInterface.init(RMTHelper.getLocalUrl(), adminCreds);
        return proxyUserInterface;
    }
}
