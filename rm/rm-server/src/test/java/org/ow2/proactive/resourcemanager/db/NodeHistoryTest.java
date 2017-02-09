package org.ow2.proactive.resourcemanager.db;

import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.core.history.NodeHistory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;

import java.util.List;


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
