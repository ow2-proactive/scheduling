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
package functionaltests;

import static com.google.common.truth.Truth.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * This class tests the CLI commands related to the node sources.
 */
public class NodeSourceCommandsFunctTest extends AbstractFunctCmdTest {

    private static final int NB_NODES = 4;

    private static final int NODE_SOURCE_DEPLOYED_WAIT_TIME_MILLIS = 5000;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Init class: " + NodeSourceCommandsFunctTest.class);
        init();
        System.out.println("Finished init class: " + NodeSourceCommandsFunctTest.class);
    }

    @Before
    public void setup() throws Exception {
        super.setUp();
    }

    @Test
    public void testCreateNodeSourceRecoverableParam() throws Exception {
        createNodeSourceAndListNodeSources("nsCreatedThroughCLIRecoverableParam", true);
    }

    @Test
    public void testCreateNotRecoverableNodeSourceNoRecoverableParam() throws Exception {
        createNodeSourceAndListNodeSources("nsCreatedThroughCLINoRecoverableParam", false);
    }

    private void createNodeSourceAndListNodeSources(String nodeSourceName, boolean nodesRecoverableParameter)
            throws Exception {
        String testLogString = "[" + NodeSourceCommandsFunctTest.class.getSimpleName() + "]";

        String nodeSourceInfrastructureClass = "org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure";
        String nodeSourcePolicyClass = "org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy";

        System.out.println(testLogString + "Test createns command");

        if (nodesRecoverableParameter) {
            typeLine("createns( '" + nodeSourceName + "', ['" + nodeSourceInfrastructureClass + "', '" +
                     RestFuncTHelper.getRmCredentialsPath() + "', " + NB_NODES + ", 60000, ''], ['" +
                     nodeSourcePolicyClass + "', 'ALL', 'ALL'], 'tRuE')");
        } else {
            typeLine("createns( '" + nodeSourceName + "', ['" + nodeSourceInfrastructureClass + "', '" +
                     RestFuncTHelper.getRmCredentialsPath() + "', " + NB_NODES + ", 60000, ''], ['" +
                     nodeSourcePolicyClass + "', 'ALL', 'ALL'])");
        }

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println(out);

        waitForNodeSourceToBeDeployed();

        assertThat(out).contains("Node source successfully created.");

        checkOccurrencesOfNodeSourceNameInNodeSourceList(testLogString, nodeSourceName);
    }

    @Test
    public void testDefineAndDeployNodeSourceRecoverableParam() throws Exception {
        defineNodeSourceAndListNodeSources("nsDefinedAndDeployedThroughCLIRecoverableParam", true);
    }

    @Test
    public void testDefineAndDeployNodeSourceNoRecoverableParam() throws Exception {
        defineNodeSourceAndListNodeSources("nsDefinedAndDeployedThroughCLINoRecoverableParam", false);
    }

    private void defineNodeSourceAndListNodeSources(String nodeSourceName, boolean nodesRecoverableParameter)
            throws Exception {
        String testLogString = "[" + NodeSourceCommandsFunctTest.class.getSimpleName() + "]";

        String nodeSourceInfrastructureClass = "org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure";
        String nodeSourcePolicyClass = "org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy";

        System.out.println(testLogString + "Test definens command");

        if (nodesRecoverableParameter) {
            typeLine("definens( '" + nodeSourceName + "', ['" + nodeSourceInfrastructureClass + "', '" +
                     RestFuncTHelper.getRmCredentialsPath() + "', " + NB_NODES + ", 60000, ''], ['" +
                     nodeSourcePolicyClass + "', 'ALL', 'ALL'], 'TrUe')");
        } else {
            typeLine("definens( '" + nodeSourceName + "', ['" + nodeSourceInfrastructureClass + "', '" +
                     RestFuncTHelper.getRmCredentialsPath() + "', " + NB_NODES + ", 60000, ''], ['" +
                     nodeSourcePolicyClass + "', 'ALL', 'ALL'])");
        }

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source successfully defined.");

        System.out.println(testLogString + "Test deployns command");

        clearAndTypeLine("deployns( '" + nodeSourceName + "')");
        runCli();

        out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println(out);

        waitForNodeSourceToBeDeployed();

        assertThat(out).contains("Node source successfully deployed.");

        checkOccurrencesOfNodeSourceNameInNodeSourceList(testLogString, nodeSourceName);

    }

    private void checkOccurrencesOfNodeSourceNameInNodeSourceList(String testLogString, String nodeSourceName) {
        String out;
        System.out.println(testLogString + "Test listns command");

        clearAndTypeLine("listns()");
        runCli();

        out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println(out);

        assertThat(out).contains(nodeSourceName);

        System.out.println(testLogString + "Test listnodes command");

        clearAndTypeLine("listnodes(\"" + nodeSourceName + "\")");
        runCli();

        out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println(out);
        int nbOccurrencesOfNodeSourceNameInNodeList = StringUtils.countMatches(out, nodeSourceName);

        // depending on the time it takes to add the nodes to the RM, the node
        // source name can appear once per node or more per node, whether the
        // node is deploying or registered (in which case the node source name
        // appears also in the URL of the node)
        assertThat(nbOccurrencesOfNodeSourceNameInNodeList).isAtLeast(NB_NODES);
    }

    private void waitForNodeSourceToBeDeployed() {
        try {
            Thread.sleep(NODE_SOURCE_DEPLOYED_WAIT_TIME_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
