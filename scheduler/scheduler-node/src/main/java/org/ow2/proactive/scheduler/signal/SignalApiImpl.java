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
import java.util.HashSet;
import java.util.Set;

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

    protected static final String READY_PREFIX = "ready_";

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
                // Initialize synchronization signals channel
                synchronization.createChannelIfAbsent(SIGNALS_CHANNEL, true);
                synchronization.putIfAbsent(SIGNALS_CHANNEL, jobId, new HashSet<String>());
            } catch (IOException | InvalidChannelException e) {
                throw new SignalApiException("Could not instantiate Signal API for the job " + jobId, e);
            }
            isInitialized = true;
        }
    }

    @Override
    public boolean readyForSignal(String signalName) throws SignalApiException {
        try {
            init();
            // Remove the signal if it already exists, then add the ready signal if it does not exist
            synchronization.compute(SIGNALS_CHANNEL,
                                    jobId,
                                    "{k, x -> x.remove('" + signalName + "'); x.add('" + READY_PREFIX + signalName +
                                           "');x}");
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (CompilationException | IOException e) {
            throw new SignalApiException("Could not add ready signal for the job " + jobId, e);
        }
        return true;
    }

    @Override
    public boolean isReceived(String signalName) throws SignalApiException {
        init();
        HashSet<String> signals = getJobSignals();
        if (!signals.isEmpty()) {
            return signals.contains(signalName);
        } else {
            return false;
        }
    }

    @Override
    public String checkForSignals(Set<String> signalsSubSet) throws SignalApiException {
        init();
        HashSet<String> signals = getJobSignals();

        if (!signals.isEmpty()) {
            return signalsSubSet.stream().filter(signals::contains).findFirst().orElse(null);
        } else {
            return null;
        }
    }

    @Override
    public Set<String> sendSignal(String signalName) throws SignalApiException {
        try {
            init();
            if (!isReceived(READY_PREFIX + signalName)) {
                throw new SignalApiException("Job " + jobId + " is not ready to receive the signal " + signalName);
            }
            // Remove the ready signal if it already exists, then add the signal if it does not exist
            synchronization.compute(SIGNALS_CHANNEL,
                                    jobId,
                                    "{k, x -> x.remove('" + READY_PREFIX + signalName + "'); x.add('" + signalName +
                                           "'); x}");
            return getJobSignals();
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (CompilationException | IOException e) {
            throw new SignalApiException("Could not add signal for the job " + jobId, e);
        }
    }

    @Override
    public boolean sendManySignals(Set<String> signalsSubSet) throws SignalApiException {
        try {
            init();
            StringBuilder actions = new StringBuilder();
            for (String signal : signalsSubSet) {
                actions.append(" x.remove('" + READY_PREFIX + signal + "'); x.add('" + signal + "'); ");
            }
            synchronization.compute(SIGNALS_CHANNEL, jobId, "{k, x -> " + actions.toString() + "x}");
            return true;
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (CompilationException | IOException e) {
            throw new SignalApiException("Could not add signals for the job " + jobId, e);
        }
    }

    @Override
    public boolean removeSignal(String signalName) throws SignalApiException {
        try {
            init();
            synchronization.compute(SIGNALS_CHANNEL, jobId, "{k, x -> x.remove('" + signalName + "');x}");
            return true;
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (CompilationException | IOException e) {
            throw new SignalApiException("Could not remove signal for the job " + jobId, e);
        }
    }

    @Override
    public boolean removeManySignals(Set<String> signalsSubSet) throws SignalApiException {
        try {
            init();
            StringBuilder actions = new StringBuilder();
            for (String signal : signalsSubSet) {
                actions.append(" x.remove('" + signal + "'); ");
            }
            synchronization.compute(SIGNALS_CHANNEL, jobId, "{k, x -> " + actions.toString() + "x}");
            return true;
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (CompilationException | IOException e) {
            throw new SignalApiException("Could not add ready signals for the job " + jobId, e);
        }
    }

    @Override
    public HashSet<String> getJobSignals() throws SignalApiException {
        try {
            init();
            //noinspection unchecked
            return (HashSet<String>) synchronization.get(SIGNALS_CHANNEL, jobId);
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        }
    }

    @Override
    public void clearJobSignals() throws SignalApiException {
        try {
            init();
            synchronization.remove(SIGNALS_CHANNEL, jobId);
            isInitialized = false;
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (IOException e) {
            throw new SignalApiException("Could not clear the job entry " + jobId + " from the signals channel", e);
        }
    }

    @Override
    public void waitFor(String signalName) throws SignalApiException {
        init();
        try {
            synchronization.waitUntil(SIGNALS_CHANNEL, jobId, "{k, x -> x.contains('" + signalName + "')}");
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (CompilationException e) {
            throw new SignalApiException("Could not check signals of the job " + jobId, e);
        }
    }

    @Override
    public String waitForAny(Set<String> signalsSubSet) throws SignalApiException {
        init();
        StringBuilder conditions = new StringBuilder();
        for (String signal : signalsSubSet) {
            conditions.append("x.contains('" + signal + "') || ");
        }
        String allConditions = conditions.substring(0, conditions.toString().lastIndexOf("||"));

        try {
            synchronization.waitUntil(SIGNALS_CHANNEL, jobId, "{k, x -> " + allConditions + " }");
            return checkForSignals(signalsSubSet);
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (CompilationException e) {
            throw new SignalApiException("Could not check signals of the job " + jobId, e);
        }
    }
}
