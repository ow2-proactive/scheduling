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
