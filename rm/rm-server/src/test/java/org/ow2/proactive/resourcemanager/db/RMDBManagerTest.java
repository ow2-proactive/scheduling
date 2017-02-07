package org.ow2.proactive.resourcemanager.db;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.MutableInteger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;

import com.google.common.collect.Table;


/**
 * @author ActiveEon Team
 * @since 07/02/17
 */
public class RMDBManagerTest {

    private RMDBManager dbManager;

    @Before
    public void setUp() {
        PAResourceManagerProperties.RM_ALIVE_EVENT_FREQUENCY.updateProperty("10000");

        dbManager = RMDBManager.createInMemoryRMDBManager();
    }

    @Test
    public void testGroupNodeUrlsByHostAndNodeSourceDistinctInputEntries() {
        List<Object[]> lockInformation = createLockInformation(10);

        Table<String, String, MutableInteger> table = dbManager.groupNodeUrlsByHostAndNodeSource(lockInformation);

        assertThat(table).hasSize(10);
        assertThat(table.rowKeySet()).hasSize(10);
        assertThat(table.values()).hasSize(10);
    }

    @Test
    public void testGroupNodeUrlsByHostAndNodeSourceNonDistinctInputEntries() {
        List<Object[]> lockInformation = createLockInformation(10);
        lockInformation.add(createTableRow("nodeSource0", "host0", "url0"));

        Table<String, String, MutableInteger> table = dbManager.groupNodeUrlsByHostAndNodeSource(lockInformation);

        assertThat(table).hasSize(10);
        assertThat(table.rowKeySet()).hasSize(10);
        assertThat(table.values()).hasSize(10);
    }

    @Test
    public void testGroupNodeUrlsByHostAndNodeSourceAddNodeUrlLevel() {
        List<Object[]> lockInformation = createLockInformation(10);

        Table<String, String, MutableInteger> table = dbManager.groupNodeUrlsByHostAndNodeSource(lockInformation);

        assertThat(table).hasSize(10);
        assertThat(table.rowKeySet()).hasSize(10);
        assertThat(table.values()).hasSize(10);

        lockInformation.add(createTableRow("nodeSource0", "host0", "urlA"));
        lockInformation.add(createTableRow("nodeSource0", "host0", "urlB"));

        table = dbManager.groupNodeUrlsByHostAndNodeSource(lockInformation);

        assertThat(table).hasSize(10);
        assertThat(table.rowKeySet()).hasSize(10);
        assertThat(table.values()).hasSize(10);

        assertThat(table.get("nodeSource0", "host0").getValue()).isEqualTo(3);
    }

    @Test
    public void testGroupNodeUrlsByHostAndNodeSourceAddHostnameLevel() {
        List<Object[]> lockInformation = createLockInformation(10);

        Table<String, String, MutableInteger> table = dbManager.groupNodeUrlsByHostAndNodeSource(lockInformation);

        assertThat(table).hasSize(10);
        assertThat(table.rowKeySet()).hasSize(10);
        assertThat(table.values()).hasSize(10);

        lockInformation.add(createTableRow("nodeSource0", "hostA", "urlA"));
        lockInformation.add(createTableRow("nodeSource0", "hostB", "urlB"));

        table = dbManager.groupNodeUrlsByHostAndNodeSource(lockInformation);

        assertThat(table).hasSize(12);
        assertThat(table.rowKeySet()).hasSize(10);
        assertThat(table.values()).hasSize(12);
        assertThat(table.row("nodeSource0")).hasSize(3);
    }

    @Test
    public void testGroupNodeUrlsByHostAndNodeSourceAddNodeSourceLevel() {
        List<Object[]> lockInformation = createLockInformation(10);

        Table<String, String, MutableInteger> table = dbManager.groupNodeUrlsByHostAndNodeSource(lockInformation);

        assertThat(table).hasSize(10);
        assertThat(table.rowKeySet()).hasSize(10);
        assertThat(table.values()).hasSize(10);

        lockInformation.add(createTableRow("nodeSourceA", "hostA", "urlA"));
        lockInformation.add(createTableRow("nodeSourceB", "hostB", "urlB"));

        table = dbManager.groupNodeUrlsByHostAndNodeSource(lockInformation);

        assertThat(table).hasSize(12);
        assertThat(table.rowKeySet()).hasSize(12);
        assertThat(table.values()).hasSize(12);
    }

    private List<Object[]> createLockInformation(int nbUniqueRows) {
        List<Object[]> lockInformation = new ArrayList<>(nbUniqueRows);

        for (int i = 0; i < nbUniqueRows; i++) {
            Object[] row = createTableRow("nodeSource" + i, "host" + i, "nodeUrl" + i);

            lockInformation.add(row);
        }

        return lockInformation;
    }

    private Object[] createTableRow(String nodeSource, String hostname, String nodeUrl) {
        return new Object[] { nodeSource, hostname, nodeUrl };
    }

}
