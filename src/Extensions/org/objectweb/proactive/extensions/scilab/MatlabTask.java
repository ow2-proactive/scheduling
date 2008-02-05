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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import ptolemy.data.Token;
import ptolemy.matlab.Engine;


/**
 * This class represents a Matlab task
 * @author fviale
 *
 */
@PublicAPI
public class MatlabTask extends AbstractGeneralTask {
    private HashMap<String, Token> listDataIn;
    private static Engine matlabEngine = null;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_TASK);
    private OperatingSystem os = OperatingSystem.getOperatingSystem();;
    private static long[] engineHandle;

    /**
     * Create a MAtlab task with the given id
     * @param id
     */
    public MatlabTask(String id) {
        super(id);
        this.listDataIn = new HashMap<String, Token>();
    }

    /**
     * Retrieve the list of input data
     * @return list of input data
     */
    public HashMap<String, Token> getListDataIn() {
        return listDataIn;
    }

    /**
     * set the list of input data
     * @param listDataIn map of <name,data> pairs
     */
    public void setListDataIn(HashMap<String, Token> listDataIn) {
        this.listDataIn = listDataIn;
    }

    /**
     * add an input data
     * @param name name of the data
     * @param data ptolemy Token data
     */
    public void addDataIn(String name, Token data) {
        this.listDataIn.put(name, data);
    }

    public void setListDataOut(List<String> listDataOut) {
        for (String name : listDataOut) {
            addDataOut(name);
        }
    }

    public void clearWorkspace() {
        try {
            matlabEngine.evalString(engineHandle, "clear");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public List<AbstractData> receiveDataOut() throws MatlabException {
        ArrayList<AbstractData> results = new ArrayList<AbstractData>();
        for (String key : listDataOut) {
            try {
                Token out = matlabEngine.get(engineHandle, key);
                results.add(new AbstractData(key, out));
            } catch (Throwable e) {
                throw new MatlabException(e);
            }
        }

        return results;
    }

    public void sendListDataIn() throws MatlabException {
        for (Map.Entry<String, Token> entry : listDataIn.entrySet()) {
            try {
                matlabEngine.put(engineHandle, entry.getKey(), entry.getValue());
            } catch (Throwable e) {
                throw new MatlabException(e);
            }
        }
    }

    public void init() throws MatlabException {
        if (matlabEngine == null) {
            String matlab_command = System.getenv().get("MATLAB");
            if (matlab_command == null) {
                logger.fatal("MATLAB environment variable must contain the name of the matlab command.");
                throw new MatlabException(
                    "MATLAB environment variable must contain the name of the matlab command");
            }
            try {
                matlabEngine = new Engine();
                if (logger.isDebugEnabled()) {
                    matlabEngine.setDebugging((byte) 2);
                }
                if (os.equals(OperatingSystem.unix)) {
                    engineHandle = matlabEngine.open(matlab_command + " -nodisplay -nosplash -nodesktop",
                            true);
                } else {
                    engineHandle = matlabEngine.open(matlab_command + " -nosplash -minimize", true);
                }
            } catch (Throwable e) {
                throw new MatlabException(e);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    terminateEngine();
                }
            }));
        }
    }

    /**
     * Static method for terminating the Matlab Engine
     */
    public static void terminateEngine() {
        if (matlabEngine != null) {
            matlabEngine.close(engineHandle);
            matlabEngine = null;
            if (logger.isDebugEnabled()) {
                logger.debug("->MatlabTask Matlab Engine Terminated");
            }
        }
    }

    public boolean execute() throws TaskException {
        if (logger.isDebugEnabled()) {
            logger.debug("->MatlabTask In:execute");
        }

        String fulljob = jobInit + "\n" + job;

        try {
            return (matlabEngine.evalString(engineHandle, fulljob) == 0);
        } catch (Throwable e) {
            throw new MatlabException(e);
        }
    }

    public String getLastMessage() {
        return matlabEngine.getOutput(engineHandle).stringValue();
    }
}
