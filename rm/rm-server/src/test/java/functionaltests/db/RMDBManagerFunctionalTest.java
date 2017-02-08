package functionaltests.db;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.db.SessionWork;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.core.history.NodeHistory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.RMDBManager;


/**
 * @author ActiveEon Team
 * @since 07/02/17
 */
public class RMDBManagerFunctionalTest {

    private static final String DB_CONFIGURATION_FILE = "/functionaltests/config/hibernate-rm.cfg.xml";

    @Before
    public void setUp() {
        PAResourceManagerProperties.RM_ALIVE_EVENT_FREQUENCY.updateProperty("10000");
    }

    @Test
    public void testGetNodesLockedOnPreviousRun() {

        final RMDBManager dbManager = RMDBManager.createInMemoryRMDBManager();

        long now = System.currentTimeMillis();
        long lastRmStartupTime = 0;

        // put 100 node history events in the database
        for (int i = 1; i <= 100; i++) {
             NodeHistory nodeHistory = createSequentialNodeHistory(now, i);

            if (i % 3 == 0) {
                nodeHistory.setNodeState(NodeState.LOST);
            }

            if (i % 7 == 0) {
                nodeHistory.setNodeState(NodeState.DOWN);
            }

            // half of the entries are locked
            if (i % 2 == 0) {
                nodeHistory.setLocked(true);
            }

            // 20 entries have a end time set
            if (i > 80) {
                nodeHistory.setEndTime(now + (i * 10));
            }

            if (i == 90) {
                lastRmStartupTime = nodeHistory.getEndTime() - 1;
            }

            dbManager.saveNodeHistory(nodeHistory);
        }

        assertThat(lastRmStartupTime).isGreaterThan(0L);

        NodeHistory additionalEntry = createSequentialNodeHistory(now, 98);
        additionalEntry.setEndTime(now + (98 * 10));
        additionalEntry.setLocked(true);
        additionalEntry.setNodeState(NodeState.LOST);
        additionalEntry.setNodeUrl("nodeUrlA");
        dbManager.saveNodeHistory(additionalEntry);

        Integer nbEntriesInDatabase = dbManager.executeReadTransaction(new SessionWork<Integer>() {
            @Override
            public Integer doInTransaction(Session session) {
                return session.createQuery("from NodeHistory").list().size();
            }
        });

        assertThat(nbEntriesInDatabase).isEqualTo(101);

        List<Object[]> nodesLockedOnPreviousRun = dbManager.findNodesLockedOnPreviousRun(lastRmStartupTime);
        assertThat(nodesLockedOnPreviousRun).hasSize(7);

        dbManager.close();
    }

    private NodeHistory createSequentialNodeHistory(long now, int i) {
        NodeHistory nodeHistory = new NodeHistory();
        nodeHistory.setNodeSource("nodeSource" + i);
        nodeHistory.setHost("host" + i);
        nodeHistory.setNodeUrl("nodeUrl" + i);
        nodeHistory.setNodeState(NodeState.FREE);
        nodeHistory.setProviderName("Grand filou");
        nodeHistory.setUserName("Petit filou");
        nodeHistory.setStartTime(now);
        nodeHistory.setStoreInDataBase(true);
        return nodeHistory;
    }

    @Test
    public void testRmLastStartupTimeUpdate() throws Exception {

        RMDBManager dbManager = new RMDBManager(new Configuration().configure(DB_CONFIGURATION_FILE), true, true);

        try {
            long initializationTime = System.currentTimeMillis();

            // on first start, value is undefined
            assertThat(dbManager.getRmLastStartupTime()).isEqualTo(0L);

            dbManager.close();

            // on restart, the value must be defined
            dbManager = new RMDBManager(new Configuration().configure(DB_CONFIGURATION_FILE),
                                        false,
                                        false);

            long rmLastStartupTime = dbManager.getRmLastStartupTime();
            assertThat(rmLastStartupTime).isGreaterThan(0L);
            assertThat(rmLastStartupTime).isLessThan(initializationTime);
        } finally {
            dbManager.close();
        }
    }

}
