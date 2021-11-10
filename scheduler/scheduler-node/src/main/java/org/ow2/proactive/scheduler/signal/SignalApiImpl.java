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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.synchronization.CompilationException;
import org.ow2.proactive.scheduler.synchronization.InvalidChannelException;
import org.ow2.proactive.scheduler.synchronization.SynchronizationInternal;
import org.ow2.proactive.scheduler.synchronization.SynchronizationWrapper;


/**
 * SignalApiImpl acts as a high level signal service that uses the synchronization API.
 * It enables workflow tasks handle signals in an easy manner.
 *
 * It implements the methods defined in the interface @see org.ow2.proactive.scheduler.signal.SignalApi.
 *
 * @author ActiveEon Team
 * @since 24/11/2020
 */
public class SignalApiImpl implements SignalApi {

    private static final String SIGNALS_CHANNEL = PASchedulerProperties.SCHEDULER_SIGNALS_CHANNEL.getValueAsString();

    public static final String READY_PREFIX = "ready_";

    private SynchronizationWrapper synchronization;

    private String jobId;

    private boolean isInitialized = false;

    public SignalApiImpl(String originator, TaskId taskId, SynchronizationInternal synchronizationInternal) {
        jobId = taskId.getJobId().value();
        synchronization = new SynchronizationWrapper(originator, taskId, synchronizationInternal);
    }

    private void init() throws SignalApiException {
        if (!isInitialized) {
            try {
                synchronization.createChannelIfAbsent(SIGNALS_CHANNEL + jobId, true);
            } catch (IOException e) {
                throw new SignalApiException("Could not instantiate Signal API for the job " + jobId, e);
            }
            isInitialized = true;
        }
    }

    @Override
    public void readyForSignal(String signalName, List<JobVariable> variables) throws SignalApiException {
        if (StringUtils.isBlank(signalName.trim())) {
            throw new SignalApiException("Empty signals are not allowed");
        }
        try {
            init();
            synchronization.remove(SIGNALS_CHANNEL + jobId, signalName);
            synchronization.put(SIGNALS_CHANNEL + jobId, READY_PREFIX + signalName, new Signal(signalName, variables));
            Set<String> signals = synchronization.keySet(SIGNALS_CHANNEL + jobId);

        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (IOException e) {
            throw new SignalApiException("Could not add ready signal for the job " + jobId, e);
        }
    }

    @Override
    public void readyForSignal(String signalName) throws SignalApiException {
        readyForSignal(signalName, null);
    }

    @Override
    public boolean isReceived(String signalName) throws SignalApiException {
        Set<String> signals = getJobSignals().keySet();
        if (!signals.isEmpty()) {
            return signals.contains(signalName);
        } else {
            return false;
        }
    }

    @Override
    public Signal checkForSignals(Set<String> signalsSubSet) throws SignalApiException {
        Map<String, Signal> signals = getJobSignals();
        if (!signals.isEmpty()) {
            Map.Entry<String, Signal> signalEntry = signals.entrySet()
                                                           .stream()
                                                           .filter(entry -> signalsSubSet.contains(entry.getKey()))
                                                           .findFirst()
                                                           .orElse(null);
            if (signalEntry != null) {
                return signalEntry.getValue();
            }
        }
        return null;
    }

    public void sendSignal(String signalName, Map<String, String> parameters) throws SignalApiException {
        if (StringUtils.isBlank(signalName.trim())) {
            throw new SignalApiException("Empty signals are not allowed");
        }
        try {
            init();
            Signal signal = new Signal(signalName, null);
            signal.setOutputValues(parameters);

            synchronization.remove(SIGNALS_CHANNEL + jobId, READY_PREFIX + signalName);
            synchronization.put(SIGNALS_CHANNEL + jobId, signalName, signal);
        } catch (InvalidChannelException | IOException e) {
            throw new SignalApiException("Could not read signals channel", e);
        }
    }

    @Override
    public void sendSignal(String signalName) throws SignalApiException {
        sendSignal(signalName, null);
    }

    @Override
    public void sendManySignals(Map<String, Map<String, String>> signalParameters) throws SignalApiException {
        if (signalParameters.keySet().stream().anyMatch(signal -> StringUtils.isBlank(signal.trim()))) {
            throw new SignalApiException("Empty signals are not allowed");
        }
        try {
            init();
            for (String signalName : signalParameters.keySet()) {
                Signal existingSignal = (Signal) synchronization.get(SIGNALS_CHANNEL + jobId,
                                                                     READY_PREFIX + signalName);
                Signal signal = existingSignal == null ? new Signal(signalName, null) : existingSignal;
                signal.setOutputValues(signalParameters.get(signalName));
                synchronization.remove(SIGNALS_CHANNEL + jobId, READY_PREFIX + signalName);
                synchronization.put(SIGNALS_CHANNEL + jobId, signalName, signal);
            }
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (IOException e) {
            throw new SignalApiException("Could not add signals for the job " + jobId, e);
        }
    }

    @Override
    public void sendManySignals(Set<String> signalsSubSet) throws SignalApiException {
        Map<String, Map<String, String>> signalParameters = new LinkedHashMap<>();
        signalsSubSet.forEach(signalName -> signalParameters.put(signalName, new HashMap<>()));
        sendManySignals(signalParameters);
    }

    @Override
    public void removeSignal(String signalName) throws SignalApiException {
        try {
            init();
            synchronization.remove(SIGNALS_CHANNEL + jobId, signalName);
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (IOException e) {
            throw new SignalApiException("Could not remove signal for the job " + jobId, e);
        }
    }

    @Override
    public void removeManySignals(Set<String> signalsSubSet) throws SignalApiException {
        try {
            init();
            for (String signal : signalsSubSet) {
                synchronization.remove(SIGNALS_CHANNEL + jobId, signal);
            }
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (IOException e) {
            throw new SignalApiException("Could not add ready signals for the job " + jobId, e);
        }
    }

    @Override
    public Map<String, Signal> getJobSignals() throws SignalApiException {
        Map<String, Signal> signalsMap = new LinkedHashMap<>();
        try {
            for (String signalName : synchronization.keySet(SIGNALS_CHANNEL + jobId)) {
                signalsMap.put(signalName, (Signal) synchronization.get(SIGNALS_CHANNEL + jobId, signalName));
            }
            if (!signalsMap.isEmpty()) {
                return signalsMap;
            }
            init();
            return Collections.emptyMap();
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        }
    }

    @Override
    public void clearJobSignals() throws SignalApiException {
        try {
            init();
            synchronization.clear(SIGNALS_CHANNEL + jobId);
            isInitialized = false;
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (IOException e) {
            throw new SignalApiException("Could not clear the job entry " + jobId + " from the signals channel", e);
        }
    }

    @Override
    public Map<String, String> waitFor(String signalName) throws SignalApiException {
        init();
        try {
            synchronization.waitUntil(SIGNALS_CHANNEL + jobId, signalName, "{k, x -> x != null}");
            Signal signal = (Signal) synchronization.get(SIGNALS_CHANNEL + jobId, signalName);
            return signal.getOutputValues();
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (CompilationException e) {
            throw new SignalApiException("Cogiuld not check signals of the job " + jobId, e);
        }
    }

    @Override
    public Signal waitForAny(Set<String> signalsSubSet) throws SignalApiException {
        init();
        try {
            synchronization.waitUntilAny(SIGNALS_CHANNEL + jobId, signalsSubSet, "{k, x -> x != null }");
            return checkForSignals(signalsSubSet);
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (CompilationException e) {
            throw new SignalApiException("Could not check signals of the job " + jobId, e);
        }
    }

    @Override
    public Map<String, Map<String, String>> waitForAll(Set<String> signalsSubSet) throws SignalApiException {
        init();
        Map<String, Map<String, String>> signalMap = new LinkedHashMap<>();
        try {
            for (String signalName : signalsSubSet) {
                synchronization.waitUntil(SIGNALS_CHANNEL + jobId, signalName, "{k, x -> x != null");
                Signal signal = (Signal) synchronization.get(SIGNALS_CHANNEL + jobId, signalName);
                signalMap.put(signalName, signal.getOutputValues());
            }
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (CompilationException e) {
            throw new SignalApiException("Could not check signals of the job " + jobId, e);
        }
        return signalMap;
    }

}
