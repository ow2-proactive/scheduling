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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.MessageSink;
import org.objectweb.proactive.core.util.MessageLogger;


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
 * RLoginProcess p = new RLoginProcess(PBS, false);
 * p.setHostname("cluster_front_end_name");
 * p.startProcess();
 * ...............
 * </pre>
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public class OARSubProcess extends AbstractExternalProcessDecorator {
    private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");
    public final static String DEFAULT_OARSUBPATH = "/usr/local/bin/oarsub";
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty(
            "user.home") + FILE_SEPARATOR + "workProActive/ProActive" +
        FILE_SEPARATOR + "scripts" + FILE_SEPARATOR + "unix" + FILE_SEPARATOR +
        "cluster" + FILE_SEPARATOR + "oarStartRuntime.sh ";
    protected static final String DEFAULT_HOSTS_NUMBER = "1";

    protected static final String DEFAULT_BOOKING_DURATION = "00:01:00";
    protected String hosts = DEFAULT_HOSTS_NUMBER;

    protected String bookingDuration = DEFAULT_BOOKING_DURATION;
    protected String interactive = "false";
    protected String outputFile;
    protected int jobID;
    protected String queueName;
    protected String hostList;
    protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;

    public OARSubProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.hostname = null;
    }

    public OARSubProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        this.hostname = null;
    }

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
     * return the list of the hostnames on which the job is running
     * the message should be in the form : host1/processor ... hostn/processor
     * @param message
     * @return the hostname
     */
    protected String parseHostname(String message) {
        String result = new String();
        if (logger.isDebugEnabled()) {
            logger.info("parseHostname() analyzing " + message);
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
        oarsubCommand.append(DEFAULT_OARSUBPATH);
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
        rs.append("nodes=").append(hosts);
        return rs;
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

    public String getHostsNumber() {
        return this.hosts;
    }

    //    public String getProcessorPerNodeNumber() {
    //        return this.processorPerNode;
    //    }

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
    }

    public void setScriptLocation(String location) {
        checkStarted();
        //     if (location != null) {
        this.scriptLocation = location;
        //    }
    }

    /** Set the output file to be passed to prun
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
     * Implementation of a MessageLogger that look for the jobID of the launched job
     */
    public class ParserMessageLogger implements MessageLogger,
        java.io.Serializable {
        private boolean foundJobID;
        private boolean foundHostname;

        public ParserMessageLogger() {
        }

        public void log(String message) {
            int nbProcessor = (new Integer(hosts)).intValue();
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
