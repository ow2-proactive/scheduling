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
package org.objectweb.proactive.core.process;

import org.objectweb.proactive.core.util.OperatingSystem;


/**
 * <p>
 * The JVMProcess class is able to start localy any class of the ProActive library by
 * creating a Java Virtual Machine.
 * </p>
 * @author The ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public interface JVMProcess extends ExternalProcess {

    /**
     * Returns the classpath associated to this process
     * @return String
     */
    public String getClasspath();

    /**
     * Sets the classpath for this process
     * @param classpath The value of the classpath environment variable
     */
    public void setClasspath(String classpath);

    /**
     * Returns the java path associated to this process.
     * @return String The path to the java command
     */
    public String getJavaPath();

    /**
     * Sets the java path for this process
     * @param javaPath The value of the path to execute 'java' command
     */
    public void setJavaPath(String javaPath);

    /** Returns the boot classpath associated to this process
     * @return String the boot classpath of the java command
     */
    public String getBootClasspath();

    /**
     *  Sets the boot classpath associated to this process
     * @param bootClasspath The boot classpath of the java command
     */
    public void setBootClasspath(String bootClasspath);

    /**
     * Returns the location (path) to the policy file
     * @return String The path to the policy file
     */
    public String getPolicyFile();

    /**
     * Sets the location of the policy file
     * @param policyFilePath The value of the path to the policy file
     */
    public void setPolicyFile(String policyFilePath);

    /**
     * Returns the location of the log4j property file.
     * @return String the location of the log4j property file
     */
    public String getLog4jFile();

    /**
     * Sets the location of the log4j property file.
     * @param log4fFilePath The value of the path to the log4j property file
     */
    public void setLog4jFile(String log4fFilePath);

    /**
     * Returns the class name that this process is about to start
     * @return String The value of the class that this process is going to start
     */
    public String getClassname();

    /**
     * Sets the value of the class to start for this process
     * @param classname The name of the class to start
     */
    public void setClassname(String classname);

    /**
     * Reset to empty value parameters associated to the class that this process
     * is going to start
     */
    public void resetParameters();

    /**
     * Returns parameters associated to the class that this process is going to start
     * @return String The value of the parameters of the class
     */
    public String getParameters();

    /**
     * Sets the parameters of the class to start with the given value
     * @param parameters Paramaters to be given in order to start the class
     */
    public void setParameters(String parameters);

    /**
     * Sets the options of the jvm to start
     * <p>
     * For instance:
     * </p>
     * <pre>
     * jvmProcess.set JvmOptions("-verbose -Xms300M -Xmx300m");
     * </pre>
     * @param options Options to be given in order to start the jvm
     */
    public void setJvmOptions(String options);

    /**
     * Returns this jvm options
     * @return this jvm options
     */
    public String getJvmOptions();

    /**
     * Sets the overwrite attribute with the given value
     * @param overwrite
     */
    public void setOverwrite(boolean overwrite);

    /**
     * Allows this JVMProcess to extend another JVMProcessImpl.
     * First implementation of this method. This method must be used carefully.
     * Here is the basic behavior:
     * If attributes are modified on this JVM using set methods, they keep the modified
     * value, otherwise they take the value of the extended jvm. This doesn't apply for
     * classname, and  parameters.
     * Moreover, for the jvm options, the default behavior is to append the options of this
     * jvm to the extended jvm ones, unless the setOverwrite is called previously with true as parameters
     * In that case the jvm options of the extended jvm are ignored.
     * At this point this method is only used in deployment descriptors.
     * @param jvmProcess the extended jvm
     */
    public void setExtendedJVM(JVMProcessImpl jvmProcess);

    public void setPriority(PriorityLevel priority);

    public void setOperatingSystem(OperatingSystem os);

    public OperatingSystem getOperatingSystem();

    public enum PriorityLevel {
        low(19, "low"), normal(0, "normal"), high(-10, "high");
        private int unixValue;
        private String windowsValue;

        PriorityLevel(int unixValue, String windowsValue) {
            this.unixValue = unixValue;
            this.windowsValue = windowsValue;
        }

        public String unixCmd() {
            return "nice -n " + unixValue + " ";
        }

        public String windowsCmd() {
            return "start /" + windowsValue + " ";
        }
    }
}
