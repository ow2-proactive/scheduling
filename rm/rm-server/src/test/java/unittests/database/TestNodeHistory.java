package unittests.database;

import java.util.List;

import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.core.history.NodeHistory;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.tests.ProcessCleaner;


public class TestNodeHistory {

    protected RMDBManager dbManager;

    @Before
    public void initDB() throws Exception {
        // cleaning all other processes that can access the database
        new ProcessCleaner(".*proactive.test=true.*").killAliveProcesses();
        Configuration config = new Configuration().configure("/functionaltests/config/hibernate.cfg.xml");
        dbManager = new RMDBManager(config, true, true);
    }

    @After
    public void cleanup() {
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

        List<?> rows = dbManager.sqlQuery("from NodeHistory");
        Assert.assertEquals(1, rows.size());

        NodeHistory actual = (NodeHistory) rows.get(0);
        assertEquals(expected, actual);
    }

    public void testUpdateHistory() throws Exception {

        NodeHistory expected1 = createNodeHistory(1);
        expected1.setEndTime(2);
        NodeHistory expected2 = createNodeHistory(2);

        dbManager.saveNodeHistory(expected2);

        List<?> rows = dbManager.sqlQuery("from NodeHistory");
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
