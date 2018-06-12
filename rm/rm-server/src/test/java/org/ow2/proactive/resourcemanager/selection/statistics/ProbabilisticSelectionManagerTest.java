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
package org.ow2.proactive.resourcemanager.selection.statistics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.SelectionManager;
import org.ow2.proactive.resourcemanager.selection.SelectionManagerTest;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;


/**
 * @author ActiveEon Team
 * @since 29/06/2017
 */
public class ProbabilisticSelectionManagerTest {

    @Before
    public void setUp() throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getLogger(ProbablisticSelectionManager.class).setLevel(Level.DEBUG);
    }

    @After
    public void tearDown() throws Exception {
        RMCore.topologyManager = null;
        System.setSecurityManager(null);
    }

    @Test
    public void testSelectNodesWithNoNodes() {
        RMCore rmCore = SelectionManagerTest.newMockedRMCore();
        SelectionManager selectionManager = new ProbablisticSelectionManager(rmCore);
        Criteria crit = new Criteria(1);
        crit.setTopology(TopologyDescriptor.ARBITRARY);
        crit.setScripts(null);
        crit.setBlackList(null);
        crit.setBestEffort(false);
        NodeSet nodeSet = selectionManager.selectNodes(crit, null);
        assertEquals(0, nodeSet.size());
    }

    @Test
    public void testIncreasingProbabilityDynamic() throws Exception {
        int nbNodes = 10;
        SelectionScript script = new SelectionScript("test", "groovy", true);
        ManagerObjects managerObjects = new ManagerObjects(nbNodes).invoke();
        SelectionManager selectionManager = managerObjects.getSelectionManager();
        ArrayList<RMNode> freeNodes = managerObjects.getFreeNodes();

        for (int i = 0; i < nbNodes; i++) {
            // we increase the probability for each node, lowest node has the min number of true results
            for (int j = 0; j < i + 1; j++) {
                selectionManager.processScriptResult(script,
                                                     Collections.EMPTY_MAP,
                                                     new ScriptResult<>(true),
                                                     freeNodes.get(i));
            }
        }

        List<RMNode> arrangedNodes = selectionManager.arrangeNodesForScriptExecution(freeNodes,
                                                                                     Collections.singletonList(script),
                                                                                     Collections.EMPTY_MAP);

        // nodes are expected to be sorted in reverse order
        for (int i = 0; i < nbNodes; i++) {
            Assert.assertEquals("mocked-node-" + (nbNodes - i), arrangedNodes.get(i).getNodeName());
            Assert.assertFalse(selectionManager.isPassed(script, Collections.EMPTY_MAP, arrangedNodes.get(i)));
        }
    }

    @Test
    public void testDecreasingProbabilityDynamicScriptWithDynamicityStorage() throws Exception {
        int nbNodes = 10;
        SelectionScript script = new SelectionScript("test", "groovy", true);
        ManagerObjects managerObjects = new ManagerObjects(nbNodes).invoke();
        SelectionManager selectionManager = managerObjects.getSelectionManager();
        ArrayList<RMNode> freeNodes = managerObjects.getFreeNodes();

        for (int i = 0; i < nbNodes; i++) {
            // we decrease the probability for each node, lowest node has the max number of false results
            for (int j = i; j < nbNodes; j++) {
                selectionManager.processScriptResult(script,
                                                     Collections.EMPTY_MAP,
                                                     new ScriptResult<>(false),
                                                     freeNodes.get(i));
            }
        }

        List<RMNode> arrangedNodes = selectionManager.arrangeNodesForScriptExecution(freeNodes,
                                                                                     Collections.singletonList(script),
                                                                                     Collections.EMPTY_MAP);

        // list is supposed to be empty because of dynamicity != 0
        Assert.assertEquals(0, arrangedNodes.size());
    }

    @Test
    public void testDecreasingProbabilityDynamicScriptWithoutDynamicityStorage() throws Exception {
        int nbNodes = 10;
        PAResourceManagerProperties.RM_SELECT_SCRIPT_NODE_DYNAMICITY.updateProperty("0");
        try {

            SelectionScript script = new SelectionScript("test", "groovy", true);
            ManagerObjects managerObjects = new ManagerObjects(nbNodes).invoke();
            SelectionManager selectionManager = managerObjects.getSelectionManager();
            ArrayList<RMNode> freeNodes = managerObjects.getFreeNodes();

            for (int i = 0; i < nbNodes; i++) {
                // we decrease the probability for each node, lowest node has the max number of false results
                for (int j = i; j < nbNodes; j++) {
                    selectionManager.processScriptResult(script,
                                                         Collections.EMPTY_MAP,
                                                         new ScriptResult<>(false),
                                                         freeNodes.get(i));
                }
            }

            List<RMNode> arrangedNodes = selectionManager.arrangeNodesForScriptExecution(freeNodes,
                                                                                         Collections.singletonList(script),
                                                                                         Collections.EMPTY_MAP);

            // list is supposed to contain all nodes because of dynamicity == 0
            Assert.assertEquals(freeNodes.size(), arrangedNodes.size());

            // nodes are expected to be sorted in reverse order
            for (int i = 0; i < nbNodes; i++) {
                Assert.assertEquals("mocked-node-" + (nbNodes - i), arrangedNodes.get(i).getNodeName());
                Assert.assertFalse(selectionManager.isPassed(script, Collections.EMPTY_MAP, arrangedNodes.get(i)));
            }
        } finally {
            PAResourceManagerProperties.RM_SELECT_SCRIPT_NODE_DYNAMICITY.updateProperty("300000");
        }
    }

    @Test
    public void testDecreasingProbabilityErrorsDynamicScriptWithoutDynamicityStorage() throws Exception {
        int nbNodes = 10;
        PAResourceManagerProperties.RM_SELECT_SCRIPT_NODE_DYNAMICITY.updateProperty("0");
        try {

            SelectionScript script = new SelectionScript("test", "groovy", true);
            ManagerObjects managerObjects = new ManagerObjects(nbNodes).invoke();
            SelectionManager selectionManager = managerObjects.getSelectionManager();
            ArrayList<RMNode> freeNodes = managerObjects.getFreeNodes();

            for (int i = 0; i < nbNodes; i++) {
                // we decrease the probability for each node, lowest node has the max number of script exceptions
                for (int j = i; j < nbNodes; j++) {
                    selectionManager.processScriptResult(script,
                                                         Collections.EMPTY_MAP,
                                                         new ScriptResult<Boolean>(new IllegalArgumentException("amistake")),
                                                         freeNodes.get(i));
                }
            }

            List<RMNode> arrangedNodes = selectionManager.arrangeNodesForScriptExecution(freeNodes,
                                                                                         Collections.singletonList(script),
                                                                                         Collections.EMPTY_MAP);

            // list is supposed to contain all nodes because of dynamicity == 0
            Assert.assertEquals(freeNodes.size(), arrangedNodes.size());

            // nodes are expected to be sorted in reverse order
            for (int i = 0; i < nbNodes; i++) {
                Assert.assertEquals("mocked-node-" + (nbNodes - i), arrangedNodes.get(i).getNodeName());
                Assert.assertFalse(selectionManager.isPassed(script, Collections.EMPTY_MAP, arrangedNodes.get(i)));
            }
        } finally {
            PAResourceManagerProperties.RM_SELECT_SCRIPT_NODE_DYNAMICITY.updateProperty("300000");
        }
    }

    @Test
    public void testIncreasingProbabilityStatic() throws Exception {
        int nbNodes = 10;
        SelectionScript script = new SelectionScript("test", "groovy", false);
        ManagerObjects managerObjects = new ManagerObjects(nbNodes).invoke();
        SelectionManager selectionManager = managerObjects.getSelectionManager();
        ArrayList<RMNode> freeNodes = managerObjects.getFreeNodes();

        for (int i = 0; i < nbNodes; i++) {
            // we increase the probability for each node, but it should not change the result for static scripts
            for (int j = 0; j < i + 1; j++) {
                selectionManager.processScriptResult(script,
                                                     Collections.EMPTY_MAP,
                                                     new ScriptResult<>(true),
                                                     freeNodes.get(i));
            }
        }

        List<RMNode> arrangedNodes = selectionManager.arrangeNodesForScriptExecution(freeNodes,
                                                                                     Collections.singletonList(script),
                                                                                     Collections.EMPTY_MAP);

        // nodes are expected to be sorted in initial order
        for (int i = 0; i < nbNodes; i++) {
            Assert.assertEquals("mocked-node-" + (i + 1), arrangedNodes.get(i).getNodeName());
            Assert.assertTrue(selectionManager.isPassed(script, Collections.EMPTY_MAP, arrangedNodes.get(i)));
        }
    }

    @Test
    public void testVariableBindings() throws Exception {
        SelectionScript script = new SelectionScript("variables.get(\"TOTO\")", "groovy", false);
        ManagerObjects managerObjects = new ManagerObjects(1).invoke();
        SelectionManager selectionManager = managerObjects.getSelectionManager();
        ArrayList<RMNode> freeNodes = managerObjects.getFreeNodes();

        selectionManager.processScriptResult(script,
                                             Collections.singletonMap("TOTO", (Serializable) "value"),
                                             new ScriptResult<>(true),
                                             freeNodes.get(0));
        Assert.assertTrue(selectionManager.isPassed(script,
                                                    Collections.singletonMap("TOTO", (Serializable) "value"),
                                                    freeNodes.get(0)));
        Assert.assertFalse(selectionManager.isPassed(script,
                                                     Collections.singletonMap("TOTO", (Serializable) "differentValue"),
                                                     freeNodes.get(0)));
        Assert.assertFalse(selectionManager.isPassed(script,
                                                     Collections.<String, Serializable> emptyMap(),
                                                     freeNodes.get(0)));

        selectionManager.processScriptResult(script,
                                             Collections.singletonMap("TOTO", (Serializable) "differentValue"),
                                             new ScriptResult<>(true),
                                             freeNodes.get(0));
        Assert.assertTrue(selectionManager.isPassed(script,
                                                    Collections.singletonMap("TOTO", (Serializable) "differentValue"),
                                                    freeNodes.get(0)));
        Assert.assertTrue(selectionManager.isPassed(script,
                                                    Collections.singletonMap("TOTO", (Serializable) "value"),
                                                    freeNodes.get(0)));
    }

    private class ManagerObjects {
        private int nbNodes;

        private ArrayList<RMNode> freeNodes;

        private SelectionManager selectionManager;

        public ManagerObjects(int nbNodes) {
            this.nbNodes = nbNodes;
        }

        public ArrayList<RMNode> getFreeNodes() {
            return freeNodes;
        }

        public SelectionManager getSelectionManager() {
            return selectionManager;
        }

        public ManagerObjects invoke() throws InvalidScriptException {
            RMCore rmCore = SelectionManagerTest.newMockedRMCore();

            freeNodes = new ArrayList<>(nbNodes);
            for (int i = 0; i < nbNodes; i++) {
                freeNodes.add(SelectionManagerTest.createMockeNode("user",
                                                                   "mocked-node-" + (i + 1),
                                                                   "mocked-node-" + (i + 1)));
            }
            when(rmCore.getFreeNodes()).thenReturn(freeNodes);

            selectionManager = new ProbablisticSelectionManager(rmCore);

            return this;
        }
    }
}
