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
package functionaltests.db;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.MutableInteger;
import org.ow2.proactive.resourcemanager.core.history.LockHistory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.RMDBManager;

import com.google.common.collect.Sets;


/**
 * @author ActiveEon Team
 * @since 07/02/17
 */
public class RMDBManagerTest {

    private RMDBManager dbManager;

    @Before
    public void setUp() {
        PAResourceManagerProperties.RM_NODES_LOCK_RESTORATION.updateProperty("true");
        PAResourceManagerProperties.RM_ALIVE_EVENT_FREQUENCY.updateProperty("10000");

        dbManager = RMDBManager.createInMemoryRMDBManager();
    }

    @After
    public void tearDown() {
        dbManager.close();
    }

    @Test
    public void testClearLockHistory() {
        insertEntries(32);

        assertThat(dbManager.getLockHistories()).hasSize(32);
        dbManager.clearLockHistory();
        assertThat(dbManager.getLockHistories()).hasSize(0);
    }

    @Test
    public void testCreateLockEntryOrUpdateDisabled() {
        PAResourceManagerProperties.RM_NODES_LOCK_RESTORATION.updateProperty("false");

        insertEntries(10);

        List<LockHistory> lockHistories = dbManager.getLockHistories();

        assertThat(lockHistories).hasSize(0);
    }

    @Test
    public void testCreateLockEntryOrUpdateEnabled() {
        int nbEntries = 42;

        Map<String, Integer> entries = insertEntries(nbEntries);

        List<LockHistory> lockHistories = dbManager.getLockHistories();

        assertThat(lockHistories).hasSize(nbEntries);

        for (int i = 0; i < nbEntries; i++) {
            Integer found = entries.remove("nodeSource" + i);

            assertThat(found).isNotNull();
            assertThat(found).isEqualTo(i);
        }

        assertThat(entries).hasSize(0);
    }

    @Test
    public void testEntityToMapNullInput() {
        assertThat(dbManager.entityToMap(null)).isEmpty();
    }

    @Test
    public void testEntityToMapEmptyCollectionInput() {
        assertThat(dbManager.entityToMap(new ArrayList<LockHistory>())).isEmpty();
    }

    @Test
    public void testEntityToMap() {
        Map<String, Integer> entries = insertEntries(10);

        Map<String, MutableInteger> transformationResult = dbManager.entityToMap(dbManager.getLockHistories());

        assertThat(transformationResult).hasSize(10);

        assertThat(Sets.symmetricDifference(entries.keySet(), transformationResult.keySet())).isNotNull();
    }

    private Map<String, Integer> insertEntries(int nbEntries) {
        Map<String, Integer> entries = new HashMap<>();

        for (int i = 0; i < nbEntries; i++) {
            String nodeSourceName = "nodeSource" + i;
            dbManager.createLockEntryOrUpdate(nodeSourceName, RMDBManager.NodeLockUpdateAction.INCREMENT);
            for (int j = 0; j < i; j++) {
                dbManager.createLockEntryOrUpdate(nodeSourceName, RMDBManager.NodeLockUpdateAction.INCREMENT);
            }

            entries.put(nodeSourceName, i);
        }
        return entries;
    }

}
