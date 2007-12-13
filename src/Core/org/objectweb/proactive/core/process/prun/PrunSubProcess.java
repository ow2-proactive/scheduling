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
package org.objectweb.proactive.core.process.prun;

import java.util.StringTokenizer;

import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.MessageSink;
import org.objectweb.proactive.core.process.SimpleExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;


/**
 * <p>
 * The PrunSubProcess class is able to start any class, of the ProActive library,
 * on a cluster using prun on top of PBS. An instance of this class can be coupled for instance with
 * RlLoginProcess or SSHProcess classes in order to log into the cluster's front end with rlogin or
 * ssh and then to run a job with PBSBSubProcess.
 * </p>
 * <p>
 * For instance:
 * </p><pre>
 * ..............
 * PrunSubProcess prun = new PrunSubProcess(new SimpleExternalProcess("ls -lsa"));
 * RLoginProcess p = new RLoginProcess(prun, false);
 * p.setHostname("cluster_front_end_name");
 * p.startProcess();
 * ...............
 * </pre>
 * @author  ProActive Team
 * @version 1.0,  2003/09/20
 * @since   ProActive 2.0
 */
public class PrunSubProcess extends AbstractExternalProcessDecorator {
    //    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty(
    //            "user.home") + FILE_SEPARATOR + "ProActive" + FILE_SEPARATOR +
    //        "scripts" + FILE_SEPARATOR + "unix" + FILE_SEPARATOR + "cluster" +
    //        FILE_SEPARATOR + "startRuntime.sh ";
    //  public final static String DEFAULT_PBSPATH = FILE_SEPARATOR + "usr" +
    //      FILE_SEPARATOR + "local" + FILE_SEPARATOR + "pbs" + FILE_SEPARATOR +
    //      "bin";
    //    public final static String DEFAULT_QSUBPATH = DEFAULT_PBSPATH +
    //        FILE_SEPARATOR + "qsub";
    public final static String DEFAULT_PRUNPATH = "/usr/local/bin/prun";

    //  public final static String DEFAULT_QJOBPATH = DEFAULT_PBSPATH +
    //       FILE_SEPARATOR + "qjobs";
    // public static final String DEFAULT_QUEUE_NAME = "normal";
    protected static final String DEFAULT_HOSTS_NUMBER = "1";
    protected static final String DEFAULT_PROCESSOR_NUMBER = "1";
    protected static final String DEFAULT_BOOKING_DURATION = "00:01:00";
    private static final String FILE_TRANSFER_DEFAULT_PROTOCOL = null;
    protected int jobID;
    protected String queueName;
    protected String hostList;

    //  protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;
    protected String hosts = DEFAULT_HOSTS_NUMBER;
    protected String processorPerNode = DEFAULT_PROCESSOR_NUMBER;
    protected String bookingDuration = DEFAULT_BOOKING_DURATION;
    protected String interactive = "false";
    protected String outputFile;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new PBSBsubProcess
     * Used with XML Descriptors
     */
    public PrunSubProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.hostname = null;
        this.command_path = DEFAULT_PRUNPATH;
    }

    /**
     * Creates a new PBSBsubProcess
     * @param targetProcess The target process associated to this process. The target process
     * represents the process that will be launched with the qsub command
     */
    public PrunSubProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        this.hostname = null;
        this.command_path = DEFAULT_PRUNPATH;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //    public void setInputMessageLogger(RemoteProcessMessageLogger inputMessageLogger) {
    //        super.setInputMessageLogger(new CompositeMessageLogger(
    //                new ParserMessageLogger(), inputMessageLogger));
    //    }
    @Override
    public void setOutputMessageSink(MessageSink outputMessageSink) {
        if (outputMessageSink == null) {
            super.setOutputMessageSink(new SimpleMessageSink());
        } else {
            super.setOutputMessageSink(outputMessageSink);
        }
    }

    /**
     * Builds qdel command and encapsulates it in a process
     * @param jobID The id of the job previously launched
     * @return ExternalProcess The process encapsulating the bkill command
     */
    public static ExternalProcess buildBKillProcess(int jobID) {
        return new SimpleExternalProcess("qdel " + jobID);
    }

    public static void main(String[] args) {
        ProActiveConfiguration.load();
        try {
            PrunSubProcess p = new PrunSubProcess(new SimpleExternalProcess("/bin/ls "));
            p.setHostsNumber("2");
            p.setQueueName("plugtest");
            p.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the filename given to prun using -o
     */
    public String getOutputFile() {
        return outputFile;
    }

    /** Set the output file to be passed to prun
     * using the -o option
     * @param string
     */
    public void setOutputFile(String string) {
        outputFile = string;
    }

    /**
     * Returns the id of the job associated to this process
     * @return int
     */
    public int getJobID() {
        return jobID;
    }

    /**
     * Returns the name of the queue where the job was launched
     * @return String
     */
    public String getQueueName() {
        return queueName;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.process.UniversalProcess#stopProcess()
     */
    @Override
    public void stopProcess() {
        //System.out.println("PrunSubProcess.stopProcess()");
        super.stopProcess();
    }

    /**
     * Sets the value of the queue where the job will be launched. The default is 'normal'
     * @param queueName
     */
    public void setQueueName(String queueName) {
        checkStarted();
        if (queueName == null) {
            throw new NullPointerException();
        }
        this.queueName = queueName;
    }

    /**
     * Sets the value of the hostList parameter with the given value
     * @param hostList
     */
    public void setHostList(String hostList) {
        checkStarted();
        this.hostList = hostList;
    }

    /**
     * Returns the hostList value of this process.
     * @return String
     */
    public String getHostList() {
        return hostList;
    }

    //    /**
    //     * Returns true if this BsubProcess is lauched with -I option false otherwise
    //     * @return boolean
    //     */
    //    public String isInteractive() {
    //        return interactive;
    //    }

    /**
     *  Set the booking duration of the cluster's nodes. The default is 00:01:00
     * @param d duration
     */
    public void setBookingDuration(String d) {
        this.bookingDuration = d;
    }

    /**
     *  Return the booking duration of the cluster's nodes.
     * @return String
     */
    public String getBookingDuration() {
        return this.bookingDuration;
    }

    /**
     * return the list of the hostnames on which the job is running
     * the message should be in the form : host1/processor ... hostn/processor
     * @param message
     * @return the hostname
     */
    protected String parseHostname(String message) {
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

    /**
     * Allows to launch this BsubProcess with -I (interactive option)
     * @param interactive true for -I option false otherwise
     */
    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    /**
     * Sets the number of nodes requested when running the job
     * @param hosts
     */
    public void setHostsNumber(String hosts) {
        checkStarted();
        if (hosts != null) {
            this.hosts = hosts;
        }

        // System.out.println("-------- setNodeNumber() " + nodes);
    }

    /**
     * Sets the number of nodes requested when running the job
     * @param processorPerNode processor per node
     */
    public void setProcessorPerNodeNumber(String processorPerNode) {
        checkStarted();
        if (processorPerNode != null) {
            this.processorPerNode = processorPerNode;
        }
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "prun_" + targetProcess.getProcessId();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        return (new Integer(getProcessorPerNodeNumber()).intValue() * (new Integer(getHostsNumber())
                .intValue()));
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    /**
     * Returns the number of nodes requested for the job
     * @return String
     */
    public String getHostsNumber() {
        return this.hosts;
    }

    public String getProcessorPerNodeNumber() {
        return this.processorPerNode;
    }

    //    public void setScriptLocation(String location) {
    //        checkStarted();
    //        if (location != null) {
    //            this.scriptLocation = location;
    //        }
    //    }
    //
    //    public String getScriptLocation() {
    //        return scriptLocation;
    //    }
    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected String internalBuildCommand() {
        return buildEnvironmentCommand(); // + buildPSubCommand();
    }

    protected void sendJobDetailsCommand() {
        //   outputMessageSink.setMessage(buildBJobsCommand());
    }

    @Override
    protected void internalStartProcess(String command) throws java.io.IOException {
        //        System.out.println(
        //            "---------------Internal start process of PBSSubProcess " +
        //            command);
        if (logger.isDebugEnabled()) {
            logger.debug(command);
        }

        super.internalStartProcess(command);
    }

    @Override
    protected String buildCommand() {
        StringBuilder prunCommand = new StringBuilder();
        prunCommand.append(command_path);
        if (interactive.equals("true")) {
            prunCommand.append(" -I");
        }

        String[] commandAndOptions = separateCommandFromOptions(targetProcess.getCommand());
        prunCommand.append(" -no-panda -v -" + processorPerNode + " -t " + bookingDuration + " ");

        if (queueName != null) {
            //prunCommand.append("-native '-q " + queueName + "' ");
            prunCommand.append("-q " + queueName + " ");
        }

        if (hostList != null) {
            prunCommand.append("-m " + hostList + " ");
        }

        if (outputFile != null) {
            prunCommand.append("-o " + outputFile + " ");
        }

        prunCommand.append(commandAndOptions[0] + " " + hosts + " " + commandAndOptions[1]); // " -q " + queueName + " ");

        if (logger.isDebugEnabled()) {
            logger.debug("prun command is " + prunCommand.toString());
        }
        return prunCommand.toString();
    }

    protected String[] separateCommandFromOptions(String s) {
        String[] result = { "", "" };

        //the problem is that the command we get should be splitted in command+option
        //since prun likes to have options for the command at the very end
        //of the line, after the requested number of nodes
        StringTokenizer st = new StringTokenizer(s);
        if (st.countTokens() > 1) {
            result[0] = st.nextToken();
            while (st.hasMoreTokens()) {
                result[1] += st.nextToken();
                result[1] += " ";
            }
        }
        return result;
    }

    @Override
    public String getFileTransferDefaultCopyProtocol() {
        return FILE_TRANSFER_DEFAULT_PROTOCOL;
    }

    // end inner class CompositeMessageLogger
}
