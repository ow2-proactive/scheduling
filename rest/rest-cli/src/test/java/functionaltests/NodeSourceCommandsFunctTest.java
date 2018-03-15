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
        String testLogString = "[" + NodeSourceCommandsFunctTest.class.getSimpleName() + "]";

        String nodeSourceInfrastructureClass = "org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure";
        String nodeSourcePolicyClass = "org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy";

        System.out.println(testLogString + " Test createns command");

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

        this.checkOccurrencesOfNodeSourceInNodeSourceList(testLogString, nodeSourceName);
        this.checkOccurrencesOfNodeSourceInNodesList(testLogString, nodeSourceName);
    }

    @Test
    public void testDefineAndDeployNodeSourceRecoverableParam() throws Exception {
        this.defineNodeSourceAndListNodeSources("nsDefinedAndDeployedThroughCLIRecoverableParam", true, true);
    }

    @Test
    public void testDefineAndDeployNodeSourceNoRecoverableParam() throws Exception {
        this.defineNodeSourceAndListNodeSources("nsDefinedAndDeployedThroughCLINoRecoverableParam", false, false);
    }

    private void defineNodeSourceAndListNodeSources(String nodeSourceName, boolean nodesRecoverableParameter,
            boolean preemptUndeploy) throws Exception {
        String testLogString = "[" + NodeSourceCommandsFunctTest.class.getSimpleName() + "]";

        String nodeSourceInfrastructureClass = "org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure";
        String nodeSourcePolicyClass = "org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy";

        System.out.println(testLogString + " Test definens command");

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

        System.out.println(testLogString + " Test deployns command");

        this.clearAndTypeLine("deployns( '" + nodeSourceName + "')");
        this.runCli();
        this.waitForNodeSourceStatusToChange();

        out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source successfully deployed.");

        this.checkOccurrencesOfNodeSourceInNodeSourceList(testLogString, nodeSourceName);
        this.checkOccurrencesOfNodeSourceInNodesList(testLogString, nodeSourceName);

        System.out.println(testLogString + "Test undeployns command");

        if (preemptUndeploy) {
            this.clearAndTypeLine("undeployns( '" + nodeSourceName + "', 'true')");
        } else {
            this.clearAndTypeLine("undeployns( '" + nodeSourceName + "')");
        }
        this.runCli();
        this.waitForNodeSourceStatusToChange();

        out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains("Node source successfully undeployed.");

        this.checkOccurrencesOfNodeSourceInNodeSourceList(testLogString, nodeSourceName);
        this.checkNoOccurrenceOfNodeSourceInNodesList(testLogString, nodeSourceName);
    }

    private void checkOccurrencesOfNodeSourceInNodeSourceList(String testLogString, String nodeSourceName) {
        System.out.println(testLogString + " List node sources");

        this.clearAndTypeLine("listns()");
        this.runCli();

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);

        assertThat(out).contains(nodeSourceName);
    }

    private void checkOccurrencesOfNodeSourceInNodesList(String testLogString, String nodeSourceName) {

        int nbOccurrencesOfNodeSourceNameInNodeList = this.listNodes(testLogString, nodeSourceName);

        assertThat(nbOccurrencesOfNodeSourceNameInNodeList).isAtLeast(NB_NODES);
    }

    private int listNodes(String testLogString, String nodeSourceName) {
        System.out.println(testLogString + " List nodes");

        this.clearAndTypeLine("listnodes(\"" + nodeSourceName + "\")");
        this.runCli();

        String out = this.capturedOutput.toString();
        System.setOut(this.stdOut);
        System.out.println(out);
        return StringUtils.countMatches(out, nodeSourceName);
    }

    private void checkNoOccurrenceOfNodeSourceInNodesList(String testLogString, String nodeSourceName) {

        int nbOccurrencesOfNodeSourceNameInNodeList = this.listNodes(testLogString, nodeSourceName);

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
