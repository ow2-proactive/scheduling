/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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

/**
 * @author rquilici
 *
 * This class represents a Globus Process. 
 */
package org.objectweb.proactive.core.process.globus;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;


/**
 * @author rquilici
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class GlobusProcess extends AbstractExternalProcessDecorator {
    protected JVMProcessImpl jvmProcess;
    private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty(
            "user.home") + FILE_SEPARATOR + "ProActive" + FILE_SEPARATOR +
        "scripts" + FILE_SEPARATOR + "unix" + FILE_SEPARATOR + "cluster" +
        FILE_SEPARATOR + "startRuntime.sh ";
    private String count = "1";
    private String scriptLocation = DEFAULT_SCRIPT_LOCATION;

    //===========================================================
    // Constructor
    //===========================================================

    /**
     * Creates a new instance of GlobusProcess
     */
    public GlobusProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.hostname = null;
    }

    /**
     * Creates a new instance of GlobusProcess. The process given as parameter represents the target process
     */
    public GlobusProcess(JVMProcess process) {
        super(process, GIVE_COMMAND_AS_PARAMETER);
        this.jvmProcess = (JVMProcessImpl) targetProcess;
        this.hostname = null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.process.AbstractExternalProcessDecorator#internalBuildCommand()
     */
    protected String internalBuildCommand() {
        return buildRSLCommand();
    }

    public static void main(String[] args) {
        //String RSL = "& (executable = /bin/ls)(directory=/net/home/rquilici)(arguments=-l)";
        String RSL = "& (executable = " + DEFAULT_SCRIPT_LOCATION +
            ")(count=5)";
        GridJob Job1 = new GridJob("cluster.inria.fr", false);

        String jobOut = Job1.GlobusRun(RSL);

        System.out.println(jobOut);
    }

    protected void internalStartProcess(String rslCommand)
        throws java.io.IOException {
        GridJob Job1 = new GridJob(hostname, true);
        String jobOut = Job1.GlobusRun(getCommand());

        //Job1.GlobusRun(getCommand());
        logger.info(jobOut);
    }

    /**
     * Method buildRSLCommand.
     * @return String
     */
    private String buildRSLCommand() {
        String RSLCommand = "& (executable = " + scriptLocation +
            ")(arguments='" + getTargetProcess().getCommand() + "')(count=" +
            count + ")(jobType=single)";

        //		if (count != null) RSLCommand = RSLCommand+"(count="+count+")";
        //		RSLCommand = RSLCommand+"(jobType=single)";
        if (environment != null) {
            RSLCommand = RSLCommand + "(environment=" +
                buildEnvironmentCommand() + ")";
        }
        return RSLCommand;
        //		String rslCommand ="&(executable=" + ((JVMProcess)targetProcess).getJavaPath()+")" + 
        //												"(arguments='-Djava.security.policy="+((JVMProcess)targetProcess).getPolicyFile()+"' '-Dlog4j.configuration="+((JVMProcess)targetProcess).getLog4jFile()+"' "+((JVMProcess)targetProcess).getClassname() + " " + ((JVMProcess)targetProcess).getParameters() +")"+
        //												"(environment=(CLASSPATH "+((JVMProcess)targetProcess).getClasspath()+")"+buildEnvironmentCommand()+")";
        //		if (count != null) rslCommand = rslCommand+"(count="+count+")(jobType=single)";
        //		System.out.println(rslCommand);
        //				return rslCommand;
    }

    protected String buildEnvironmentCommand() {
        if (environment == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        String[] globusEnvironment = new String[environment.length];
        for (int i = 0; i < environment.length; i++) {
            globusEnvironment[i] = environment[i].replace('=', ' ');
            sb.append("(");
            sb.append(globusEnvironment[i]);
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * Returns the count.
     * @return String
     */
    public String getCount() {
        return count;
    }

    /**
     * Sets the count.
     * @param count The count to set
     */
    public void setCount(String count) {
        this.count = count;
    }

    /**
     * Returns the scriptLocation.
     * @return String
     */
    public String getScriptLocation() {
        return scriptLocation;
    }

    /**
     * Sets the scriptLocation.
     * @param scriptLocation The scriptLocation to set
     */
    public void setScriptLocation(String scriptLocation) {
        this.scriptLocation = scriptLocation;
    }
}
