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
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMFunctionalTest;


/**
 * Test checks that only authorized scripts can be executed in the resource manager
 * if property pa.rmHelper.select.script.authorized.dir is set
 */
public class UnauthorizedSelectionScriptTest extends RMFunctionalTest {

    @Test
    public void action() throws Exception {
        String rmconf = new File(PAResourceManagerProperties.getAbsolutePath(getClass().getResource("/functionaltests/config/rm-authorized-selection-script.ini")
                                                                                       .getFile())).getAbsolutePath();

        rmHelper.startRM(rmconf);
        ResourceManager rm = this.rmHelper.getResourceManager();

        this.rmHelper.createNodeSource("Dummy", 1);

        String authorizedScriptPath = PAResourceManagerProperties.RM_HOME.getValueAsString() +
                                      "/samples/scripts/selection/checkPhysicalFreeMem.js";
        URL unauthorizedSelectionScriptpath = this.getClass().getResource("dummySelectionScript.js");

        log("Test 1 - unautorized script");
        Criteria criteria = new Criteria(1);
        List<SelectionScript> scripts = new ArrayList<>();
        scripts.add(new SelectionScript(new File(unauthorizedSelectionScriptpath.toURI()), null, true));
        criteria.setScripts(scripts);

        NodeSet ns = rm.getNodes(criteria);
        System.out.println("Number of nodes matched " + ns.size());
        Assert.assertEquals("No nodes should be selected by the unauthorized script", 0, ns.size());
        log("Test 1 - passed");

        log("Test 2 - authorized script");

        criteria = new Criteria(1);
        scripts = new ArrayList<>();
        scripts.add(new SelectionScript(new File(authorizedScriptPath), new String[] { "1" }, true));
        criteria.setScripts(scripts);
        ns = rm.getNodes(criteria);
        System.out.println("Number of nodes matched " + ns.size());
        assertEquals(1, ns.size());

        rm.releaseNodes(ns);
        log("Test 2 - passed");

        log("Test 3 - list with authorized and unauthorized scripts");
        criteria = new Criteria(1);
        scripts = new ArrayList<>();
        scripts.add(new SelectionScript(new File(unauthorizedSelectionScriptpath.toURI()), null, true));
        scripts.add(new SelectionScript(new File(authorizedScriptPath), new String[] { "1" }, true));
        criteria.setScripts(scripts);
        ns = rm.getNodes(criteria);
        System.out.println("Number of nodes matched " + ns.size());
        Assert.assertEquals("No nodes should be selected by the unauthorized script", 0, ns.size());

        log("Test 3 - passed");
    }
}
