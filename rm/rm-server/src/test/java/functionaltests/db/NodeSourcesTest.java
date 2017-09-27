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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.tests.ProActiveTest;


public class NodeSourcesTest extends ProActiveTest {

    // Arbitrary values used for the test
    private static final String INFRASTRUCTURE_VARIABLE_KEY = "key";

    private static final String INFRASTRUCTURE_VARIABLE_VALUE = "value";

    protected static RMDBManager dbManager;

    private Map<String, Serializable> infrastructureVariables;

    @Before
    public void setUp() throws Exception {
        PAResourceManagerProperties.RM_ALIVE_EVENT_FREQUENCY.updateProperty("100");
        Configuration config = new Configuration().configure("/functionaltests/config/hibernate-unit.cfg.xml");
        dbManager = new RMDBManager(config, true, true);
        infrastructureVariables = new HashMap<>();
        infrastructureVariables.put(INFRASTRUCTURE_VARIABLE_KEY, INFRASTRUCTURE_VARIABLE_VALUE);
    }

    @After
    public void cleanUp() {
        if (dbManager != null) {
            dbManager.close();
        }
    }

    @Test
    public void emptyNodeSource() throws Exception {
        NodeSourceData nodeSourceData = new NodeSourceData();
        try {
            dbManager.addNodeSource(nodeSourceData);
            Assert.fail("Empty node source successfully added");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void addNodeSource() throws Exception {
        NodeSourceData nodeSourceData = createNodeSource();
        nodeSourceData.setInfrastructureVariables(infrastructureVariables);

        assertThat(dbManager.getNodeSources()).isEmpty();

        dbManager.addNodeSource(nodeSourceData);

        Collection<NodeSourceData> nodeSources = dbManager.getNodeSources();
        assertThat(nodeSources).hasSize(1);

        NodeSourceData nodeSource = nodeSources.iterator().next();
        Assert.assertEquals("ns1", nodeSource.getName());
        Assert.assertEquals(DefaultInfrastructureManager.class.getName(), nodeSource.getInfrastructureType());
        Assert.assertEquals("infrastructure", nodeSource.getInfrastructureParameters()[0]);
        Assert.assertEquals(StaticPolicy.class.getName(), nodeSource.getPolicyType());
        Assert.assertEquals("policy", nodeSource.getPolicyParameters()[0]);
        assertThat(nodeSource.getInfrastructureVariables()).hasSize(1);
        Assert.assertEquals(INFRASTRUCTURE_VARIABLE_VALUE,
                            nodeSource.getInfrastructureVariables().get(INFRASTRUCTURE_VARIABLE_KEY));
    }

    @Test
    public void removeNodeSource() throws Exception {
        dbManager.addNodeSource(createNodeSource());

        assertThat(dbManager.getNodeSources()).hasSize(1);

        dbManager.removeNodeSource("ns1");

        assertThat(dbManager.getNodeSources()).isEmpty();
    }

    @Test
    public void testAddNodeSourceWithEmptyInfrastructureVariables() {
        NodeSourceData nodeSourceData = createNodeSource();
        nodeSourceData.setInfrastructureVariables(new HashMap<String, Serializable>());
        dbManager.addNodeSource(nodeSourceData);

        Collection<NodeSourceData> nodeSources = dbManager.getNodeSources();
        assertThat(nodeSources).hasSize(1);

        NodeSourceData nodeSource = nodeSources.iterator().next();
        assertThat(nodeSource.getInfrastructureVariables()).hasSize(0);
    }

    @Test
    public void testAddNodeSourceWithVariousObjectsInInfrastructureVariables() {
        NodeSourceData nodeSourceData = createNodeSource();
        Map<String, Serializable> infrastructureVariables = new HashMap<>();

        Integer integer = new Integer(42);
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        char c = 'z';
        infrastructureVariables.put("anInteger", integer);
        infrastructureVariables.put("aBoolean", atomicBoolean);
        infrastructureVariables.put("aChar", c);

        nodeSourceData.setInfrastructureVariables(infrastructureVariables);
        dbManager.addNodeSource(nodeSourceData);

        Collection<NodeSourceData> nodeSources = dbManager.getNodeSources();
        assertThat(nodeSources).hasSize(1);

        NodeSourceData nodeSource = nodeSources.iterator().next();
        Map<String, Serializable> retrievedInfravariables = nodeSource.getInfrastructureVariables();
        assertThat(retrievedInfravariables).hasSize(3);
        assertThat(retrievedInfravariables.get("anInteger")).isEqualTo(integer);
        // works with autoboxing
        assertThat(retrievedInfravariables.get("anInteger")).isEqualTo(42);
        assertThat(((AtomicBoolean) retrievedInfravariables.get("aBoolean")).get()).isEqualTo(atomicBoolean.get());
        assertThat(retrievedInfravariables.get("aChar")).isEqualTo(c);
    }

    private NodeSourceData createNodeSource() {
        return new NodeSourceData("ns1",
                                  DefaultInfrastructureManager.class.getName(),
                                  new String[] { "infrastructure" },
                                  StaticPolicy.class.getName(),
                                  new String[] { "policy" },
                                  new Client(null, false));
    }

}
