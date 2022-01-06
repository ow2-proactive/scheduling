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
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobVariable;


/**
 * SignalApi uses the synchronization API store to provide a persistent <i>key/value channel</i> for job signals.
 *
 * SignalApi uses a single system channel to store all signals needed by jobs.
 * The signal channel contains an entry per job.
 * The key of each entry is the job id, whereas the value of the entry is the set of signals used by the considered job.
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
     * Add a new signal to the set of job signals
     *
     * @param signalName name of the signal to be added
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when adding the ready signal to job signals
     */
    void readyForSignal(String signalName) throws SignalApiException;

    /**
     * Add a new signal to the set of job signals
     *
     * @param signalName name of the signal to be added
     * @param variables the input variables of the signal to be added
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when adding the ready signal to job signals
     */
    void readyForSignal(String signalName, List<JobVariable> variables) throws SignalApiException;

    /**
     * Check if the given signal was received
     *
     * @param signalName name of the signal to be checked
     * @return true if the signal was received
     * @throws SignalApiException if an error occurred while reading in the signals channel
     */
    boolean isReceived(String signalName) throws SignalApiException;

    /**
     * Check if all of the signals given as input exist among the set of job signals
     *
     * @param signalsSubSet set of the signals to be checked
     * @return the the map of signals names and updatedVariables found in the set of job signals, empty map if no signals are found
     * @throws SignalApiException if an error occurred while reading in the signals channel
     */
    Map<String, Map<String, String>> checkForSignals(Set<String> signalsSubSet) throws SignalApiException;

    /**
     * Wait until the given signal is added to the set of job signals
     *
     * @param signalName name of the signal to wait for
     * @return a map containing the signal name and the updated values of the signal
     * @throws SignalApiException if an error occurred while reading in the signals channel
     */
    Map<String, String> waitFor(String signalName) throws SignalApiException;

    /**
     * Wait until one signal (among those of the given {@code signalsSubSet}) is added to the set of job signals
     *
     * @param signalsSubSet set of the signals to wait for any of them
     * @return the first signal received among those given as input
     * @throws SignalApiException if an error occurred while reading in the signals channel
     */
    Signal waitForAny(Set<String> signalsSubSet) throws SignalApiException;

    /**
     * Wait until all the input signals are added to the set of job signals
     *
     * @param signalsSubSet set of the signals to wait for
     * @return a map containing the signal name and the updated values of the signal
     * @throws SignalApiException if an error occurred while reading in the signals channel
     */
    Map<String, Map<String, String>> waitForAll(Set<String> signalsSubSet) throws SignalApiException;

    /**
     * Add the given {@code signal} to the set of job signals
     *
     * @param signalName name of the signal to be added to job signals
     * @throws SignalApiException if the corresponding ready signal does not exist, or when an error occurs while sending the signal
     */
    void sendSignal(String signalName) throws SignalApiException;

    /**
     * Add the given {@code signal} to the set of job signals
     *
     * @param signalName name of the signal to be added to job signals
     * @param parameters the updated values of the signal to be added
     * @throws SignalApiException if the corresponding ready signal does not exist, or when an error occurs while sending the signal
     */
    void sendSignal(String signalName, Map<String, String> parameters) throws SignalApiException;

    /**
     * Add the given set of signals to job signals
     *
     * @param signalsSubSet set of the signals to be added to job signals
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when adding the signals to job signals
     */
    void sendManySignals(Set<String> signalsSubSet) throws SignalApiException;

    /**
     * Add the given set of signals to job signals
     *
     * @param signalParameters  a map containing the signal name and the updated values of the signal
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when adding the signals to job signals
     */
    void sendManySignals(Map<String, Map<String, String>> signalParameters) throws SignalApiException;

    /**
     * Remove the given {@code signal} from the set of job signals
     * In order to remove a signal that was not yet received, the signalsSubSet parameter should contain "_ready" prefix and the signal name
     *
     * @param signal name of the signal to be removed from job signals
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when removing the signal from job signals
     */
    void removeSignal(String signal) throws SignalApiException;

    /**
     * Remove the given set of signals from job signals
     * In order to remove signals that were not yet received, the signalsSubSet parameter should contain "_ready" prefix and the signal names
     *
     * @param signalsSubSet set of the signals to be removed from job signals
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when removing the signals from job signals
     */
    void removeManySignals(Set<String> signalsSubSet) throws SignalApiException;

    /**
     * Return the set of job signals
     *
     * @return a map containing the signal name and the Signal object
     * @throws SignalApiException if an error occurred while reading in the signals channel
     */
    Map<String, Signal> getJobSignals() throws SignalApiException;

    /**
     * Remove the entry corresponding to job signals from the signals channel
     * @throws SignalApiException if an error occurred while reading in the signals channel, or when removing an entry from this channel
     */
    void clearJobSignals() throws SignalApiException;
}
