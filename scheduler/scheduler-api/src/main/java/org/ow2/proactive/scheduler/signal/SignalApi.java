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

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * SignalApi uses the synchronization API store to provide a persistent <i>key/value channel</i> for job signals.
 *
 * SignalApi uses a single system channel to store all signals needed by jobs.
 * The signal channel contains an entry per job.
 * The key of each entry is the job id, whereas the value of the entry is the list of signals used by the considered job.
 *
 * The SignalApi service is automatically started with the ProActive Server.
 *
 * @author ActiveEon Team
 * @since 24/11/2020
 * @see org.ow2.proactive.scheduler.synchronization
 */
@PublicAPI
public interface SignalApi extends Serializable {

    /**
     * Add a new signal to the list of job signals
     * The new signal is composed of a prefix (i.e., "ready_") and the given signal
     *
     * @param signal name of the signal to be added
     * @return true if the signal is successfully added to the list of job signals
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when adding the ready signal to job signals
     */
    boolean readyForSignal(String signal) throws SignalApiException;

    /**
     * Check if the given signal exists among the list of job signals
     *
     * @param signal name of the signal to be checked
     * @return true if the signal exists among the list of job signals
     * @throws SignalApiException if an error occurred while reading in the signals channel
     */
    boolean isReceived(String signal) throws SignalApiException;

    /**
     * Wait until the given signal is added to the list of job signals
     *
     * @param signal name of the signal to wait for
     * @throws SignalApiException if an error occurred while reading in the signals channel
     */
    void waitFor(String signal) throws SignalApiException;

    /**
     * Wait until one signal (among those of the given {@code signalsList}) is added to the list of job signals
     *
     * @param signalsList list of the signals to wait for any of them
     * @throws SignalApiException if an error occurred while reading in the signals channel
     */
    void waitForAny(List<String> signalsList) throws SignalApiException;

    /**
     * Add the given {@code signal} to the list of job signals
     *
     * @param signal name of the signal to be added to job signals
     * @return true if the signal is successfully added to job signals
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when adding the signal to job signals
     */
    boolean sendSignal(String signal) throws SignalApiException;

    /**
     * Add the given list of signals to job signals
     *
     * @param signalsList list of the signals to be added to job signals
     * @return true if all the given signals are successfully added to job signal
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when adding the signals to job signals
     */
    boolean sendManySignals(List<String> signalsList) throws SignalApiException;

    /**
     * Remove the given {@code signal} from the list of job signals
     *
     * @param signal name of the signal to be removed from job signals
     * @return true if the signal is successfully removed from the list of job signals
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when removing the signal from job signals
     */
    boolean removeSignal(String signal) throws SignalApiException;

    /**
     * Remove the given list of signals from job signals
     *
     * @param signalsList list of the signals to be removed from job signals
     * @return true if all the given signals are successfully removed from job signals
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when removing the signals from job signals
     */
    boolean removeManySignals(List<String> signalsList) throws SignalApiException;

    /**
     * Return the list of job signals
     *
     * @return the list of job signals
     * @throws SignalApiException if an error occurred while reading in the signals channel
     */
    List<String> getJobSignals() throws SignalApiException;

    /**
     * Remove the entry corresponding to job signals from the signals channel
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when removing an entry from this channel
     */
    void clearJobSignals() throws SignalApiException;
}
