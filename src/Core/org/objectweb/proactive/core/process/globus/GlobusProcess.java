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
package org.objectweb.proactive.core.process.globus;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.UniversalProcess;


/**
 * Globus Process implementation.
 * This implementation works only for ProActive deployment, and not to submit single commands
 * @author  ProActive Team
 * @version 2.0,  2005/09/20
 * @since   ProActive 3.0
 */
public class GlobusProcess extends AbstractExternalProcessDecorator {
    protected JVMProcessImpl jvmProcess;
    protected String count = "1";
    protected String stderr = null;
    protected String stdout = null;
    protected String queue = null;
    protected String maxTime = null;

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
    @Override
    protected String internalBuildCommand() {
        return buildRSLCommand();
    }

    public static void main(String[] args) {
        String RSL = "& (executable = /nfs/software/java/j2sdk1.4.2_07/bin/java )(count=5)";

        // String RSL = "& (executable = /user/rquilici/home/ProActive/scripts/unix/cluster/startRuntime.sh )(arguments='/user/rquilici/home/j2sdk1.4.2_05/bin/java -Dproactive.jobid=JOB-986939693  -cp /user/rquilici/home/ProActive/classes:/user/rquilici/home/ProActive/lib/asm.jar:/user/rquilici/home/ProActive/lib/log4j.jar:/user/rquilici/home/ProActive/lib/components/fractal.jar:/user/rquilici/home/ProActive/lib/xercesImpl.jar:/user/rquilici/home/ProActive/lib/bouncycastle.jar -Djava.security.policy=/user/rquilici/home/ProActive/scripts/proactive.java.policy -Dlog4j.configuration=file:/user/rquilici/home/ProActive/scripts/proactive-log4j org.objectweb.proactive.core.runtime.StartRuntime Renderer //sea.inria.fr/PA_JVM986939693_sea.inria.fr 1 globus_jvm Jvm4')(jobType=multiple)(count=5)";
        GridJob Job1 = new GridJob("viz-login.isi.edu/jobmanager-pbs", false);

        //GridJob Job1 = new GridJob("cluster.inria.fr", false);
        Job1.GlobusRun(RSL);
        //String jobOut = 
        //System.out.println(jobOut);
    }

    @Override
    protected void internalStartProcess(String rslCommand)
        throws java.io.IOException {
        GridJob Job1 = new GridJob(hostname, false);
        String jobOut = Job1.GlobusRun(getCommand());
        logger.info(jobOut);
    }

    /**
     * Method buildRSLCommand.
     * @return String
     */
    private String buildRSLCommand() {
        //here we guess that the process behind globus is a java process. Indeed it makes no sense to
        //run a globus process foloowed by something else, since we target direclty the globus frontend
        String java_command = ((JVMProcess) getTargetProcess()).getJavaPath();
        String initial_args = ((JVMProcess) getTargetProcess()).getCommand()
                               .substring(java_command.length() + 1);

        //This is because the GT2 provider doesn't support the = in the RSL command,
        //we surround the whole arg with \. This has to be removed for GT4 if one day 
        // we include the notion of provider, since GT4 supports =.
        String args = checkSyntax(initial_args);

        String RSLCommand = "& (executable = " + java_command + ")(arguments=" +
            args + ")(count=" + count + ")";

        if (stdout != null) {
            RSLCommand = RSLCommand + "(stdout=" + stdout + ")";
        }

        if (stderr != null) {
            RSLCommand = RSLCommand + "(stderr=" + stderr + ")";
        }

        if (queue != null) {
            RSLCommand = RSLCommand + "(queue=" + queue + ")";
        }

        if (environment != null) {
            RSLCommand = RSLCommand + "(environment=" +
                buildEnvironmentCommand() + ")";
        }
        if (maxTime != null) {
            RSLCommand = RSLCommand + "(maxTime=" + maxTime + ")";
        }
        return RSLCommand;
    }

    @Override
    protected String buildEnvironmentCommand() {
        if (environment == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
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
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "globus_" + targetProcess.getProcessId();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        return (new Integer(getCount()).intValue());
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    /**
     * @param string
     */
    private String checkSyntax(String args) {
        String formatted_args = "";
        String[] splitted_args = args.split("\\s");
        for (int i = 0; i < splitted_args.length; i++) {
            if (!(splitted_args[i].indexOf("=") < 0)) {
                splitted_args[i] = "\"" + splitted_args[i] + "\"";
            }
            formatted_args = formatted_args + " " + splitted_args[i];
        }
        return formatted_args;
    }

    /**
     * @return Returns the stderr.
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * @param stderr The stderr to set.
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    /**
     * @return Returns the stdout.
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * @param stdout The stdout to set.
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    /**
     * @return Returns the queue.
     */
    public String getQueue() {
        return queue;
    }

    /**
     * @param queue The queue to set.
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(String maxTime) {
        this.maxTime = maxTime;
    }
}
