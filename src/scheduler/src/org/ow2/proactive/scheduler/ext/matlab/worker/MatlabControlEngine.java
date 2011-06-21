package org.ow2.proactive.scheduler.ext.matlab.worker;

import matlabcontrol.*;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabEngineConfig;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


/**
 * MatlabControlEngine
 *
 * @author The ProActive Team
 */
public class MatlabControlEngine {
    /**
     * The ptolemy Matlab engine
     */
    private static RemoteMatlabProxyFactory factory = null;

    private static RemoteMatlabProxy proxy = null;

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

    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    private static void init() {
        if (proxy == null) {

            try {

                if (debug) {
                    System.out.println("Starting a new Matlab engine:");
                    System.out.println(configuration);
                }
                String loc = configuration.getMatlabHome() + os.fileSeparator() +
                    configuration.getMatlabBinDir() + os.fileSeparator() +
                    configuration.getMatlabCommandName();
                //Configuration.setStartupOptions(new String[] { "-nosplash", "-nodesktop", "-wait" });

                //Create a factory
                factory = new RemoteMatlabProxyFactory(loc);

                proxy = factory.getProxy();

                //                factory.addConnectionListener(new MatlabConnectionListener() {
                //
                //                    @Override
                //                    public void connectionEstablished(RemoteMatlabProxy remoteMatlabProxy) {
                //                         proxy = remoteMatlabProxy;
                //                    }
                //
                //                    @Override
                //                    public void connectionLost(RemoteMatlabProxy remoteMatlabProxy) {
                //                        proxy = null;
                //                    }
                //                });
                //
                //                factory.requestProxy();

                hook = new Thread(new Runnable() {
                    public void run() {
                        if (proxy != null) {
                            try {
                                proxy.exit();
                            } catch (MatlabInvocationException e) {
                                e.printStackTrace();
                            }
                            proxy = null;
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
                proxy = null;
                throw ne;
            } catch (NoClassDefFoundError e) {
                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Can't find the ptolemy classes");
                pw.println("java.class.path=" + System.getProperty("java.class.path"));

                NoClassDefFoundError ne = new NoClassDefFoundError(error_message.toString());
                ne.initCause(e);
                proxy = null;
                throw ne;
            } catch (MatlabConnectionException e) {
                System.out.println("Can't open matlab engine");
                if (os.equals(OperatingSystem.unix)) {
                    System.out.println("LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH"));
                } else {
                    System.out.println("Path=" + System.getenv("Path"));
                }
                System.out.println("java.library.path=" + System.getProperty("java.library.path"));
                RuntimeException ne = new RuntimeException(e.getMessage());
                ne.initCause(e);
                proxy = null;

                throw ne;
            }
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

    private static void testEngineInitOrRestart() {
        init();
        try {

            proxy.waitReady();

            Double test = new Double(1);
            put("test", test);

            evalString("testok=exist('test','var');");
            Object ok = get("testok");
            boolean okj = false;
            if (ok != null) {
                if (ok instanceof double[]) {
                    okj = (((double[]) ok)[0] == 1.0);
                }
            }

            if (!okj) {
                restart();
            } else {
                evalString("clear test testok");
            }

        } catch (Exception e) {
            e.printStackTrace();
            restart();
        }
        // ok
    }

    private static void restart() {
        try {
            close();
        } catch (Exception e) {

        }
        init();
    }

    /**
     * Clears the engine's workspace
     *
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    private static void clear() throws MatlabInvocationException {
        init();
        proxy.eval("clear all;");
    }

    /**
     * Evaluate the given string in the workspace
     *
     * @param command
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    private static void evalString(String command) throws MatlabInvocationException {

        init();
        //System.out.println(command);
        String out = proxy.eval2(command);
        System.out.println(out);
    }

    /**
     * Extract a variable from the workspace
     *
     * @param variableName name of the variable
     * @return value of the variable
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    private static Object get(String variableName) throws MatlabInvocationException {
        init();
        return proxy.getVariable(variableName);
    }

    /**
     * Push a variable in to the workspace
     *
     * @param variableName name of the variable
     * @param token        value
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    private static void put(String variableName, Object token) throws MatlabInvocationException {
        init();
        proxy.setVariable(variableName, token);
    }

    /**
     * Close the engine
     */
    public synchronized static void close() {
        lock.lock();
        if (proxy != null) {
            try {
                proxy.exit();
            } catch (Exception e) {

            }
            proxy = null;
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

        public void testEngineInitOrRestart() throws MatlabInvocationException {
            MatlabControlEngine.testEngineInitOrRestart();
        }

        /**
         * Evaluate the given string in the workspace
         *
         * @param command
         * @throws ptolemy.kernel.util.IllegalActionException
         */
        public void evalString(String command) throws MatlabInvocationException {
            MatlabControlEngine.evalString(command);
        }

        /**
         * Extract a variable from the workspace
         *
         * @param variableName name of the variable
         * @return value of the variable
         * @throws ptolemy.kernel.util.IllegalActionException
         */
        public Object get(String variableName) throws MatlabInvocationException {
            return MatlabControlEngine.get(variableName);
        }

        /**
         * Push a variable in to the workspace
         *
         * @param variableName name of the variable
         * @param token        value
         * @throws ptolemy.kernel.util.IllegalActionException
         */
        public void put(String variableName, Object token) throws MatlabInvocationException {
            MatlabControlEngine.put(variableName, token);
        }

        /**
         * Clears the engine's workspace
         *
         * @throws ptolemy.kernel.util.IllegalActionException
         */
        public void clear() throws MatlabInvocationException {
            MatlabControlEngine.clear();
        }

        /**
         * Release the engine connection
         */
        public void release() throws MatlabInvocationException {
            MatlabControlEngine.release();
            released = true;
        }

        @Override
        protected void finalize() throws Throwable {
            release();
        }
    }
}
