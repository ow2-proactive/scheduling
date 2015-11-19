/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.nodestate;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;
import functionaltests.utils.TestUsers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.resourcemanager.utils.TargetType;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.PAProperties;
import org.python.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.List;


/**
 * The purpose of this test is to check that file encoding is forwarded to JVMs
 * created for nodes with an infrastructure manager such as the {@link LocalInfrastructure}.
 * <p>
 * The test deploys a Resource Manager with a specific file encoding, creates a node
 * source and one node using the local infrastructure.
 * Once the node is up and free, a groovy script is executed on the node to get
 * the file encoding from the JVM running the node. This last is expected to be
 * the same as the one set for the JVM running the Resource Manager.
 *
 * @author ProActive Team
 */
public class TestNodeEncoding extends RMFunctionalTest {

    // https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html
    private static final String FILE_ENCODING_NAME = "KOI8-R";

    @Before
    public void setUp() throws Exception {
        // RM needs to be restarted in order to forward
        // the right file encoding
        rmHelper.killRM();
    }

    @Test
    public void test() throws Exception {
        String javaProperty = "-D" + PAProperties.KEY_PA_FILE_ENCODING + "=" + FILE_ENCODING_NAME;

        ResourceManager resourceManager = rmHelper.getResourceManager(TestUsers.TEST, javaProperty);

        String nodeUrl = createNodeSourceWithOneLocalNode();

        List<ScriptResult<Object>> results =
                resourceManager.executeScript(
                        "import org.ow2.proactive.utils.PAProperties; print PAProperties.getFileEncoding()",
                        "groovy", TargetType.NODE_URL.name(),
                        ImmutableSet.of(nodeUrl));

        Assert.assertTrue(!results.isEmpty());
        Assert.assertEquals(FILE_ENCODING_NAME, results.get(0).getOutput());
    }

    @After
    public void tearDown() throws Exception {
        // kill RM to be sure that next tests are
        // not using an instance with wrong encoding
        rmHelper.killRM();
    }

    protected String createNodeSourceWithOneLocalNode() throws Exception {
        byte[] credentials =
                FileToBytesConverter.convertFileToByteArray(
                        new File(PAResourceManagerProperties.getAbsolutePath(
                                PAResourceManagerProperties.RM_CREDS.getValueAsString())));

        String javaProperty =
                "-D" + PAProperties.KEY_PA_FILE_ENCODING + "=" + FILE_ENCODING_NAME;

        rmHelper.getResourceManager(TestUsers.TEST, javaProperty).createNodeSource(
                "testEncoding", LocalInfrastructure.class.getName(),
                new Object[]{
                        credentials,
                        1,
                        RMTHelper.DEFAULT_NODES_TIMEOUT,
                        ""},
                StaticPolicy.class.getName(), null);

        rmHelper.waitForNodeSourceCreation("testEncoding");
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        RMNodeEvent rmNodeEvent = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        String nodeUrl = rmNodeEvent.getNodeUrl();
        logger.info("New node added " + nodeUrl);
        logger.info("Node has state '" + rmNodeEvent.getNodeState() + "'");

        return nodeUrl;
    }

}
