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

import static com.google.common.truth.Truth.assertThat;

import java.util.Collection;

import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.tests.ProActiveTest;


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
        return new NodeSourceData("ns1",
                                  DefaultInfrastructureManager.class.getName(),
                                  new String[] { "infrastructure" },
                                  StaticPolicy.class.getName(),
                                  new String[] { "policy" },
                                  new Client(null, false));
    }

}
