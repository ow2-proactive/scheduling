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
package org.ow2.proactive.resourcemanager.integration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;


/**
 * Provides a key and a value: the key is
 * org.ow2.proactive.resourcemanager.ShowActionsl;  the value is: showRMActions
 * (if the current perspective is RMPerspective) or hideRMActions (otherwise)
 * The current perspective needs to be updates via the perspectiveChanged method
 * 
 * This provider is used in the plugin.xml in order to decide to show or not rm
 * specific workbench contributions (toolbar buttons, menu entries)
 * 
 * @author esalagea
 * 
 */
public class RMPerspectiveSourceProvider extends AbstractSourceProvider {

    public final static String rmPerspectiveKey = "org.ow2.proactive.resourcemanager.ShowActions";
    private final static String rmPerspective = "showRMActions";
    private final static String otherPerspective = "hideRMActions";

    /**
     * true if the current perspective is the Resource Manager Perspective
     */
    private boolean isRmPerspective = true;

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public Map getCurrentState() {
        Map<String, String> currentState = new HashMap<String, String>(1);
        String currentState1 = isRmPerspective ? rmPerspective : otherPerspective;
        currentState.put(rmPerspectiveKey, currentState1);
        return currentState;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return new String[] { rmPerspectiveKey };
    }

    public void perspectiveChanged(boolean isRMPerspective) {
        if (this.isRmPerspective == isRMPerspective)
            return; // no change

        this.isRmPerspective = isRMPerspective;
        String currentState = isRmPerspective ? rmPerspective : otherPerspective;
        fireSourceChanged(ISources.WORKBENCH, rmPerspectiveKey, currentState);
    }

}
