package org.ow2.proactive.scheduler.ext.matlab.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProcessCreator;
import matlabcontrol.RemoteMatlabProxy;
import matlabcontrol.RemoteMatlabProxyFactory;

import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderFactory;
import org.objectweb.proactive.extensions.processbuilder.OSUser;
import org.objectweb.proactive.extensions.processbuilder.PAOSProcessBuilderFactory;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabInitException;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabTaskException;


/**
 * This class uses the Matlabcontrol API to establish a connection with MATLAB for
 * MATLAB tasks executions. There can be only one instance at a time.
 * If the instance is created using an anonymous {@link MatlabExecutable} not under
 * a {@link OSUser} it can be saved for later (if keepEngine=true).
 * If the instance is created with an identity it must be stopped at the end of the task.
 * Be careful this class is not thread safe.
 */
public class MatlabConnection {

    /** The reference on the previous anonymous connection */
    private static MatlabConnection previousAnonymousConnection;

    /** The proxy to the remote MATLAB */
    private RemoteMatlabProxy proxy;

    /** The hash code of configuration related to MATLAB */
    private final int configHashCode;

    /**
     * Depending on the given executable will return an instance of a
     * MatlabConnection.
     *
     * @throws MatlabInitException if MATLAB could not be initialized
     */
    public static MatlabConnection acquire(final MatlabExecutable executable) throws MatlabInitException {

        final OSUser user = executable.getUser();
        MatlabConnection connection;

        final boolean previousConn = MatlabConnection.previousAnonymousConnection != null;
        final boolean noUser = user == null;
        final boolean keep = executable.paconfig.isKeepEngine();
        final boolean sameConfig = previousConn &&
            (MatlabConnection.previousAnonymousConnection.configHashCode == executable.matlabEngineConfig
                    .hashCode());

        if (previousConn && noUser && keep && sameConfig) {
            connection = MatlabConnection.previousAnonymousConnection;
        } else {
            // Cannot reuse the connection then release it
            if (previousConn) {
                MatlabConnection.previousAnonymousConnection.release();
                MatlabConnection.previousAnonymousConnection = null;
            }

            // Create the connection, may throw a MatlabInitException
            connection = createInternal(executable, user);

            // Save the connection as previous only if anonymous context and "keep" asked
            if (noUser && keep) {
                MatlabConnection.previousAnonymousConnection = connection;
            }
        }

        return connection;
    }

    private static MatlabConnection createInternal(final MatlabExecutable executable, final OSUser user)
            throws MatlabInitException {
        // String matlabLocation = executable.matlabEngineConfig.getFullCommand();
        String matlabLocation = "C:\\Program Files\\MATLAB\\R2010b\\bin\\matlab.exe"; // TODO: REMOVE ME

        // If a user is specified create the proxy factory with a specific
        // MATLAB process as user creator
        RemoteMatlabProxyFactory proxyFactory;
        try {
            if (user != null) {
                MatlabProcessCreator prCreator = new MatlabProcessAsUserCreator(user, matlabLocation,
                    executable.localSpace);
                proxyFactory = new RemoteMatlabProxyFactory(prCreator);
            } else {
                proxyFactory = new RemoteMatlabProxyFactory(matlabLocation);
            }
        } catch (MatlabConnectionException e) {
            // Possible cause: registry problem or receiver is not bind
            e.printStackTrace();

            // Nothing can be done maybe a retry ... check this later
            MatlabInitException me = new MatlabInitException(
                "Unable to create the MATLAB proxy factory. Possible causes: registry cannot be created or the receiver cannot be bind");
            me.initCause(e);
            throw me;
        }

        // This will start a MATLAB process, wait until the JVM inside MATLAB
        RemoteMatlabProxy proxy;
        try {
            proxy = proxyFactory.getProxy();
        } catch (MatlabConnectionException e) {
            // Possible cause: timeout
            e.printStackTrace();

            // Nothing can be done maybe a retry ... check this later
            MatlabInitException me = new MatlabInitException(
                "Unable to create the MATLAB proxy factory. Possible causes: registry cannot be created or the receiver cannot be bind");
            me.initCause(e);
            throw me;
        }

        // Return a new MATLAB connection
        return new MatlabConnection(proxy, executable.matlabEngineConfig.hashCode());
    }

    private MatlabConnection(final RemoteMatlabProxy proxy, final int configHashCode) {
        this.proxy = proxy;
        this.configHashCode = configHashCode;
    }

    /*********** PUBLIC METHODS ***********/

    /**
     * Releases the connection, after a call to this method
     * the connection becomes unusable !
     */
    public void release() {
        if (this.proxy == null) {
            return;
        }

        try {
            this.proxy.exit();
        } catch (Exception e) {
        }

        this.proxy = null;
        try {
            // Runtime.getRuntime().removeShutdownHook(hook);
        } catch (Exception e) {
        }
    }

    //    public void testEngineInitOrRestart() {
    //        try {
    //            this.proxy.waitReady();
    //
    //            Double test = new Double(1);
    //            put("test", test);
    //
    //            evalString("testok=exist('test','var');");
    //            Object ok = get("testok");
    //            boolean okj = false;
    //            if (ok != null) {
    //                if (ok instanceof double[]) {
    //                    okj = (((double[]) ok)[0] == 1.0);
    //                }
    //            }
    //
    //            if (!okj) {
    //                restart();
    //            } else {
    //                evalString("clear test testok");
    //            }
    //
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            restart();
    //        }
    //        // ok
    //    }

    /**
     * Clears the engine's workspace.
     *
     * @throws MatlabTaskException If unable to clear the workspace
     */
    public void clear() throws MatlabTaskException {
        try {
            this.proxy.eval("clear all;");
        } catch (MatlabInvocationException e) {
            throw new MatlabTaskException("Unable to clear MATLAB", e);
        }
    }

    /**
     * Evaluate the given string in the workspace.
     *
     * @param command the command to evaluate
     * @throws MatlabTaskException If unable to evaluate the command
     */
    public void evalString(final String command) throws MatlabTaskException {
        try {
            String out = this.proxy.eval2(command);
            System.out.println(out);
        } catch (MatlabInvocationException e) {
            throw new MatlabTaskException("Unable to eval command " + command, e);
        }
    }

    /**
     * Extract a variable from the workspace.
     *
     * @param variableName name of the variable
     * @return value of the variable
     * @throws MatlabTaskException if unablet o get the variable
     */
    public Object get(String variableName) throws MatlabTaskException {
        try {
            return this.proxy.getVariable(variableName);
        } catch (MatlabInvocationException e) {
            throw new MatlabTaskException("Unable to get get the variable " + variableName, e);
        }
    }

    /**
     * Push a variable in to the workspace.
     *
     * @param variableName name of the variable
     * @param value the value of the variable
     * @throws MatlabTaskException if unable to set a variable
     */
    public void put(final String variableName, final Object value) throws MatlabTaskException {
        try {
            this.proxy.setVariable(variableName, value);
        } catch (MatlabInvocationException e) {
            throw new MatlabTaskException("Unable to set the variable " + variableName, e);
        }
    }

    /*********** PRIVATE INTERNAL CLASS ***********/

    /**
     * This class is used to create a MATLAB process under a
     * specific user
     */
    private static final class MatlabProcessAsUserCreator implements MatlabProcessCreator {

        private final String[] startUpOptions = new String[] { "-nosplash", "-nodesktop", "-wait" };
        private final OSUser user;
        private final String matlabLocation;
        private final File workingDirectory;

        public MatlabProcessAsUserCreator(final OSUser user, final String matlabLocation,
                final File workingDirectory) {
            this.user = user;
            this.matlabLocation = matlabLocation;
            this.workingDirectory = workingDirectory;
        }

        public void createMatlabProcess(String runArg) throws Exception {
            OSProcessBuilderFactory factory = new PAOSProcessBuilderFactory();
            OSProcessBuilder b = factory.getBuilder(this.user);

            // Attempt to run MATLAB
            ArrayList<String> commandList = new ArrayList<String>();
            commandList.add(this.matlabLocation);
            commandList.addAll(Arrays.asList(this.startUpOptions));
            commandList.add("-r");
            commandList.add("\"" + runArg + "\"");

            String[] command = (String[]) commandList.toArray(new String[commandList.size()]);
            b.command(command);
            b.directory(this.workingDirectory);
            b.start();
        }
    }
}