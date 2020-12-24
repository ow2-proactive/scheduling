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
package org.ow2.proactive.scheduler.signal;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author ActiveEon Team
 * @since 24/12/2020
 */
public interface SignalInternal extends Serializable {

    // Signal Operations
    boolean ready(String signalName);

    boolean isReceived(String signalName);

    void waitFor(String signalName);

    void waitForAny(List<String> signalsList);

    // signals' list operations
    int size(List<String> signalsList);

    boolean isEmpty(List<String> signalsList);

    boolean containsSignal(List<String> signalsList, String signalName);

    boolean addSignal(List<String> signalsList, String signalName);

    boolean addAllSignals(List<String> signalsList, List<String> signalsSubList);

    boolean removeSignal(List<String> signalsList, String signalName);

    boolean removeAllSignals(List<String> signalsList, List<String> signalsSubList);

    // signal channel operations
    int size(String channel);

    boolean containsJob(String channel, String jobId);

    Set<String> jobSet(String channel);

    List<String> getJobSignals(String channel, String jobId);

    boolean clearJobSignals(String channel, String jobId);

}
