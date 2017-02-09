package org.ow2.proactive.resourcemanager.db;

import java.util.Collection;

import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.tests.ProActiveTest;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;


public class NodeSourcesTest extends ProActiveTest {

    protected static RMDBManager dbManager;

    @Before
    public void setUp() throws Exception {
        PAResourceManagerProperties.RM_ALIVE_EVENT_FREQUENCY.updateProperty("100");
        Configuration config = new Configuration().configure("/functionaltests/config/hibernate.cfg.xml");
        dbManager = new RMDBManager(config, true, true);
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
    }

    @Test
    public void removeNodeSource() throws Exception {
        dbManager.addNodeSource(createNodeSource());

        assertThat(dbManager.getNodeSources()).hasSize(1);

        dbManager.removeNodeSource("ns1");

        assertThat(dbManager.getNodeSources()).isEmpty();
    }

    private NodeSourceData createNodeSource() {
        return new NodeSourceData("ns1", DefaultInfrastructureManager.class
                .getName(), new String[] { "infrastructure" }, StaticPolicy.class.getName(),
                new String[] { "policy" }, new Client(null, false));
    }

}
