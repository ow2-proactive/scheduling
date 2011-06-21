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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matlab.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProcessCreator;
import matlabcontrol.RemoteMatlabProxy;
import matlabcontrol.RemoteMatlabProxyFactory;

import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabInitException;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabTaskException;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfigBase;
import org.rzo.yajsw.os.ms.win.w32.WindowsProcess;


/**
 * This class uses the matlabcontrol API to establish a connection with MATLAB for
 * MATLAB tasks executions. There can be only one instance at a time.
 * Be careful this class is not thread safe.
 */
public class MatlabConnection {

    /** The proxy to the remote MATLAB */
    private RemoteMatlabProxy proxy;

    /** The thread executed on shutdown that releases this connection */
    private Thread shutdownHook;

    private CustomMatlabProcessCreator processCreator;

    private MatlabConnection() {
    }

    /**
     * Each time this method is called creates a new MATLAB process using
     * the matlabcontrol API.
     *
     * @param matlabExecutablePath The full path to the MATLAB executable
     * @param workingDir the directory where to start MATLAB
     * @throws MatlabInitException if MATLAB could not be initialized
     */
    public static MatlabConnection acquire(final String matlabExecutablePath, final File workingDir,
            final boolean debug, final String[] startupOptions) throws MatlabInitException {
        RemoteMatlabProxyFactory proxyFactory;

        final MatlabConnection conn = new MatlabConnection();

        // If a user is specified create the proxy factory with a specific
        // MATLAB process as user creator
        try {
            if (File.pathSeparatorChar == ';') {
		conn.processCreator = new WindowsMatlabProcessCreator(matlabExecutablePath, workingDir, startupOptions, debug);
            } else {
		conn.processCreator = new CustomMatlabProcessCreator(matlabExecutablePath, workingDir, startupOptions, debug);
            }
            proxyFactory = new RemoteMatlabProxyFactory(conn.processCreator);
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

            // clean factory
            proxyFactory.clean();

            // destroy process that can be spawned
            // even if we didn't managed to get the proxy
            conn.processCreator.killProcess();

            throw me;
        }

        conn.proxy = proxy;

        // Add shutdown hook to release the connection on jvm exit
        conn.shutdownHook = new Thread(new Runnable() {
            public final void run() {
                conn.release();
            }
        });
        Runtime.getRuntime().addShutdownHook(conn.shutdownHook);

        // Return a new MATLAB connection
        return conn;
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
        // Stop MATLAB use true for immediate
        try {
            this.proxy.exit(true);
        } catch (Exception e) {
            // Here maybe we should kill the process it self ... need more tests
        }

        // Clean threads used by the proxy
        this.proxy.clean();

        // Kill the MATLAB process
        this.processCreator.killProcess();

        this.proxy = null;
        // Remove the shutdown hook
        try {
            Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
        } catch (Exception e) {
        }
        System.gc();
    }

    /**
     * Evaluate the given string in the workspace.
     *
     * @param command the command to evaluate
     * @throws MatlabTaskException If unable to evaluate the command
     */
    public void evalString(final String command) throws MatlabTaskException {
        try {
            String out = this.proxy.eval(command);
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
     * @throws MatlabTaskException if unable to get the variable
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
	 * This class is used to create a MATLAB process under a specific user
	 */
	private static class CustomMatlabProcessCreator implements
			MatlabProcessCreator {

		protected final String tmpDir = System.getProperty("java.io.tmpdir");

		protected String nodeName;

		protected String[] startUpOptions;
		protected final String matlabLocation;
		protected final File workingDirectory;

		protected File logFile;
		protected boolean debug;

		private Process process;

		public CustomMatlabProcessCreator(final String matlabLocation,
				final File workingDirectory,
				String[] startUpOptions, boolean debug) {
			this.matlabLocation = matlabLocation;
			this.workingDirectory = workingDirectory;
			this.debug = debug;
			this.startUpOptions = startUpOptions;
			try {
				this.nodeName = MatSciEngineConfigBase.getNodeName();
			} catch (Exception e) {
				e.printStackTrace();
			}
			logFile = new File(tmpDir, "MatlabStart" + nodeName + ".log");
		}

		public Process createMatlabProcess(String runArg) throws Exception {
			// Attempt to run MATLAB
			final ArrayList<String> commandList = new ArrayList<String>();
			commandList.add(this.matlabLocation);
			commandList.addAll(Arrays.asList(this.startUpOptions));
			commandList.add("-logfile");
			commandList.add(logFile.toString());
			commandList.add("-r");
			commandList.add(runArg);

			String[] command = (String[]) commandList
					.toArray(new String[commandList.size()]);

			ProcessBuilder b = new ProcessBuilder();
			b.directory(this.workingDirectory);
			b.command(command);

			process = b.start();

			return process;

		}

		public File getLogFile() {
			return logFile;
		}

		public boolean isDebug() {
			return debug;
		}

		public void killProcess() {
			try {
				process.destroy();
			} catch (Exception e) {}
		}
	}

    /**
     * This class is used to create a MATLAB process under a
     * specific user
     */
    private static class WindowsMatlabProcessCreator extends CustomMatlabProcessCreator {

	private WindowsProcess process;

        public WindowsMatlabProcessCreator(String matlabLocation,
				File workingDirectory,
				String[] startUpOptions, boolean debug) {
			super(matlabLocation, workingDirectory, startUpOptions, debug);
		}

		public Process createMatlabProcess(String runArg) throws Exception {
            // Attempt to run MATLAB
            final ArrayList<String> commandList = new ArrayList<String>();
            commandList.add(this.matlabLocation);
            commandList.addAll(Arrays.asList(this.startUpOptions));
            commandList.add("-logfile");
            commandList.add(logFile.toString());
            commandList.add("-r");
            commandList.add("\"" + runArg + "\"");

            String[] command = (String[]) commandList.toArray(new String[commandList.size()]);

            process = new WindowsProcess();
            process.setWorkingDir(this.workingDirectory.getAbsolutePath());
            process.setCommand(command);
            process.setVisible(false);

            if (!process.start()) {
		throw new IllegalStateException("Unable to start MATLAB process " + command + " errorCode: " + WindowsProcess.getLastError());
            }

			// Sometimes the MATLAB process starts but dies very fast with exit
			// code 1
			// this must be considered as an error
//			try {
//				int exitValue = process.exitValue();
//				throw new Exception(
//						"The MATLAB process has exited abnormally with exit value "
//								+ exitValue
//								+ ", this can caused by a missing privilege of the user "
//								+ System.getenv("user.name"));
//			} catch (IllegalThreadStateException e) {
//				// This is normal behavior, it means the process is still
//				// running
//			}


            return null;
        }

		public void killProcess() {
			final List<Integer> pids = WindowsProcess.getProcessTree(process.getPid());
			for (final Integer i : pids) {
				if (! WindowsProcess.kill(i, 1)) {
					try {
						Runtime.getRuntime().exec("TASKKILL /F /T /PID " + i);
					} catch(Exception e) {
						// Maybe here kill using tree kill
					}
				}
			}
		}
    }
}