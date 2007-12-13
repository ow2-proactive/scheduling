/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.scilab;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.process.AbstractExternalProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 *
 * This class activates a new JVM which wraps the Scilab Engine and forwards the Scilab tasks
 *
 */
public class MSEngine implements Serializable {

    /**
     *
     */
    private MSEngineWorker mSEngineWorker;
    private String idEngine;
    private JVMProcessImpl process;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_WORKER);

    /**
     * default constructor
     */
    public MSEngine() {
    }

    /**
     * @param idEngine
     */
    public MSEngine(String idEngine) {
        this.idEngine = idEngine;
    }

    /**
     * Set the immediate services for this active object
     */
    public int setImmediateServices() {
        PAActiveObject.setImmediateService("killWorker");
        PAActiveObject.setImmediateService("exit");
        return 0; // synchronous call
    }

    public void exit() {
        //logger.debug("->MSEngineWorker In:exit");
        mSEngineWorker.exit();
        this.killWorker();
        System.exit(0);
    }

    /**
     * execute a task
     * @param sciTask Scilab task
     * @return result of the computation
     */
    public GeneralResult execute(GeneralTask genTask) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSEngine In:execute:" + genTask.getId());
        }

        GeneralResult genResult = this.mSEngineWorker.execute(genTask);
        return genResult;
    }

    /**
     * Activate a new JVM to wrap a worker
     * @return a future representing the state of the activation
     */
    public BooleanWrapper activate() {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSEngine In:activate");
        }

        String uri = URIBuilder.buildURI("localhost", "" + idEngine + (new Date()).getTime()).toString();
        try {
            process = new JVMProcessImpl();
            process.setInputMessageLogger(new AbstractExternalProcess.StandardOutputMessageLogger());
            process.setParameters(uri);
            process.startProcess();
        } catch (IOException e) {
            return new BooleanWrapper(false);
        }

        for (int i = 0; i < 30; i++) {
            try {
                try {
                    mSEngineWorker = (MSEngineWorker) PAActiveObject.newActive(
                            MSEngineWorker.class.getName(), null, uri);
                    return new BooleanWrapper(true);
                } catch (ProActiveException e) {
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return new BooleanWrapper(false);
    }

    /**
     * Kill the worker related the task
     * It is an immediat services
     */
    public synchronized void killWorker() {
        process.stopProcess();
    }
}
