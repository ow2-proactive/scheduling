/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package selectionscript;

import java.io.File;

import nodestate.FunctionalTDefaultRM;
import nodestate.RMEventReceiver;

import org.junit.Assert;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


/**
 * Test timeout in SelectionScriptExecution
 * launch a selection script that always says 'selected',
 * but before performs a Thread.sleep() two time longer 
 * than defined selection script max waiting time for selection
 * script execution. So no nodes a got for this selection script.  
 * 
 * @author ProActice team
 *
 */
public class SelectionScriptTimeOutTest extends FunctionalTDefaultRM {

    private String selectionScriptWithtimeOutPath = this.getClass().getResource(
            "selectionScriptWithtimeOut.js").getPath();

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework. 
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        log("Deployment");

        System.out.println(monitor.isAlive());
        System.out.println(admin.isAlive());

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODE_REMOVED,
                RMEventType.NODESOURCE_CREATED, RMEventType.NODE_STATE_CHANGED };

        RMEventReceiver receiver = (RMEventReceiver) PAActiveObject.newActive(
                RMEventReceiver.class.getName(), new Object[] { monitor, eventsList });

        receiver.cleanEventLists();
        super.deployDefault();

        //wait for creation of GCM Node Source event, and deployment of its nodes
        receiver.waitForNEvent(defaultDescriptorNodesNb + 1);
        receiver.cleanEventLists();

        int scriptSleepingTime = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT.getValueAsInt() * 2;

        log("Test 1");

        //create the static selection script object
        SelectionScript sScript = new SelectionScript(new File(selectionScriptWithtimeOutPath),
            new String[] { Integer.toString(scriptSleepingTime) }, false);

        NodeSet nodes = admin.getAtMostNodes(2, sScript);

        //wait node selection
        try {
            PAFuture.waitFor(nodes);
            System.out.println("Number of found nodes " + nodes.size());
            Assert.assertTrue(false);
        } catch (RuntimeException e) {
        }

        Assert.assertEquals(defaultDescriptorNodesNb, admin.getFreeNodesNumber().intValue());
    }
}
