package org.ow2.proactive.resourcemanager.db;

import java.util.Collection;

import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.tests.ProActiveTest;
import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestNodeSources extends ProActiveTest {

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

        NodeSourceData nodeSourceData = new NodeSourceData("ns1", DefaultInfrastructureManager.class
                .getName(), new String[] { "infrastructure" }, StaticPolicy.class.getName(),
            new String[] { "policy" }, new Client(null, false));

        dbManager.addNodeSource(nodeSourceData);
        Collection<NodeSourceData> sources = dbManager.getNodeSources();
        Assert.assertEquals(1, sources.size());

        NodeSourceData source = sources.iterator().next();
        Assert.assertEquals("ns1", source.getName());
        Assert.assertEquals(DefaultInfrastructureManager.class.getName(), source.getInfrastructureType());
        Assert.assertEquals("infrastructure", source.getInfrastructureParameters()[0]);
        Assert.assertEquals(StaticPolicy.class.getName(), source.getPolicyType());
        Assert.assertEquals("policy", source.getPolicyParameters()[0]);
    }

    @Test
    public void removeNodeSource() throws Exception {
        dbManager.removeNodeSource("ns1");
        Collection<NodeSourceData> sources = dbManager.getNodeSources();
        Assert.assertEquals(0, sources.size());
    }

}
