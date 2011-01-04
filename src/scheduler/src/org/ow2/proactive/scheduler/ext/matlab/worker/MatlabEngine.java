/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matlab.worker;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabEngineConfig;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.Engine;
import ptolemy.matlab.Engine.ConversionParameters;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


/**
 * This class is an interface to the Matlab Engine
 *
 * @author The ProActive Team
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
    private static MatlabEngineConfig configuration;

    /**
     * Is the engine currently used by a thread ?
     */

    private static boolean debug = false;

    private static byte debugLevel = 0;

    private static Thread hook = null;

    private static Lock lock = new Lock() {
        public void lock() {

        }

        public void lockInterruptibly() throws InterruptedException {

        }

        public boolean tryLock() {
            return false;
        }

        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        public void unlock() {

        }

        public Condition newCondition() {
            return null;
        }
    };

    /**
     * Ptolemy engine customization
     */
    private static ConversionParameters convP = null;

    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    static {
        convP = new ConversionParameters();
        convP.getIntMatrices = true;
    }

    private static void init() throws IllegalActionException {
        if (eng == null) {
            try {

                eng = new Engine();
                eng.setDebugging(debugLevel);

                if (debug) {
                    System.out.println("Starting a new Matlab engine:");
                    System.out.println(configuration);
                }

                // we build the matlab command, depending on the os
                if (os.equals(OperatingSystem.unix)) {
                    engineHandle = eng.open(configuration.getMatlabHome() + os.fileSeparator() +
                        configuration.getMatlabBinDir() + os.fileSeparator() +
                        configuration.getMatlabCommandName() + " -nodisplay -nosplash -nodesktop", true);
                } else {
                    engineHandle = eng.open("\"" + configuration.getMatlabHome() + os.fileSeparator() +
                        configuration.getMatlabBinDir() + os.fileSeparator() +
                        configuration.getMatlabCommandName() + "\"" + " -automation", true);
                }

                hook = new Thread(new Runnable() {
                    public void run() {
                        if (eng != null) {
                            eng.close(engineHandle);
                            eng = null;
                        }
                    }
                });

                Runtime.getRuntime().addShutdownHook(hook);

                // highly verbose exceptions
            } catch (UnsatisfiedLinkError e) {
                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Can't find the Matlab libraries");
                if (os.equals(OperatingSystem.unix)) {
                    pw.println("LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH"));
                } else {
                    pw.println("Path=" + System.getenv("Path"));
                }
                pw.println("java.library.path=" + System.getProperty("java.library.path"));

                UnsatisfiedLinkError ne = new UnsatisfiedLinkError(error_message.toString());
                ne.initCause(e);
                eng = null;
                throw ne;
            } catch (NoClassDefFoundError e) {
                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Can't find the ptolemy classes");
                pw.println("java.class.path=" + System.getProperty("java.class.path"));

                NoClassDefFoundError ne = new NoClassDefFoundError(error_message.toString());
                ne.initCause(e);
                eng = null;
                throw ne;
            } catch (IllegalActionException e) {
                System.out.println("Can't open matlab engine");
                if (os.equals(OperatingSystem.unix)) {
                    System.out.println("LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH"));
                } else {
                    System.out.println("Path=" + System.getenv("Path"));
                }
                System.out.println("java.library.path=" + System.getProperty("java.library.path"));
                eng = null;
                throw e;
            }
        }

    }

    public static void setDebug(byte dL) {
        debugLevel = dL;
        debug = (dL > 0);
        if (eng != null) {
            eng.setDebugging(debugLevel);
        }

    }

    public static void setConfiguration(MatlabEngineConfig config) {
        configuration = config;
    }

    public static MatlabEngineConfig getConfiguration() {
        return configuration;
    }

    public static String getCommandName() {
        return configuration.getMatlabCommandName();
    }

    /**
     * Acquire a connection to the matlab engine
     *
     * @return an engine connection
     */
    public synchronized static Connection acquire() {
        lock.lock();
        return new Connection();
    }

    /**
     * Release the connection to the matlab engine
     */
    private static void release() {
        lock.unlock();
    }

    private static void testEngineInitOrRestart() throws IllegalActionException {
        init();
        try {
            IntToken test = new IntToken(1);
            put("test", test);
            Token answer = get("test");
            if ((answer == null) || !(answer instanceof IntToken)) {
                restart();
            }
            if (((IntToken) answer).intValue() != 1) {
                restart();
            }
            evalString("clear test");
        } catch (IllegalActionException e) {
            restart();
        }
        // ok
    }

    private static void restart() throws IllegalActionException {
        eng = null;
        init();
    }

    /**
     * Clears the engine's workspace
     *
     * @throws IllegalActionException
     */
    private static void clear() throws IllegalActionException {
        init();
        eng.evalString(engineHandle, "clear all;");
    }

    /**
     * Evaluate the given string in the workspace
     *
     * @param command
     * @throws IllegalActionException
     */
    private static void evalString(String command) throws IllegalActionException {

        init();
        eng.evalString(engineHandle, command);
        System.out.println(eng.getOutput(engineHandle).stringValue());
    }

    /**
     * Extract a variable from the workspace
     *
     * @param variableName name of the variable
     * @return value of the variable
     * @throws IllegalActionException
     */
    private static Token get(String variableName) throws IllegalActionException {
        init();
        return eng.get(engineHandle, variableName, convP);
    }

    /**
     * Push a variable in to the workspace
     *
     * @param variableName name of the variable
     * @param token        value
     * @throws IllegalActionException
     */
    private static void put(String variableName, Token token) throws IllegalActionException {
        init();
        eng.put(engineHandle, variableName, token);
    }

    /**
     * Close the engine
     */
    public synchronized static void close() {
        lock.lock();
        if (eng != null) {
            eng.close(engineHandle);
            eng = null;
            try {
                Runtime.getRuntime().removeShutdownHook(hook);
            } catch (Exception e) {

            }
        }
        lock.unlock();
    }

    /**
     * Public access to the engine, locking the engine is necessary to have a public access
     *
     * @author The ProActive Team
     */
    public static class Connection {

        boolean released = false;

        public Connection() {
        }

        public void testEngineInitOrRestart() throws IllegalActionException {
            if (released)
                throw new IllegalActionException("Connection is released");
            MatlabEngine.testEngineInitOrRestart();
        }

        /**
         * Evaluate the given string in the workspace
         *
         * @param command
         * @throws IllegalActionException
         */
        public void evalString(String command) throws IllegalActionException {
            if (released)
                throw new IllegalActionException("Connection is released");
            MatlabEngine.evalString(command);
        }

        /**
         * Extract a variable from the workspace
         *
         * @param variableName name of the variable
         * @return value of the variable
         * @throws IllegalActionException
         */
        public Token get(String variableName) throws IllegalActionException {
            if (released)
                throw new IllegalActionException("Connection is released");
            return MatlabEngine.get(variableName);
        }

        /**
         * Push a variable in to the workspace
         *
         * @param variableName name of the variable
         * @param token        value
         * @throws IllegalActionException
         */
        public void put(String variableName, Token token) throws IllegalActionException {
            if (released)
                throw new IllegalActionException("Connection is released");
            MatlabEngine.put(variableName, token);
        }

        /**
         * Clears the engine's workspace
         *
         * @throws IllegalActionException
         */
        public void clear() throws IllegalActionException {
            if (released)
                throw new IllegalActionException("Connection is released");
            MatlabEngine.clear();
        }

        /**
         * Release the engine connection
         */
        public void release() throws IllegalActionException {
            if (released)
                throw new IllegalActionException("Connection is released");
            MatlabEngine.release();
            released = true;
        }

        @Override
        protected void finalize() throws Throwable {
            release();
        }
    }
}
