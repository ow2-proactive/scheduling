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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.synchronization.InvalidChannelException;


/**
 * Signal uses the synchronization API store to provide a persistent <i>key/value channel</i> for job signals.
 *
 * Signal uses a single system channel to store all signals needed by jobs.
 * The signal channel contains an entry per job.
 * The key of each entry is the job id, whereas the value of the entry is the list of signals used by the considered job.
 *
 * The Signal service is automatically started with the ProActive Server.
 *
 * @author ActiveEon Team
 * @since 24/11/2020
 * @see org.ow2.proactive.scheduler.synchronization
 */
@PublicAPI
public interface Signal extends Serializable {

    boolean ready(String signalName) throws IOException, InvalidChannelException;

    boolean isReceived(String signalName) throws InvalidChannelException;

    void waitFor(String signalName) throws InvalidChannelException, InterruptedException;

    void waitForAny(List<String> signalsList) throws InterruptedException, InvalidChannelException;

}
