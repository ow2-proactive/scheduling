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
package org.ow2.proactive.resourcemanager.selection.policies;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;


/**
 * 
 * Test for NodeSourcePriorityPolicy
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestNodeSourcePolicy {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void action() throws Exception {

        /**
         * Creating a config with ns names 0, 1, 2, 3, ... 9
         */
        File config = tmpFolder.newFile("policies");
        BufferedWriter out = new BufferedWriter(new FileWriter(config));
        for (int i = 0; i < 10; i++) {
            out.write(i + "\n");
        }
        out.close();

        System.setProperty(NodeSourcePriorityPolicy.CONFIG_NAME_PROPERTY, config.getAbsolutePath());
        NodeSourcePriorityPolicy policy = new NodeSourcePriorityPolicy();

        // checking the arrangement of all nodes
        List<RMNode> nodes = createNodes();
        List<RMNode> res = policy.arrangeNodes(1000, nodes, null);

        assertEquals("Incorrect result size", 400, res.size());
        Iterator<RMNode> iterator = res.iterator();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 20; j++) {
                RMNode node = iterator.next();
                assertEquals("Incorrect arrangenemt (" + i + ", " + j + ")", String.valueOf(i), node.getNodeName());
            }
        }

    }

    private List<RMNode> createNodes() {
        /**
         * Creating an artificial list of nodes
         */
        List<RMNode> nodes = new LinkedList<>();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                RMNode mockedNode = Mockito.mock(RMNode.class);
                when(mockedNode.getNodeName()).thenReturn(String.valueOf(j));
                when(mockedNode.getNodeSourceName()).thenReturn(String.valueOf(j));
                when(mockedNode.compareTo(any())).thenAnswer(invocation -> mockedNode.getNodeName()
                                                                                     .compareTo(invocation.getArgumentAt(0,
                                                                                                                         RMNode.class)
                                                                                                          .getNodeName()));
                nodes.add(mockedNode);
            }
        }
        return nodes;
    }

}
