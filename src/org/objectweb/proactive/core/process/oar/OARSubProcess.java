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
package org.objectweb.proactive.core.process.oar;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.MessageSink;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.util.MessageLogger;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>
 * The OARSubProcess class is able to start any class, of the ProActive library,
 * on a cluster managed by OAR protocol. An istance of this class can be coupled for instance with
 * RLoginProcess or SSHProcess classes in order to log into the cluster's front end with rlogin or
 * ssh and then to run a job with OARSubProcess.
 * </p>
 * <p>
 * For instance:
 * </p><pre>
 * ..............
 * OARSubProcess oar = new OARSubProcess(new SimpleExternalProcess("ls -lsa"));
 * SSHProcess ssh = new RLoginProcess(oar, false);
 * ssh.setHostname("cluster_front_end_name");
 * ssh.startProcess();
 * ...............
 * </pre>
 * Anyway it is strongly advised to use XML Deployment files to run such processes
 * @author  ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class OARSubProcess extends AbstractExternalProcessDecorator {
    private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");
    public final static String DEFAULT_OARSUBPATH = "/usr/local/bin/oarsub";
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty(
            "user.home") + FILE_SEPARATOR + "ProActive" + FILE_SEPARATOR +
        "scripts" + FILE_SEPARATOR + "unix" + FILE_SEPARATOR + "cluster" +
        FILE_SEPARATOR + "oarStartRuntime.sh ";
    protected static final String DEFAULT_HOSTS_NUMBER = "1";
    protected static final String DEFAULT_BOOKING_DURATION = "00:01:00";
    protected String hostNumber = DEFAULT_HOSTS_NUMBER;
    protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;
    protected String bookingDuration = DEFAULT_BOOKING_DURATION;

    //Following options are not yet included in the command
    protected String interactive = "false";
    protected String outputFile;
    protected int jobID;
    protected String queueName;
    protected String hostList;

    public OARSubProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.hostname = null;
        this.command_path = DEFAULT_OARSUBPATH;
    }

    public OARSubProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        this.hostname = null;
        this.command_path = DEFAULT_OARSUBPATH;
    }

    //  ----------------------------------------------------------------------------------------
    //-----------------------Extends AbstractExternalProcessDecorator-------------------------
    //  ----------------------------------------------------------------------------------------
    public void setErrorMessageLogger(MessageLogger errorMessageLogger) {
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
     * Returns the number of nodes requested when running the job
     * @return the number of nodes requested when running the job
     */
    public String getHostsNumber() {
        return this.hostNumber;
    }

    /**
     * Sets the number of nodes requested when running the job
     * @param hosts
     */
    public void setHostsNumber(String hosts) {
        checkStarted();
        if (hosts != null) {
            this.hostNumber = hosts;
        }
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "oar_" + targetProcess.getProcessId();
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
     *  Set the booking duration of the cluster's nodes. The default is 00:01:00
     * @param d duration
     */
    public void setBookingDuration(String d) {
        this.bookingDuration = d;
    }

    //Following methods are not used yet for the command

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
     * Not yet included in the oar command
     * @return String
     */
    public String getHostList() {
        return hostList;
    }

    //    public String getProcessorPerNodeNumber() {
    //        return this.processorPerNode;
    //    }

    /**
     * Allows to launch this OARSubProcess with -I (interactive option)
     * Not yet included in the oar command
     * @param interactive true for -I option false otherwise
     */
    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    /** Set the output file to be passed to oar
     * Not yet included in the oar command
     * @param string
     */
    public void setOutputFile(String string) {
        outputFile = string;
    }

    //    /**
    //     * Sets the number of nodes requested when running the job
    //     * @param processorPerNode processor per node
    //     */
    //    public void setProcessorPerNodeNumber(String processorPerNode) {
    //        checkStarted();
    //        if (processorPerNode != null) {
    //            this.processorPerNode = processorPerNode;
    //        }
    //    }

    /**
     * Sets the value of the queue where the job will be launched. The default is 'normal'
     * Not yet included in the oar command
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
        //To be modified for OAR, does not work with the present code
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

    protected void internalStartProcess(String commandToExecute)
        throws java.io.IOException {
        ArrayList al = new ArrayList();

        //we divide the command into tokens
        //it's basically 3 blocks, the script path, the option and the rest
        Pattern p = Pattern.compile("(.*) .*(-c).*'(.*)'");
        Matcher m = p.matcher(command);
        if (!m.matches()) {
            System.err.println("Could not match command ");
            System.err.println(commandToExecute);
        }
        for (int i = 1; i <= m.groupCount(); i++) {
            //            System.out.println(m.group(i));
            al.add(m.group(i));
        }
        String[] command = (String[]) al.toArray(new String[] {  });

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

    /**
     * oarsub is not able to receive env variables or parameters for a script
     * we thus rely on the following trick, the command has the form
     * echo "real command" | qsub -I ...   oarStartRuntime.sh
     */
    protected String buildCommand() {
        StringBuffer oarsubCommand = new StringBuffer();
        oarsubCommand.append(
            "/bin/sh -c  'echo for i in \\`cat \\$OAR_NODEFILE\\` \\; do rsh \\$i  ");
        oarsubCommand.append(targetProcess.getCommand());
        oarsubCommand.append(" \\&  done  \\; wait > ");
        oarsubCommand.append(scriptLocation).append(" ; ");
        oarsubCommand.append(command_path);
        oarsubCommand.append(" ");
        oarsubCommand.append(buildResourceString()).append(" ");
        oarsubCommand.append(scriptLocation).append(" '");
        if (logger.isDebugEnabled()) {
            logger.debug("oarsub command is " + oarsubCommand.toString());
        }
        return oarsubCommand.toString();
    }

    protected StringBuffer buildResourceString() {
        StringBuffer rs = new StringBuffer();
        rs.append(" -l walltime=").append(bookingDuration).append(",");
        //to specify nodes and processor per nodes, the syntax is different from
        //other resources
        rs.append("nodes=").append(hostNumber);
        return rs;
    }

    /**
     * Implementation of a MessageLogger that look for the jobID of the launched job
     */
    public class ParserMessageLogger implements MessageLogger,
        java.io.Serializable {
        private boolean foundJobID;
        private boolean foundHostname;

        public ParserMessageLogger() {
        }

        public void log(String message) {
            int nbProcessor = (new Integer(hostNumber)).intValue();
            String h = parseHostname(message);
        }

        public void log(Throwable t) {
        }

        public void log(String message, Throwable t) {
        }
    }

    public static void main(String[] args) {
        System.out.println("Testing OARSubProcess");
        JVMProcessImpl p = new JVMProcessImpl();
        OARSubProcess oar = new OARSubProcess(p);
        oar.setHostsNumber("2");
        System.out.println(oar.buildCommand());
    }
}
