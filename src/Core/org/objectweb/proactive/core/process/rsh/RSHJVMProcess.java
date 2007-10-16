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
package org.objectweb.proactive.core.process.rsh;

import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;


/**
 * <p>
 * The RSHJVMProcess class is able to start any class, of the ProActive library,
 * using rsh protocol. The difference between this class and RSHProcess class is that the target process
 * for this class is automatically a JVMProcess, whereas for the RSHProcess, the target process has to be defined
 * and can be any command and any process.
 * </p><p>
 * For instance:
 * </p><pre>
 * .......
 * RSHProcess rsh = new RSHJVMProcess(new StandardOutputMessageLogger());
 * rsh.setHostname("machine_name");
 * rsh.startProcess();
 * .....
 * </pre>
 * <p>
 * This piece of code creates a new RSHJVMProcess. It allows to log on a remote machine with the rsh protocol and then,
 * on this machine to create a Java Virtual Machine, by launching a ProActive java class.
 * </p>
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public class RSHJVMProcess extends RSHProcess implements JVMProcess {
    protected JVMProcessImpl jvmProcess;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    // 

    /**
     * Creates a new RSHJVMProcess
     * Used with XML Descriptor
     */
    public RSHJVMProcess() {
        super();
    }

    /**
     * Creates a new RSHJVMProcess
     * @param messageLogger The logger that handles input and error stream of the target JVMProcess
     */
    public RSHJVMProcess(RemoteProcessMessageLogger messageLogger) {
        this(messageLogger, messageLogger);
    }

    /**
     * Creates a new RSHJVMProcess
     * @param inputMessageLogger The logger that handles input stream of the target JVMProcess
     * @param errorMessageLogger The logger that handles error stream of the target JVMProcess
     */
    public RSHJVMProcess(RemoteProcessMessageLogger inputMessageLogger,
        RemoteProcessMessageLogger errorMessageLogger) {
        super(new JVMProcessImpl(inputMessageLogger, errorMessageLogger));
        jvmProcess = (JVMProcessImpl) targetProcess;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public static void main(String[] args) {
        try {
            RSHProcess rsh = new RSHJVMProcess(new StandardOutputMessageLogger());
            rsh.setHostname("solida");
            rsh.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // -- implements JVMProcess -----------------------------------------------
    //

    /**
     * Returns the classpath associated to the target JVMProcess
     * @return String
     */
    public String getClasspath() {
        return jvmProcess.getClasspath();
    }

    /**
     * Sets the classpath for the target JVMProcess
     * @param classpath The value of the classpath environment variable
     */
    public void setClasspath(String classpath) {
        checkStarted();
        jvmProcess.setClasspath(classpath);
    }

    /**
     * Returns the java path associated the target JVMProcess
     * @return String The path to the java command
     */
    public String getJavaPath() {
        return jvmProcess.getJavaPath();
    }

    /**
     * Sets the java path for the target JVMProcess
     * @param javaPath The value of the path to execute 'java' command
     */
    public void setJavaPath(String javaPath) {
        checkStarted();
        jvmProcess.setJavaPath(javaPath);
    }

    /** Returns the boot classpath of the target JVMProcess
     * @return String the boot classpath of the java command
     */
    public String getBootClasspath() {
        checkStarted();
        return jvmProcess.getBootClasspath();
    }

    /**
     *  Sets the boot classpath  for the target JVMProcess
     * @param bootClasspath The boot classpath of the java command
     */
    public void setBootClasspath(String bootClasspath) {
        checkStarted();
        jvmProcess.setBootClasspath(bootClasspath);
    }

    /**
     * Returns the location (path) to the policy file for the target JVMProcess
     * @return String The path to the policy file
     */
    public String getPolicyFile() {
        return jvmProcess.getPolicyFile();
    }

    /**
     * Sets the location of the policy file for the target JVMProcess
     * @param policyFile The value of the path to the policy file
     */
    public void setPolicyFile(String policyFile) {
        checkStarted();
        jvmProcess.setPolicyFile(policyFile);
    }

    public String getLog4jFile() {
        return jvmProcess.getLog4jFile();
    }

    public void setLog4jFile(String log4jFile) {
        checkStarted();
        jvmProcess.setLog4jFile(log4jFile);
    }

    /**
     * Returns the class name that the target JVMProcess is about to start
     * @return String The value of the class that the target JVMProcess is going to start
     */
    public String getClassname() {
        return jvmProcess.getClassname();
    }

    /**
     * Sets the value of the class to start for the target JVMProcess
     * @param classname The name of the class to start
     */
    public void setClassname(String classname) {
        checkStarted();
        jvmProcess.setClassname(classname);
    }

    /**
     * Returns parameters associated to the class that the target JVMProcess is going to start
     * @return String The value of the parameters of the class
     */
    public String getParameters() {
        return jvmProcess.getParameters();
    }

    /**
     * Sets the parameters of the class to start with the given value for the target JVMProcess
     * @param parameters Paramaters to be given in order to start the class
     */
    public void setParameters(String parameters) {
        checkStarted();
        jvmProcess.setParameters(parameters);
    }

    /**
     * Reset to empty value parameters associated to the class that this process
     * is going to start
     */
    public void resetParameters() {
        jvmProcess.resetParameters();
    }

    /**
     * Sets the parameters of the jvm to start with the given parameters for the target JVMProcess
     * @param parameters Paramaters to be given in order to start the jvm
     */
    public void setJvmOptions(String parameters) {
        jvmProcess.setJvmOptions(parameters);
    }

    public String getJvmOptions() {
        return jvmProcess.getJvmOptions();
    }

    public void setOverwrite(boolean overwrite) {
        jvmProcess.setOverwrite(overwrite);
    }

    public void setExtendedJVM(JVMProcessImpl jvmProcess) {
        jvmProcess.setExtendedJVM(jvmProcess);
    }

    public int getNewGroupId() {
        return jvmProcess.getNewGroupId();
    }

    public void setPriority(PriorityLevel priority) {
        jvmProcess.setPriority(priority);
    }

    public void setOperatingSystem(OperatingSystem os) {
        jvmProcess.setOperatingSystem(os);
    }

    public OperatingSystem getOperatingSystem() {
        return jvmProcess.getOperatingSystem();
    }
    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
}
