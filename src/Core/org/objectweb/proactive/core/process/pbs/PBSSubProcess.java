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
package org.objectweb.proactive.core.process.pbs;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.MessageSink;
import org.objectweb.proactive.core.process.UniversalProcess;


/**
 * <p>
 * The PBSSubProcess class is able to start any class, of the ProActive library,
 * on a cluster managed by PBS protocol. An instance of this class can be coupled for instance with
 * RlLoginProcess or SSHProcess classes in order to log into the cluster's front end with rlogin or
 * ssh and then to run a job with PBSBSubProcess.
 * </p>
 * <p>
 * For instance:
 * </p><pre>
 * ..............
 * PBSSubProcess PBS = new PBSSubProcess(new SimpleExternalProcess("ls -lsa"));
 * SSHProcess p = new SSHProcess(PBS, false);
 * p.setHostname("cluster_front_end_name");
 * p.startProcess();
 * ...............
 * </pre>
 * Anyway it is strongly advised to use XML Deployment files to run such processes
 * @author  ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class PBSSubProcess extends AbstractExternalProcessDecorator {
    public final static String DEFAULT_QSUBPATH = "/usr/local/bin/qsub";
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty("user.home") + File.separator +
        "ProActive" + File.separator + "scripts" + File.separator + "unix" + File.separator + "cluster" +
        File.separator + "pbsStartRuntime.sh ";
    protected static final String DEFAULT_HOSTS_NUMBER = "1";
    protected static final String DEFAULT_PROCESSOR_NUMBER = "1";
    protected static final String DEFAULT_BOOKING_DURATION = "00:01:00";
    protected String hostNumber = DEFAULT_HOSTS_NUMBER;
    protected String processorPerNode = DEFAULT_PROCESSOR_NUMBER;
    protected String bookingDuration = DEFAULT_BOOKING_DURATION;
    protected String interactive = "false";
    protected String outputFile;
    protected int jobID;
    protected String queueName;
    protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;

    //Following options is not yet available for pbs, it might be in the future
    protected String hostList;

    public PBSSubProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.hostname = null;
        this.command_path = DEFAULT_QSUBPATH;
    }

    public PBSSubProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        this.hostname = null;
        this.command_path = DEFAULT_QSUBPATH;
    }

    //  ----------------------------------------------------------------------------------------
    //-----------------------Extends AbstractExternalProcessDecorator-------------------------
    //  ----------------------------------------------------------------------------------------
    @Override
    public void setOutputMessageSink(MessageSink outputMessageSink) {
        if (outputMessageSink == null) {
            super.setOutputMessageSink(new SimpleMessageSink());
        } else {
            super.setOutputMessageSink(outputMessageSink);
        }
    }

    /**
     * Returns the number of hosts requested when running the job
     * @return the number of nodes requested when running the job
     */
    public String getHostsNumber() {
        return this.hostNumber;
    }

    /**
     * Sets the number of nodes requested when running the job
     * @param nodeNumber
     */
    public void setHostsNumber(String nodeNumber) {
        checkStarted();
        if (nodeNumber != null) {
            this.hostNumber = nodeNumber;
        }
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "pbs_" + targetProcess.getProcessId();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        return (new Integer(getProcessorPerNodeNumber()).intValue()) *
            (new Integer(getHostsNumber()).intValue());
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    /**
     * Allows to launch this PBSubProcess with -I (interactive option)
     * @param interactive true for -I option false otherwise
     */
    public void setInteractive(String interactive) {
        this.interactive = interactive;
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

    /** Set the output file to be passed to pbs
     * using the -o option
     * @param string
     */
    public void setOutputFile(String string) {
        outputFile = string;
    }

    /**
     *  Set the booking duration of the cluster's nodes. The default is 00:01:00
     * @param d duration
     */
    public void setBookingDuration(String d) {
        this.bookingDuration = d;
    }

    /**
     * Returns the number of processor per node requested when running the job
     */
    public String getProcessorPerNodeNumber() {
        return this.processorPerNode;
    }

    /**
     * Sets the number of processor per node requested when running the job
     * @param processorPerNode processor per node
     */
    public void setProcessorPerNodeNumber(String processorPerNode) {
        checkStarted();
        if (processorPerNode != null) {
            this.processorPerNode = processorPerNode;
        }
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
     * Not yet included in the oar command
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

    @Override
    protected String internalBuildCommand() {
        return buildEnvironmentCommand(); // + buildPSubCommand();
    }

    @Override
    protected void internalStartProcess(String commandToExecute) {
        //java does not seem to be able to deal with command
        //where there are quotation marks
        //thus we have to divide our command into tokens 
        //and pass them to the runtime
        //tokens are separated by space or quotation marks
        StringTokenizer st = new StringTokenizer(commandToExecute, " \"", true);
        String token;
        ArrayList<String> al = new ArrayList<String>();
        int quotationFound = 0;
        boolean commandFound = false;
        StringBuilder buff = new StringBuilder();
        while (st.hasMoreTokens()) {
            token = (String) st.nextElement();
            if (!commandFound) {
                if (token.equals("PROACTIVE_COMMAND=")) {
                    quotationFound = 0;
                    buff.append(token);
                    commandFound = true;
                } else {
                    if (!token.equals(" ")) {
                        al.add(token);
                    }
                }
            } else {
                //we have found the command, we are now looking for two quotation mark
                if (token.equals("\"")) {
                    quotationFound++;
                    buff.append(token);
                    if (quotationFound == 2) {
                        al.add(buff.toString());
                        commandFound = false;
                    }
                } else {
                    buff.append(token);
                }
            }
        }
        String[] command = al.toArray(new String[] {});

        try {
            externalProcess = Runtime.getRuntime().exec(command);
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(
                externalProcess.getInputStream()));
            java.io.BufferedReader err = new java.io.BufferedReader(new java.io.InputStreamReader(
                externalProcess.getErrorStream()));
            java.io.BufferedWriter out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
                externalProcess.getOutputStream()));
            handleProcess(in, out, err);
        } catch (java.io.IOException e) {
            isFinished = true;
            //throw e;
            e.printStackTrace();
        }
    }

    @Override
    protected String buildCommand() {
        StringBuilder qsubCommand = new StringBuilder();
        qsubCommand.append(command_path).append(" ");
        if (interactive.equals("true")) {
            qsubCommand.append(" -I");
        }

        qsubCommand.append(buildResourceString());
        if (outputFile != null) {
            qsubCommand.append(" -o ").append(outputFile).append(" ");
        }

        //the parameters for the script are given as an 
        //environment variable
        qsubCommand.append(" -v ").append("PROACTIVE_COMMAND=\" ").append(targetProcess.getCommand()).append(
                "\" ");
        qsubCommand.append(scriptLocation);

        if (queueName != null) {
            qsubCommand.append(" -q " + queueName + " ");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("qsub command is " + qsubCommand.toString());
        }

        //System.out.println("PBSSubProcess.buildCommand() " + qsubCommand);
        return qsubCommand.toString();
    }

    protected String buildResourceString() {
        StringBuilder rs = new StringBuilder();
        rs.append(" -l walltime=").append(bookingDuration).append(",");
        //to specify nodes and processor per nodes, the syntax is different from
        //other resources
        rs.append("nodes=").append(hostNumber).append(":");
        rs.append("ppn=").append(processorPerNode);
        return rs.toString();
    }
}
