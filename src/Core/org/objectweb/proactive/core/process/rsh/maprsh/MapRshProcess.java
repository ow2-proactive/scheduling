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
package org.objectweb.proactive.core.process.rsh.maprsh;

import java.io.File;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.UniversalProcess;


/**
 * <p>
 * The MapRshProcess class is able to start any class, of the ProActive library,
 * using maprsh.
 * </p><p>
 * For instance:
 * </p><pre>
 * ..........
 * JVMProcess process = new JVMProcessImpl(new StandardOutputMessageLogger());
 * process.setParameters("///toto");
 * MapRshProcess maprsh = new MapRshProcess(process);
 * maprsh.setHostname("waha owenii");
 * maprsh.startProcess();
 * .....
 * </pre>
 * @author The ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public class MapRshProcess extends AbstractExternalProcessDecorator {
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty("user.home") + File.separator +
        "ProActive" + File.separator + "scripts" + File.separator + "unix" + File.separator +
        "gridexperiment" + File.separator + "oasis-exp";
    protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;
    private String parallelize = null;

    //private String logFile = System.getProperty("user.home")+File.separator+"oasisgridlog.txt";
    public MapRshProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
    }

    public MapRshProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
    }

    /**
     * Set the -n option with the given parameter for the maprsh command
     * @param parallelize
     */
    public void setParallelization(String parallelize) {
        this.parallelize = parallelize;
    }

    /**
     * Returns the degree of parallelization of maprsh command (value of -n option)
     * @return String
     */
    public String getParallelization() {
        return this.parallelize;
    }

    /**
     * Sets the variable scriptLocation with the given location
     * @param scriptLocation
     */
    public void setScriptLocation(String scriptLocation) {
        this.scriptLocation = scriptLocation;
    }

    /**
     * Returns the value of scriptLocation
     * @return String
     */
    public String getScriptLocation() {
        return scriptLocation;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "maprsh_" + targetProcess.getProcessId();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        return targetProcess.getNodeNumber();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected String internalBuildCommand() {
        return buildMapRshCommand() + buildEnvironmentCommand();
    }

    protected String buildMapRshCommand() {
        StringBuilder command = new StringBuilder();
        try {
            java.io.File script = new java.io.File(scriptLocation);
            byte[] b = getBytesFromInputStream(new java.io.FileInputStream(script));
            String scriptText = new String(b);
            scriptText = removeJavaCommand(scriptText);
            //System.out.println(scriptText);
            scriptText = appendJavaCommand(scriptText);
            if (logger.isDebugEnabled()) {
                logger.debug(scriptText);
            }
            b = scriptText.getBytes();
            // script.delete();
            java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(script));
            out.write(b, 0, b.length);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        command.append("/usr/local/bin/maprsh ");
        if (parallelize != null) {
            command.append(parallelize + " ");
        }
        command.append(scriptLocation + " " + hostname);
        if (logger.isDebugEnabled()) {
            logger.debug(command.toString());
        }
        return command.toString();
    }

    /**
     * Method appendJavaCommand.
     * @param scriptText
     * @return String
     */
    private String appendJavaCommand(String scriptText) {
        StringBuilder newScriptText = new StringBuilder(scriptText.length());
        String targetCommand = targetProcess.getCommand();

        newScriptText.append(scriptText);
        newScriptText.append("\ntime " + targetCommand + " ) & \n");
        return newScriptText.toString();
    }

    /**
     * Method removeJavaCommand.
     * @param scriptText
     * @return String
     */
    private String removeJavaCommand(String scriptText) {
        int marker = scriptText.lastIndexOf("}");
        String newScriptText = scriptText.substring(0, marker + 1);

        //System.out.println(newScriptText);
        return newScriptText;
    }

    public static void main(String[] args) {
        try {
            JVMProcess process = new JVMProcessImpl(new StandardOutputMessageLogger());
            process.setParameters("///toto");

            //ExternalProcess process = new SimpleExternalProcess("ls -la");
            MapRshProcess maprsh = new MapRshProcess(process);
            maprsh.setHostname("waha owenii");
            maprsh.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the InputStream
     * @param in the inputstream of the class file
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private static byte[] getBytesFromInputStream(java.io.InputStream in) throws java.io.IOException {
        java.io.DataInputStream din = new java.io.DataInputStream(in);
        byte[] bytecodes = new byte[in.available()];
        try {
            din.readFully(bytecodes);
        } finally {
            if (din != null) {
                din.close();
            }
        }
        return bytecodes;
    }
}
