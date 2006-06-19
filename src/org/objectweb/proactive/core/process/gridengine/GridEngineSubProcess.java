/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.core.process.gridengine;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.MessageSink;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;


/**
 * <p>
 * The GridEngineSubProcess class is able to start any class, of the ProActive library,
 * on a cluster managed by Sun Grid Engine protocol. An istance of this class can be coupled for instance with
 * RLoginProcess or SSHProcess classes in order to log into the cluster's front end with rlogin or
 * ssh and then to run a job with LSFBSubProcess.
 * </p>
 * <p>
 * For instance:
 * </p><pre>
 * ..............
 * GridEngineSubProcess sge = new GridEngineSubProcess(new SimpleExternalProcess("ls -lsa"));
 * SSHProcess ssh = new RLoginProcess(sge, false);
 * ssh.setHostname("cluster_front_end_name");
 * ssh.startProcess();
 * ...............
 * </pre>
 * Anyway it is strongly advised to use XML Deployment files to run such processes
 * @author  ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class GridEngineSubProcess extends AbstractExternalProcessDecorator {
    private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");
    public final static String DEFAULT_QSUBPATH = "/opt/gridengine/bin/glinux/qsub";
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty(
            "user.home") + FILE_SEPARATOR + "ProActive" + FILE_SEPARATOR +
        "scripts" + FILE_SEPARATOR + "unix" + FILE_SEPARATOR + "cluster" +
        FILE_SEPARATOR + "gridEngineStartRuntime.sh ";
    protected static final String DEFAULT_HOSTS_NUMBER = "1";
    protected static final String DEFAULT_PARALLEL_ENVIRONMENT = "make";
    protected static final String DEFAULT_BOOKING_DURATION = "00:01:00";
    protected String hostNumber = DEFAULT_HOSTS_NUMBER;
    protected String parallelEnvironment = DEFAULT_PARALLEL_ENVIRONMENT;
    protected String bookingDuration = DEFAULT_BOOKING_DURATION;
    protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;

    //Following options are not yet included in the command
    protected String interactive = "false";
    protected String outputFile;
    protected int jobID;
    protected String queueName;
    protected String hostList;

    public GridEngineSubProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.hostname = null;
        this.command_path = DEFAULT_QSUBPATH;
    }

    public GridEngineSubProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        this.hostname = null;
        this.command_path = DEFAULT_QSUBPATH;
    }

    //----------------------------------------------------------------------------------------
    //-----------------------Extends AbstractExternalProcessDecorator-------------------------
    //  ----------------------------------------------------------------------------------------
    public void setErrorMessageLogger(
        RemoteProcessMessageLogger errorMessageLogger) {
        super.setErrorMessageLogger(new CompositeMessageLogger(
                new ParserMessageLogger(), errorMessageLogger));
    }

    public void setOutputMessageSink(MessageSink outputMessageSink) {
        if (outputMessageSink == null) {
            super.setOutputMessageSink(new SimpleMessageSink());
        } else {
            super.setOutputMessageSink(outputMessageSink);
        }
    }

    /**
     *  Set the booking duration of the cluster's nodes. The default is 00:01:00
     * @param d duration
     */
    public void setBookingDuration(String d) {
        this.bookingDuration = d;
    }

    /**
     * Returns the number of nodes requested when running the job
     * @return the number of nodes requested when running the job
     */
    public String getHostsNumber() {
        return this.hostNumber;
    }

    /**
     * Sets the number of nodes requested when running the job
     * @param hostNumber the number of nodes requested when running the job
     */
    public void setHostsNumber(String hostNumber) {
        checkStarted();
        if (hostNumber != null) {
            this.hostNumber = hostNumber;
        }
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "gridengine_" + targetProcess.getProcessId();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        return (new Integer(getHostsNumber()).intValue());
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        ;
        return targetProcess.getFinalProcess();
    }

    /**
     * Sets the location of the script on the remote host
     * @param location
     */
    public void setScriptLocation(String location) {
        checkStarted();
        //     if (location != null) {
        this.scriptLocation = location;
        //    }
    }

    /**
     * Sets the parallel environmemnt for this GridEngineSubProcess
     * @param p the parallel environmemnt to use
     */
    public void setParallelEnvironment(String p) {
        this.parallelEnvironment = p;
    }

    /**
     * Returns the parallel environmemnt for this GridEngineSubProcess
     * @return the parallel environmemnt for this GridEngineSubProcess
     */
    public String getParallelEnvironment() {
        return this.parallelEnvironment;
    }

    /**
     * Sets the value of the hostList parameter with the given value
     * Not yet included in the sge command
     * @param hostList
     */
    public void setHostList(String hostList) {
        checkStarted();
        this.hostList = hostList;
    }

    /**
     * Returns the hostList value of this process.
     * Not yet included in the sge command
     * @return String
     */
    public String getHostList() {
        return hostList;
    }

    /**
     * Allows to launch this GridEngineSubProcess with -I (interactive option)
     * Not yet included in the sge command
     * @param interactive true for -I option false otherwise
     */
    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    /** Set the output file to be passed to sge
     * Not yet included in the sge command
     * @param string
     */
    public void setOutputFile(String string) {
        outputFile = string;
    }

    /**
     * Sets the value of the queue where the job will be launched. The default is 'normal'
     * Not yet included in the sge command
     * @param queueName
     */
    public void setQueueName(String queueName) {
        checkStarted();
        if (queueName == null) {
            throw new NullPointerException();
        }
        this.queueName = queueName;
    }

    protected String parseHostname(String message) {
        //To be modified for SGE, does not work with the present code
        String result = new String();
        if (logger.isDebugEnabled()) {
            logger.debug("parseHostname() analyzing " + message);
        }
        java.util.StringTokenizer st = new java.util.StringTokenizer(message);
        if (st.countTokens() < 2) {
            return null; //at least two tokens
        }
        if (!":".equals(st.nextToken())) {
            return null; //should start with :
        }

        while (st.hasMoreTokens()) {
            result += (st.nextToken());
            result += " ";
        }
        return result;
    }

    protected String internalBuildCommand() {
        return buildEnvironmentCommand(); // + buildPSubCommand();
    }

    protected String buildCommand() {
        StringBuffer qsubCommand = new StringBuffer();

        qsubCommand.append(command_path).append(" -S /bin/bash  ");
        if ((this.queueName != null) && (this.queueName.length() > 0)) {
            qsubCommand.append("-q ").append(queueName).append(" ");
        }

        if ((this.outputFile != null) && (this.outputFile.length() > 0)) {
            qsubCommand.append("-o ").append(outputFile).append(" ");
        }

        if ((this.parallelEnvironment != null) &&
                (this.parallelEnvironment.length() > 0)) {
            qsubCommand.append("-pe ").append(parallelEnvironment).append(" ");
        }

        if ((this.hostNumber != null) && (this.hostNumber.length() > 0)) {
            qsubCommand.append(hostNumber).append(" ");
        } else {
            qsubCommand.append("1").append(" ");
        }

        qsubCommand.append(scriptLocation).append(" ");
        qsubCommand.append(targetProcess.getCommand());

        if (logger.isDebugEnabled()) {
            logger.debug("qsub command is " + qsubCommand.toString());
        }
        System.out.println("GridEngineSubProcess.buildCommand() " +
            qsubCommand);
        return qsubCommand.toString();
    }

    protected StringBuffer buildResourceString() {
        StringBuffer rs = new StringBuffer();
        rs.append(" -l walltime=").append(bookingDuration).append(",");
        //to specify nodes and processor per nodes, the syntax is different from
        //other resources
        rs.append("nodes=").append(hostNumber).append(":");

        return rs;
    }

    /**
     * Implementation of a RemoteProcessMessageLogger that look for the jobID of the launched job
     *
     */
    public class ParserMessageLogger implements RemoteProcessMessageLogger,
        java.io.Serializable {
        private boolean foundJobID;
        private boolean foundHostname;

        public ParserMessageLogger() {
        }

        public void log(String message) {
            //System.out.println(" >>> log :" +message);
            int nbProcessor = (new Integer(hostNumber)).intValue();
            String h = parseHostname(message);

            //            if (h != null) {
            //                System.out.println(" ---- hostname " + h);
            //            }
        }

        public void log(Throwable t) {
        }

        public void log(String message, Throwable t) {
        }
    }
}
