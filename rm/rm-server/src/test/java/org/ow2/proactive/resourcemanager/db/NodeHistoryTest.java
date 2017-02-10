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
package org.ow2.proactive.resourcemanager.db;

import java.util.List;

import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.core.history.NodeHistory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


public class NodeHistoryTest {

    protected static RMDBManager dbManager;

    @BeforeClass
    public static void initDB() throws Exception {
        PAResourceManagerProperties.RM_ALIVE_EVENT_FREQUENCY.updateProperty("100");
        Configuration config = new Configuration().configure("/functionaltests/config/hibernate.cfg.xml");
        dbManager = new RMDBManager(config, true, true);
    }

    @AfterClass
    public static void cleanup() {
        if (dbManager != null) {
            dbManager.close();
        }
    }

    @Test
    public void testNodeHistory() throws Exception {
        testSaveHistory();
        testUpdateHistory();
    }

    public void testSaveHistory() throws Exception {

        Configuration config = new Configuration().configure("/functionaltests/config/hibernate.cfg.xml");
        dbManager = new RMDBManager(config, true, true);

        NodeHistory expected = createNodeHistory(1);

        dbManager.saveNodeHistory(expected);

        List<?> rows = dbManager.executeSqlQuery("from NodeHistory");
        Assert.assertEquals(1, rows.size());

        NodeHistory actual = (NodeHistory) rows.get(0);
        assertEquals(expected, actual);
    }

    public void testUpdateHistory() throws Exception {

        NodeHistory expected1 = createNodeHistory(1);
        expected1.setEndTime(2);
        NodeHistory expected2 = createNodeHistory(2);

        dbManager.saveNodeHistory(expected2);

        List<?> rows = dbManager.executeSqlQuery("from NodeHistory");
        Assert.assertEquals(2, rows.size());

        assertEquals(expected1, (NodeHistory) rows.get(0));
        assertEquals(expected2, (NodeHistory) rows.get(1));

        dbManager.close();
    }

    private void assertEquals(NodeHistory expected, NodeHistory actual) {
        Assert.assertEquals(expected.getHost(), actual.getHost());
        Assert.assertEquals(expected.getNodeSource(), actual.getNodeSource());
        Assert.assertEquals(expected.getNodeState(), actual.getNodeState());
        Assert.assertEquals(expected.getNodeUrl(), actual.getNodeUrl());
        Assert.assertEquals(expected.getProviderName(), actual.getProviderName());
        Assert.assertEquals(expected.getStartTime(), actual.getStartTime());
        Assert.assertEquals(expected.getEndTime(), actual.getEndTime());
    }

    private NodeHistory createNodeHistory(long startTime) {

        NodeHistory nodeHistory = new NodeHistory();

        nodeHistory.setHost("host");
        nodeHistory.setNodeSource("ns");
        nodeHistory.setNodeState(NodeState.FREE);
        nodeHistory.setNodeUrl("url");
        nodeHistory.setProviderName("provider");
        nodeHistory.setStartTime(startTime);
        nodeHistory.setEndTime(0);
        nodeHistory.setStoreInDataBase(true);

        return nodeHistory;
    }
}
