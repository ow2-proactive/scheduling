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

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.extensions.annotation.ActiveObject;

import groovy.lang.GroovyShell;


/**
 * Implementation of the Signal service
 *
 * This implementation uses an Active Object to allow remote interaction and enforce atomicity
 *
 * All requests are <b>synchronous</b> (this is done by returning primitive objects or throwing checked exception)
 *
 * A custom {@link RunActive#runActivity(Body) runActivity} is implemented to handle wait Methods.
 * @author ActiveEon Team
 * @since 26/03/2018
 */
@ActiveObject
public class AOSignal implements RunActive, InitActive, EndActive, SignalInternal {

    private static final Logger logger = Logger.getLogger(AOSignal.class);

    private boolean isStarted = false;

    /** Groovy related configuration */
    private GroovyShell shell;

    public AOSignal() {
        initializeGroovyCompiler();
    }

    @Override
    public boolean ready(String signalName) {
        return false;
    }

    @Override
    public boolean isReceived(String signalName) {
        return false;
    }

    @Override
    public void waitFor(String signalName) {

    }

    @Override
    public void waitForAny(List<String> signalsList) {

    }

    @Override
    public int size(List<String> signalsList) {
        return 0;
    }

    @Override
    public boolean isEmpty(List<String> signalsList) {
        return false;
    }

    @Override
    public boolean containsSignal(List<String> signalsList, String signalName) {
        return false;
    }

    @Override
    public boolean addSignal(List<String> signalsList, String signalName) {
        return false;
    }

    @Override
    public boolean addAllSignals(List<String> signalsList, List<String> signalsSubList) {
        return false;
    }

    @Override
    public boolean removeSignal(List<String> signalsList, String signalName) {
        return false;
    }

    @Override
    public boolean removeAllSignals(List<String> signalsList, List<String> signalsSubList) {
        return false;
    }

    @Override
    public int size(String channel) {
        return 0;
    }

    @Override
    public boolean containsJob(String channel, String jobId) {
        return false;
    }

    @Override
    public Set<String> jobSet(String channel) {
        return null;
    }

    @Override
    public List<String> getJobSignals(String channel, String jobId) {
        return null;
    }

    @Override
    public boolean clearJobSignals(String channel, String jobId) {
        return false;
    }

    private void initializeGroovyCompiler() {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setTargetBytecode(CompilerConfiguration.JDK8);
        shell = new GroovyShell(this.getClass().getClassLoader(), compilerConfiguration);
    }

    @Override
    public void initActivity(Body body) {
        logger.info("Starting Signal service");

        logger.info("Signal service is started");
    }

    @Override
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (body.isActive()) {
            try {
                Request request = service.blockingRemoveOldest();
                if (request != null) {
                    service.serve(request);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void endActivity(Body body) {
        logger.info("Signal service is closed");
    }

    public boolean isStarted() {
        return isStarted;
    }
}
