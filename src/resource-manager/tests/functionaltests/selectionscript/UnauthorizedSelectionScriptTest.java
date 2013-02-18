/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionaltests.selectionscript;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;


/**
 * Test checks that only authorized scripts can be executed in the resource manager
 * if property pa.rm.select.script.authorized.dir is set
 */
public class UnauthorizedSelectionScriptTest extends RMConsecutive {

    @org.junit.Test
    public void action() throws Exception {
        String rmconf = new File(
            PAResourceManagerProperties
                    .getAbsolutePath("src/resource-manager/tests/functionaltests/config/rm-authorized-selection-script.ini"))
                .getAbsolutePath();

        RMTHelper helper = RMTHelper.getDefaultInstance();
        helper.startRM(rmconf, CentralPAPropertyRepository.PA_RMI_PORT.getValue());
        ResourceManager rm = helper.getResourceManager();

        helper.createNodeSource("Dummy", 1);

        RMTHelper.log("Test 1 - unautorized script");
        Criteria criteria = new Criteria(1);

        URL vmPropSelectionScriptpath = this.getClass().getResource("dummySelectionScript.js");
        List<SelectionScript> scripts = new ArrayList<SelectionScript>();
        scripts.add(new SelectionScript(new File(vmPropSelectionScriptpath.toURI()), null, true));
        criteria.setScripts(scripts);
        try {
            NodeSet ns = rm.getNodes(criteria);
            System.out.println("Number of nodes matched " + ns.size());
            Assert.fail("Executed unauthorized selection script");
        } catch (SecurityException ex) {
            RMTHelper.log("Test 1 - passed");
        }

        RMTHelper.log("Test 2 - autorized script");

        String authorizedScriptPath = PAResourceManagerProperties.RM_HOME.getValueAsString() +
            "/samples/scripts/selection/checkPhysicalFreeMem.js";
        scripts = new ArrayList<SelectionScript>();
        scripts.add(new SelectionScript(new File(authorizedScriptPath), new String[] { "1" }, true));
        criteria.setScripts(scripts);
        NodeSet ns = rm.getNodes(criteria);
        System.out.println("Number of nodes matched " + ns.size());
        Assert.assertEquals(1, ns.size());

        RMTHelper.log("Test 2 - passed");
    }
}
