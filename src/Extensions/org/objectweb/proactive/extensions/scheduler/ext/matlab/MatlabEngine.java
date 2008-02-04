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
package org.objectweb.proactive.extensions.scheduler.ext.matlab;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.objectweb.proactive.core.util.OperatingSystem;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.Engine;
import ptolemy.matlab.Engine.ConversionParameters;


/**
 * This class is an interface to the Matlab Engine
 * @author fviale
 *
 */
public class MatlabEngine {
    /**
     * The ptolemy Matlab engine
     */
    private static Engine eng = null;
    /**
     * The engine handle
     */
    private static long[] engineHandle;
    /**
     * Name of the matlab command
     */
    private static String commandName;
    /**
     * Is the engine currently used by a thread ?
     */
    private static boolean locked = false;

    /**
     * Ptolemy engine customization
     */
    private static ConversionParameters convP = null;

    private static OperatingSystem os = OperatingSystem.getOperatingSystem();;

    static {
        convP = new ConversionParameters();
        convP.getIntMatrices = true;
    }

    // In order to prevent that different threads access the matlab engine at the same time,
    // we use a singleton pattern
    private static Connection instance = new MatlabEngine.Connection();

    static synchronized void init() throws IllegalActionException {
        if (eng == null) {
            try {
                eng = new Engine();

                eng.setDebugging((byte) 0);

                // we build the matlab command, depending on the os
                if (os.equals(OperatingSystem.unix)) {
                    engineHandle = eng.open(commandName + " -nodisplay -nosplash -nodesktop", true);
                } else {
                    engineHandle = eng.open(commandName + " -nosplash -minimize", true);
                }
                // highly verbose exceptions
            } catch (UnsatisfiedLinkError e) {
                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Can't find the Matlab libraries");
                pw.println("PATH=" + System.getenv("PATH"));
                pw.println("LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH"));
                pw.println("java.library.path=" + System.getProperty("java.library.path"));

                UnsatisfiedLinkError ne = new UnsatisfiedLinkError(error_message.toString());
                ne.initCause(e);
                throw ne;
            } catch (NoClassDefFoundError e) {
                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Can't find the ptolemy classes");
                pw.println("java.class.path=" + System.getProperty("java.class.path"));

                NoClassDefFoundError ne = new NoClassDefFoundError(error_message.toString());
                ne.initCause(e);
                throw ne;
            }
        }
    }

    public static void setCommandName(String name) {
        commandName = name;
    }

    public static String getCommandName() {
        return commandName;
    }

    private static void waitLock() {
        while (locked == true) {
            Thread.yield();
        }
    }

    /**
     * Acquire a connection to the matlab engine
     * @return an engine connection
     */
    public synchronized static Connection acquire() {
        waitLock();
        locked = true;
        return instance;
    }

    /**
     * Release the connection to the matlab engine
     */
    private synchronized static void release() {
        locked = false;
    }

    /**
     * Clears the engine's workspace
     * @throws IllegalActionException
     */
    private synchronized static void clear() throws IllegalActionException {
        init();
        eng.evalString(engineHandle, "clear");
    }

    /**
     * Evaluate the given string in the workspace
     * @param command
     * @throws IllegalActionException
     */
    private synchronized static void evalString(String command) throws IllegalActionException {
        init();
        eng.evalString(engineHandle, command);
    }

    /**
     * Extract a variable from the workspace
     * @param variableName name of the variable
     * @return value of the variable
     * @throws IllegalActionException
     */
    private synchronized static Token get(String variableName) throws IllegalActionException {
        init();
        return eng.get(engineHandle, variableName, convP);
    }

    /**
     * Push a variable in to the workspace
     * @param variableName name of the variable
     * @param token value
     * @throws IllegalActionException
     */
    private synchronized static void put(String variableName, Token token) throws IllegalActionException {
        init();
        eng.put(engineHandle, variableName, token);
    }

    /**
     * Close the engine
     */
    public static void close() {
        eng.close(engineHandle);
        eng = null;
    }

    /**
     * Public access to the engine, locking the engine is necessary to have a public access
     * @author fviale
     *
     */
    public static class Connection {
        public Connection() {
        }

        /**
         * Evaluate the given string in the workspace
         * @param command
         * @throws IllegalActionException
         */
        public void evalString(String command) throws IllegalActionException {
            MatlabEngine.evalString(command);
        }

        /**
         * Extract a variable from the workspace
         * @param variableName name of the variable
         * @return value of the variable
         * @throws IllegalActionException
         */
        public Token get(String variableName) throws IllegalActionException {
            return MatlabEngine.get(variableName);
        }

        /**
         * Push a variable in to the workspace
         * @param variableName name of the variable
         * @param token value
         * @throws IllegalActionException
         */
        public void put(String variableName, Token token) throws IllegalActionException {
            MatlabEngine.put(variableName, token);
        }

        /**
         * Clears the engine's workspace
         * @throws IllegalActionException
         */
        public void clear() throws IllegalActionException {
            MatlabEngine.clear();
        }

        /**
         * Release the engine connection
         */
        public void release() {
            MatlabEngine.release();
        }

        protected void finalize() throws Throwable {
            release();
        }
    }
}
