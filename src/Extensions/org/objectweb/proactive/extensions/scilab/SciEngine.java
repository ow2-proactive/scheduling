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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.scilab;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProActiveObject;
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
public class SciEngine implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -5906306770453347764L;
    private SciEngineWorker sciEngineWorker;
    private String idEngine;
    private JVMProcessImpl process;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_WORKER);

    /**
     * default constructor
     */
    public SciEngine() {
    }

    /**
     * @param idEngine
     */
    public SciEngine(String idEngine) {
        this.idEngine = idEngine;
    }

    /**
     * Set the immediate services for this active object
     */
    public int setImmediateServices() {
        ProActiveObject.setImmediateService("killWorker");
        ProActiveObject.setImmediateService("exit");
        return 0; // synchronous call
    }

    public void exit() {
        //logger.debug("->SciEngineWorker In:exit");
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
            logger.debug("->SciEngine In:execute:" + genTask.getId());
        }

        GeneralResult genResult = this.sciEngineWorker.execute(genTask);
        return genResult;
    }

    /**
     * Activate a new JVM to wrap a worker
     * @return a future representing the state of the activation
     */
    public BooleanWrapper activate() {
        if (logger.isDebugEnabled()) {
            logger.debug("->SciEngine In:activate");
        }

        String uri = URIBuilder.buildURI("localhost",
                "" + idEngine + (new Date()).getTime()).toString();
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
                    sciEngineWorker = (SciEngineWorker) ProActiveObject.newActive(SciEngineWorker.class.getName(),
                            null, uri);
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
