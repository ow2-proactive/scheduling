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
package functionaltests.selectionscript;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.TestNode;


/**
 *
 * This class tests RM's mechanism of resource selection with selection script late binding
 *
 * It creates a temporary script file, and based on its content, expect the resource manager to select nodes or not
 *
 * @author ProActive team
 *
 */
public class LateBindingSelectionScriptTest extends RMFunctionalTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void action() throws Exception {
        File selectionScriptFile = null;
        try {
            ResourceManager resourceManager = rmHelper.getResourceManager();
            int nodeNumber = rmHelper.createNodeSource("LateBindingSelectionScriptTest");

            selectionScriptFile = temp.newFile("test.groovy");
            FileUtils.writeStringToFile(selectionScriptFile, "selected = true", Charset.defaultCharset());

            //create the static selection script object
            SelectionScript sScript = new SelectionScript(selectionScriptFile.toURI().toURL(), "groovy", false);

            log("Test 1 : selection script contains selected = true");
            NodeSet nodes = resourceManager.getAtMostNodes(1, sScript);
            //wait node selection
            PAFuture.waitFor(nodes);
            RMNodeEvent event = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.BUSY, event.getNodeState());
            assertEquals(1, nodes.size());
            assertEquals(nodeNumber - 1, resourceManager.getState().getFreeNodesNumber());

            resourceManager.releaseNodes(nodes);
            event = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.FREE, event.getNodeState());

            log("Test 2 : selection script contains selected = false");

            FileUtils.writeStringToFile(selectionScriptFile, "selected = false", Charset.defaultCharset());

            nodes = resourceManager.getAtMostNodes(1, sScript);

            //wait node selection
            PAFuture.waitFor(nodes);

            assertEquals(0, nodes.size());
            assertEquals(nodeNumber, resourceManager.getState().getFreeNodesNumber());
        } finally {
            if (selectionScriptFile != null) {
                FileUtils.deleteQuietly(selectionScriptFile);
            }
        }
    }
}
