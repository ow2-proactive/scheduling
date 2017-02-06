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
package org.ow2.proactive.resourcemanager.rmnode;

import static java.util.Collections.emptySet;
import static org.mockito.Mockito.when;

import java.security.Permission;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mockito;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;

/**
 * @author ActiveEon Team
 * @since 06/02/17
 */
public class RMNodeHelper {

    public static Pair<RMNodeImpl, Node> basicWithMockedInternals() {
        return basicWithMockedInternals(
                "name", "hostName", "nodeInformationUrl", "proactiveRuntimeUrl");
    }

    public static Pair<RMNodeImpl, Node> basicWithMockedInternals(
            String name, String hostname, String nodeUrl, String proActiveRuntimeUrl) {
        Node node = createNode(name, hostname, nodeUrl, proActiveRuntimeUrl);

        return basicWithMockedInternals(node);
    }

    private static Node createNode(String name, String hostname, String nodeUrl, String proActiveRuntimeUrl) {
        VMInformation vmInformation = Mockito.mock(VMInformation.class);
        when(vmInformation.getHostName()).thenReturn(hostname);

        NodeInformation nodeInformation = Mockito.mock(NodeInformation.class);
        when(nodeInformation.getName()).thenReturn(name);
        when(nodeInformation.getURL()).thenReturn(nodeUrl);
        when(nodeInformation.getVMInformation()).thenReturn(vmInformation);

        ProActiveRuntime proActiveRuntime = Mockito.mock(ProActiveRuntime.class);
        when(proActiveRuntime.getURL()).thenReturn(proActiveRuntimeUrl);

        Node node = Mockito.mock(Node.class);
        when(node.getNodeInformation()).thenReturn(nodeInformation);
        when(node.getProActiveRuntime()).thenReturn(proActiveRuntime);
        return node;
    }

    public static Pair<RMNodeImpl, Node> basicWithMockedInternals(Node node) {

        NodeSource nodeSource = Mockito.mock(NodeSource.class);

        Set<Principal> principals = new HashSet<>();
        principals.add(new UserNamePrincipal("provider"));

        Client provider =
                new Client(
                        new Subject(
                                false, principals, emptySet(), emptySet()), false);

        Permission permission = Mockito.mock(Permission.class);

        when(nodeSource.getName()).thenReturn("nodeSourceName");

        return new ImmutablePair<>(new RMNodeImpl(node, nodeSource, provider, permission), node);
    }

}
