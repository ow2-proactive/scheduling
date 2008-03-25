/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Contributor(s):
 *
 * ################################################################
 */
//@snippet-start webservice_cma_full
package org.objectweb.proactive.examples.userguide.cmagent.webservice;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.userguide.cmagent.initialized.CMAgentInitialized;
import org.objectweb.proactive.extensions.webservices.WebServices;


public class CMAgentService extends CMAgentInitialized {

    public static void main(String[] args) {
        String url = "http://localhost:8080";
        System.out.println("Started a monitoring agent on : " + url);
        try {
            CMAgentService hw = (CMAgentService) PAActiveObject.newActive(
                    "org.objectweb.proactive.examples.userguide.cmagent.webservice.CMAgentService",
                    new Object[] {});

            //TODO 1.
            /*******************************************************/
            /* Expose as web service (on URL 'url') the methods   
            /* "getLastRequestServeTime" and "getCurrentState" 
            /* of 'hw' CMAgentService. Name your service  "cmAgentService"*/
            /*******************************************************/
            //@snippet-start ws_call
            WebServices.exposeAsWebService(hw, url, "cmAgentService", new String[] {
                    "getLastRequestServeTime", "getCurrentState" });
            //@snippet-end ws_call

        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

}
//@snippet-end webservice_cma_full
