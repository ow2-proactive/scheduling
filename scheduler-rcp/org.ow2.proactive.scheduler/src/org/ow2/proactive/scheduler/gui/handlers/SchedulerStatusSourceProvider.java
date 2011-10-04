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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;


/**
 * A source provider which give information on the Scheduler Current Status. It
 * provides "YES"/"NO" values for the variables CAN_START_SCHEDULER and
 * CAN_STOP_SCHEDULER.
 * <p>
 * If The current perspective is not the Scheduler Perspective, both CAN_START_SCHEDULER and CAN_STOP_SCHEDULER are set to "NO". 
 *  
 *  It is used in the plugin.xml file, by the
 * org.eclipse.ui.services extension. The values provided are used in order to
 * show or hide the Start/Stop scheduler commands. 
 * 
 * @author esalagea
 * 
 */
public class SchedulerStatusSourceProvider extends AbstractSourceProvider {

    public final static String CAN_START_SCHEDULER = "org.ow2.proactive.scheduler.gui.handlers.canstart";
    public final static String CAN_STOP_SCHEDULER = "org.ow2.proactive.scheduler.gui.handlers.canstop";

    public final static String YES = "YES";
    public final static String NO = "NO";

    private boolean isSchedPerspective = false;
    private boolean canStart = false;
    private boolean canStop = false;

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public Map getCurrentState() {
        Map<String, String> map = new HashMap<String, String>(1);
        String value = canStart && isSchedPerspective ? YES : NO;
        map.put(CAN_START_SCHEDULER, value);

        String value1 = canStop && isSchedPerspective ? YES : NO;
        map.put(CAN_STOP_SCHEDULER, value);
        map.put(CAN_STOP_SCHEDULER, value1);

        return map;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return new String[] { CAN_START_SCHEDULER, CAN_STOP_SCHEDULER };
    }

    public void setCanStart(boolean can_start) {
        if (this.canStart == can_start)
            return;

        this.canStart = can_start;
        fireSourceChanged(ISources.WORKBENCH, CAN_START_SCHEDULER, canStart && isSchedPerspective ? YES : NO);
    }

    public void setCanStop(boolean can_stop) {
        if (this.canStop == can_stop)
            return;

        this.canStop = can_stop;
        fireSourceChanged(ISources.WORKBENCH, CAN_STOP_SCHEDULER, canStop && isSchedPerspective ? YES : NO);
    }

    public void perspectiveChanged(boolean isSchedulerPerspective) {
        this.isSchedPerspective = isSchedulerPerspective;
        fireSourceChanged(ISources.WORKBENCH, CAN_STOP_SCHEDULER, canStop && isSchedPerspective ? YES : NO);
        fireSourceChanged(ISources.WORKBENCH, CAN_STOP_SCHEDULER, canStop && isSchedPerspective ? YES : NO);
    }
}
