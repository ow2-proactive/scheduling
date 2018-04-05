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

    private static final String LOG_HEADER = "[" + NodeSourceCommandsFunctTest.class.getSimpleName() + "]";

    private static final int NB_NODES = 4;

    private static final int MODIFIED_NB_NODES = 6;

    private static final int NODE_SOURCE_DEPLOYED_WAIT_TIME_MILLIS = 5000;

    private static String rmCredentialPath;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Init class: " + NodeSourceCommandsFunctTest.class);
        init();
        rmCredentialPath = RestFuncTHelper.getRmCredentialsPath();
        rmCredentialPath = rmCredentialPath.replace("\\", "\\\\");
        System.out.println("Finished init class: " + NodeSourceCommandsFunctTest.class);
    }

    @Before
    public void setup() throws Exception {
        super.setUp();
    }

    @Test
    public void testCreateNodeSourceRecoverableParam() throws Exception {
        this.createNodeSourceAndListNodeSources("nsCreatedThroughCLIRecoverableParam", true);
    }

    @Test
    public void testCreateNotRecoverableNodeSourceNoRecoverableParam() throws Exception {
        this.createNodeSourceAndListNodeSources("nsCreatedThroughCLINoRecoverableParam", false);
    }

    private void createNodeSourceAndListNodeSources(String nodeSourceName, boolean nodesRecoverableParameter)
            throws Exception {

        String nodeSourceInfrastructureClass = "org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure";
        String nodeSourcePolicyClass = "org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy";

        System.out.println(LOG_HEADER + " Test createns command");

        if (nodesRecoverableParameter) {
            this.typeLine("createns( '" + nodeSourceName + "', ['" + nodeSourceInfrastructureClass + "', '" +
                          rmCredentialPath + "', " + NB_NODES + ", 60000, ''], ['" + nodeSourcePolicyClass +
                          "', 'ALL', 'ALL'], 'tRuE')");
        } else {
            this.typeLine("createns( '" + nodeSourceName + "', ['" + nodeSourceInfrastructureClass + "', '" +
                          rmCredentialPath + "', " + NB_NODES + ", 60000, ''], ['" + nodeSourcePolicyClass +
                          "', 'ALL', 'ALL'])");
        }

        this.runCli();
        this.waitForNodeSourceStatusToChange();

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source successfully created.");

        this.checkOccurrencesOfNodeSourceInNodeSourceList(nodeSourceName);
        this.checkOccurrencesOfNodeSourceInNodesList(nodeSourceName, NB_NODES);
    }

    @Test
    public void testDefineAndDeployNodeSourceRecoverableParam() throws Exception {
        String nodeSourceName = "nsDefinedAndDeployedThroughCLIRecoverableParam";
        this.defineNodeSourceAndCheckOutput(nodeSourceName, true, true);
        this.deployNodeSourceAndCheckOutput(nodeSourceName, NB_NODES);
        this.undeployNodeSourceAndCheckOutput(nodeSourceName, true);
        this.editNodeSourceAndCheckOutput(nodeSourceName);
        this.deployNodeSourceAndCheckOutput(nodeSourceName, MODIFIED_NB_NODES);
    }

    @Test
    public void testDefineAndDeployNodeSourceNoRecoverableParam() throws Exception {
        String nodeSourceName = "nsDefinedAndDeployedThroughCLINoRecoverableParam";
        this.defineNodeSourceAndCheckOutput(nodeSourceName, false, false);
        this.deployNodeSourceAndCheckOutput(nodeSourceName, NB_NODES);
        this.undeployNodeSourceAndCheckOutput(nodeSourceName, false);
        this.editNodeSourceAndCheckOutput(nodeSourceName);
        this.deployNodeSourceAndCheckOutput(nodeSourceName, MODIFIED_NB_NODES);
    }

    private void defineNodeSourceAndCheckOutput(String nodeSourceName, boolean nodesRecoverableParameter,
            boolean preemptUndeploy) throws Exception {

        String nodeSourceInfrastructureClass = "org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure";
        String nodeSourcePolicyClass = "org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy";

        System.out.println(LOG_HEADER + " Test definens command");

        if (nodesRecoverableParameter) {
            this.typeLine("definens( '" + nodeSourceName + "', ['" + nodeSourceInfrastructureClass + "', '" +
                          rmCredentialPath + "', " + NB_NODES + ", 60000, ''], ['" + nodeSourcePolicyClass +
                          "', 'ALL', 'ALL'], 'TrUe')");
        } else {
            this.typeLine("definens( '" + nodeSourceName + "', ['" + nodeSourceInfrastructureClass + "', '" +
                          rmCredentialPath + "', " + NB_NODES + ", 60000, ''], ['" + nodeSourcePolicyClass +
                          "', 'ALL', 'ALL'])");
        }

        this.runCli();
        this.waitForNodeSourceStatusToChange();

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source successfully defined.");
    }

    private void deployNodeSourceAndCheckOutput(String nodeSourceName, int expectedNumberOfNodes) {
        String out;
        System.out.println(LOG_HEADER + " Test deployns command");

        this.clearAndTypeLine("deployns( '" + nodeSourceName + "')");
        this.runCli();
        this.waitForNodeSourceStatusToChange();

        out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source successfully deployed.");

        this.checkOccurrencesOfNodeSourceInNodeSourceList(nodeSourceName);
        this.checkOccurrencesOfNodeSourceInNodesList(nodeSourceName, expectedNumberOfNodes);
    }

    private void undeployNodeSourceAndCheckOutput(String nodeSourceName, boolean preemptUndeploy) {

        System.out.println(LOG_HEADER + "Test undeployns command");

        if (preemptUndeploy) {
            this.clearAndTypeLine("undeployns( '" + nodeSourceName + "', 'true')");
        } else {
            this.clearAndTypeLine("undeployns( '" + nodeSourceName + "')");
        }
        this.runCli();
        this.waitForNodeSourceStatusToChange();

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source successfully undeployed.");

        this.checkOccurrencesOfNodeSourceInNodeSourceList(nodeSourceName);
        this.checkNoOccurrenceOfNodeSourceInNodesList(nodeSourceName);
    }

    private void editNodeSourceAndCheckOutput(String nodeSourceName) {

        String nodeSourceInfrastructureClass = "org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure";
        String nodeSourcePolicyClass = "org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy";

        System.out.println(LOG_HEADER + " Test editns command");

        this.clearAndTypeLine("editns( '" + nodeSourceName + "', ['" + nodeSourceInfrastructureClass + "', '" +
                              rmCredentialPath + "', " + MODIFIED_NB_NODES + ", 60000, ''], ['" +
                              nodeSourcePolicyClass + "', 'ALL', 'ALL'], 'false')");

        this.runCli();
        this.waitForNodeSourceStatusToChange();

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source successfully edited.");
    }

    private void checkOccurrencesOfNodeSourceInNodeSourceList(String nodeSourceName) {

        System.out.println(LOG_HEADER + " List node sources");

        this.clearAndTypeLine("listns()");
        this.runCli();

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains(nodeSourceName);
    }

    private void checkOccurrencesOfNodeSourceInNodesList(String nodeSourceName, int expectedNumberOfNodes) {

        int nbOccurrencesOfNodeSourceNameInNodeList = this.listNodes(nodeSourceName);

        // The name of the node source appears once in the command
        // (hence "+ 1"), and, for each nodes, once in the node name and once 
        // in the node URL (hence 2 times expectedNumberOfNodes)
        assertThat(nbOccurrencesOfNodeSourceNameInNodeList).isEqualTo(expectedNumberOfNodes * 2 + 1);
    }

    private int listNodes(String nodeSourceName) {

        System.out.println(LOG_HEADER + " List nodes");

        this.clearAndTypeLine("listnodes(\"" + nodeSourceName + "\")");
        this.runCli();

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        return StringUtils.countMatches(out, nodeSourceName);
    }

    private void checkNoOccurrenceOfNodeSourceInNodesList(String nodeSourceName) {

        int nbOccurrencesOfNodeSourceNameInNodeList = this.listNodes(nodeSourceName);

        // node source name is only listed once in the command itself
        assertThat(nbOccurrencesOfNodeSourceNameInNodeList).isEqualTo(1);
    }

    private void waitForNodeSourceStatusToChange() {
        try {
            Thread.sleep(NODE_SOURCE_DEPLOYED_WAIT_TIME_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
