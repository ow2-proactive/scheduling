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

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;


/**
 * This class tests the CLI commands related to the node sources.
 */
public class NodeSourceCommandsFunctTest extends AbstractFunctCmdTest {

    @Rule
    public Timeout testTimeout = new Timeout(CentralPAPropertyRepository.PA_TEST_TIMEOUT.getValue(),
                                             TimeUnit.MILLISECONDS);

    private static final String LOG_HEADER = "[" + NodeSourceCommandsFunctTest.class.getSimpleName() + "]";

    private static final String INFRASTRUCTURE_CLASS = LocalInfrastructure.class.getCanonicalName();

    private static final String STATIC_POLICY_CLASS = StaticPolicy.class.getCanonicalName();

    private static final String RESTART_DOWN_NODES_POLICY_CLASS = RestartDownNodesPolicy.class.getCanonicalName();

    private static final int NUMBER_OF_NODES = 4;

    private static final int MODIFIED_NUMBER_OF_NODES = 6;

    private static final int NODES_TIMEOUT = 60000;

    private static final int WAIT_FOR_FREE_NODES_DURATION = 5;

    private static String rmCredentialPath;

    private static String initialInfrastructureParametersString;

    private static String modifiedInfrastructureParametersString;

    private static String staticPolicyParametersString;

    private static String restartDownNodesPolicyParametersString;

    @BeforeClass
    public static void beforeClass() throws Exception {

        System.out.println("Init class: " + NodeSourceCommandsFunctTest.class);

        init();

        rmCredentialPath = RestFuncTHelper.getRmCredentialsPath();
        rmCredentialPath = rmCredentialPath.replace("\\", "\\\\");

        initialInfrastructureParametersString = getInfrastructureParametersString(NUMBER_OF_NODES);
        modifiedInfrastructureParametersString = getInfrastructureParametersString(MODIFIED_NUMBER_OF_NODES);
        staticPolicyParametersString = getStaticPolicyParametersString();
        restartDownNodesPolicyParametersString = getRestartDownNodesPolicyParametersString();

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

        System.out.println(LOG_HEADER + " Test createns command");

        if (nodesRecoverableParameter) {
            this.typeLine("createns( '" + nodeSourceName + "', " + initialInfrastructureParametersString + ", " +
                          staticPolicyParametersString + ", 'tRuE')");
        } else {
            this.typeLine("createns( '" + nodeSourceName + "', " + initialInfrastructureParametersString + ", " +
                          staticPolicyParametersString + ")");
        }

        this.runCli();

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source successfully created.");

        this.checkOccurrencesOfNodeSourceInNodeSourceList(nodeSourceName);
        this.checkOccurrencesOfNodeSourceInNodesList(nodeSourceName, NUMBER_OF_NODES);
    }

    @Test
    public void testDefineAndDeployNodeSourceRecoverableParamWithStaticPolicy() throws Exception {
        String nodeSourceName = "nsDefinedAndDeployedThroughCLIRecoverableParam";
        this.defineNodeSourceAndCheckOutput(nodeSourceName, staticPolicyParametersString, true, true);
        this.deployNodeSourceAndCheckOutput(nodeSourceName, NUMBER_OF_NODES);
        this.undeployNodeSourceAndCheckOutput(nodeSourceName, true);
        this.editNodeSourceAndCheckOutput(nodeSourceName, staticPolicyParametersString);
        this.deployNodeSourceAndCheckOutput(nodeSourceName, MODIFIED_NUMBER_OF_NODES);
    }

    @Test
    public void testDefineAndDeployNodeSourceNoRecoverableParamWithRestartDownNodesPolicy() throws Exception {
        String nodeSourceName = "nsDefinedAndDeployedThroughCLINoRecoverableParam";
        this.defineNodeSourceAndCheckOutput(nodeSourceName, restartDownNodesPolicyParametersString, false, false);
        this.deployNodeSourceAndCheckOutput(nodeSourceName, NUMBER_OF_NODES);
        this.undeployNodeSourceAndCheckOutput(nodeSourceName, false);
        this.editNodeSourceAndCheckOutput(nodeSourceName, restartDownNodesPolicyParametersString);
        this.deployNodeSourceAndCheckOutput(nodeSourceName, MODIFIED_NUMBER_OF_NODES);
        this.updateDynamicParametersAndCheckOutput(nodeSourceName);
    }

    private void defineNodeSourceAndCheckOutput(String nodeSourceName, String policyParametersString,
            boolean nodesRecoverableParameter, boolean preemptUndeploy) throws Exception {

        System.out.println(LOG_HEADER + " Test definens command");

        if (nodesRecoverableParameter) {
            this.typeLine("definens( '" + nodeSourceName + "', " + initialInfrastructureParametersString + ", " +
                          policyParametersString + ", 'TrUe')");
        } else {
            this.typeLine("definens( '" + nodeSourceName + "', " + initialInfrastructureParametersString + ", " +
                          policyParametersString + ")");
        }

        this.runCli();

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

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source successfully undeployed.");

        this.checkOccurrencesOfNodeSourceInNodeSourceList(nodeSourceName);
        this.checkNoOccurrenceOfNodeSourceInNodesList(nodeSourceName);
    }

    private void editNodeSourceAndCheckOutput(String nodeSourceName, String policyParametersString) {

        System.out.println(LOG_HEADER + " Test editns command");

        this.clearAndTypeLine("editns( '" + nodeSourceName + "', " + modifiedInfrastructureParametersString + ", " +
                              policyParametersString + ", 'false')");

        this.runCli();

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source successfully edited.");
    }

    private void updateDynamicParametersAndCheckOutput(String nodeSourceName) {

        System.out.println(LOG_HEADER + " Test updatensparam command");

        this.clearAndTypeLine("updatensparam( '" + nodeSourceName + "', " + modifiedInfrastructureParametersString +
                              ", " + restartDownNodesPolicyParametersString + ")");

        this.runCli();

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source dynamic parameters successfully updated.");
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

        int nbOccurrencesOfNodeSourceNameInNodeList = this.listNodes(nodeSourceName, expectedNumberOfNodes);

        // The name of the node source appears once in the command
        // (hence "+ 1"), and, for each nodes, the node source name appears
        // once in the node name and once in the node URL
        // (hence 2 times expectedNumberOfNodes)
        assertThat(nbOccurrencesOfNodeSourceNameInNodeList).isEqualTo(expectedNumberOfNodes * 2 + 1);
    }

    private int listNodes(String nodeSourceName, int expectedNumberOfNodes) {

        System.out.println(LOG_HEADER + " List nodes");

        String output;

        do {

            output = waitForFreeNodesAndList(nodeSourceName);

        } while (StringUtils.countMatches(output, "FREE") != expectedNumberOfNodes);

        return StringUtils.countMatches(output, nodeSourceName);
    }

    private String waitForFreeNodesAndList(String nodeSourceName) {

        try {

            TimeUnit.SECONDS.sleep(WAIT_FOR_FREE_NODES_DURATION);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.clearAndTypeLine("listnodes(\"" + nodeSourceName + "\")");
        this.runCli();

        String output = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(output);
        return output;
    }

    private void checkNoOccurrenceOfNodeSourceInNodesList(String nodeSourceName) {

        int nbOccurrencesOfNodeSourceNameInNodeList = this.listNodes(nodeSourceName, 0);

        // node source name is only listed once in the command itself
        assertThat(nbOccurrencesOfNodeSourceNameInNodeList).isEqualTo(1);
    }

    private static String getInfrastructureParametersString(int numberOfNodes) {
        return String.format("['%s', '%s', %o, %o, '']",
                             INFRASTRUCTURE_CLASS,
                             rmCredentialPath,
                             numberOfNodes,
                             NODES_TIMEOUT);
    }

    private static String getStaticPolicyParametersString() {
        return String.format("['%s', '%s', '%s']", STATIC_POLICY_CLASS, "ALL", "ALL");
    }

    private static String getRestartDownNodesPolicyParametersString() {
        return String.format("['%s', '%s', '%s', %s]", RESTART_DOWN_NODES_POLICY_CLASS, "ALL", "ALL", 5000);
    }

}
