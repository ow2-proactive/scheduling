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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import org.objectweb.proactive.core.util.CircularArrayList;


public class JobOutput {

    private CircularArrayList<String> currentLogs;
    private CircularArrayList<String> allLogs;

    public JobOutput() {
        currentLogs = new CircularArrayList<>(50);
        allLogs = new CircularArrayList<>(50);
    }

    public synchronized void log(String message) {
        currentLogs.add(message);
        allLogs.add(message);
    }

    /**
     * @return the logs appended since last call, i.e logs are removed when fetched
     */
    public synchronized String fetchNewLogs() {
        int size = currentLogs.size();
        StringBuilder mes = new StringBuilder();
        for (int i = 0; i < size; i++) {
            mes.append(currentLogs.remove(0));
        }
        return mes.toString();
    }

    /**
     * @return all the logs since creation, i.e logs are not removed
     */
    public synchronized String fetchAllLogs() {
        StringBuilder mes = new StringBuilder();
        for (String aSavedOutput : allLogs) {
            mes.append(aSavedOutput);
        }
        return mes.toString();
    }

    public synchronized int size() {
        return currentLogs.size();
    }
}
