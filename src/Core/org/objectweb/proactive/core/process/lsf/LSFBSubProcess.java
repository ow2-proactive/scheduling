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
package org.objectweb.proactive.core.process.lsf;

import java.io.File;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.MessageSink;
import org.objectweb.proactive.core.process.SimpleExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;


/**
 * <p>
 * The LSFBSubProcess class is able to start any class, of the ProActive library,
 * on a cluster managed by LSF prtocol. An istance of this class can be coupled for instance with
 * RlLoginProcess or SSHProcess classes in order to log into the cluster's front end with rlogin or
 * ssh and then to run a job with LSFBSubProcess.
 * </p>
 * <p>
 * For instance:
 * </p><pre>
 * ..............
 * LSFBSubProcess lsf = new LSFBSubProcess(new SimpleExternalProcess("ls -lsa"));
 * RLoginProcess p = new RLoginProcess(lsf, false);
 * p.setHostname("cluster_front_end_name");
 * p.startProcess();
 * ...............
 * </pre>
 * @author The ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public class LSFBSubProcess extends AbstractExternalProcessDecorator {
    protected static final String DEFAULT_SCRIPT_LOCATION = System.getProperty("user.home") + File.separator +
        "ProActive" + File.separator + "scripts" + File.separator + "unix" + File.separator + "cluster" +
        File.separator + "startRuntime.sh ";
    public final static String DEFAULT_LSFPATH = File.separator + "usr" + File.separator + "local" +
        File.separator + "lsf" + File.separator + "bin";
    public final static String DEFAULT_BSUBPATH = DEFAULT_LSFPATH + File.separator + "bsub";
    public final static String DEFAULT_BJOBPATH = DEFAULT_LSFPATH + File.separator + "bjobs";
    public static final String DEFAULT_QUEUE_NAME = "normal";
    protected static final String DEFAULT_PROCESSOR_NUMBER = "1";
    protected int jobID;
    protected String queueName = DEFAULT_QUEUE_NAME;
    protected String hostList;
    protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;
    protected String processor = DEFAULT_PROCESSOR_NUMBER;
    protected String interactive = "false";
    protected String res_requirement = "";
    protected String jobname;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new LSFBsubProcess
     * Used with XML Descriptors
     */
    public LSFBSubProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.hostname = null;
        this.command_path = DEFAULT_BSUBPATH;
    }

    /**
     * Creates a new LSFBsubProcess
     * @param targetProcess The target process associated to this process. The target process
     * represents the process that will be launched with the bsub command
     */
    public LSFBSubProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        this.hostname = null;
        this.command_path = DEFAULT_BSUBPATH;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    @Override
    public void setInputMessageLogger(RemoteProcessMessageLogger inputMessageLogger) {
        super
                .setInputMessageLogger(new CompositeMessageLogger(new ParserMessageLogger(),
                    inputMessageLogger));
    }

    @Override
    public void setOutputMessageSink(MessageSink outputMessageSink) {
        if (outputMessageSink == null) {
            super.setOutputMessageSink(new SimpleMessageSink());
        } else {
            super.setOutputMessageSink(outputMessageSink);
        }
    }

    /**
     * Builds bkill command and encapsulates it in a process
     * @param jobID The id of the job previously launched
     * @return ExternalProcess The process encapsulating the bkill command
     */
    public static ExternalProcess buildBKillProcess(int jobID) {
        return new SimpleExternalProcess("bkill " + jobID);
    }

    public static void main(String[] args) {
        try {
            LSFBSubProcess p = new LSFBSubProcess(new SimpleExternalProcess("ls -lsa"));
            p.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * Returns true if this BsubProcess is lauched with -I option false otherwise
     * @return boolean
     */
    public String isInteractive() {
        return interactive;
    }

    /**
     * Allows to launch this BsubProcess with -I (interactive option)
     * @param interactive true for -I option false otherwise
     */
    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    /**
     * Sets the number of processor requested when running the job
     * @param processor
     */
    public void setProcessorNumber(String processor) {
        checkStarted();
        if (processor != null) {
            this.processor = processor;
        }
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "lsf_" + targetProcess.getProcessId();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        return (new Integer(getProcessorNumber()).intValue());
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    /**
     * Returns the number of processor requested for the job
     * @return String
     */
    public String getProcessorNumber() {
        return processor;
    }

    public void setScriptLocation(String location) {
        checkStarted();
        if (location != null) {
            this.scriptLocation = location;
        }
    }

    public String getScriptLocation() {
        return scriptLocation;
    }

    public String getRes_requirement() {
        return res_requirement;
    }

    public void setRes_requirement(String res_requirement) {
        this.res_requirement = "-R " + res_requirement + " ";
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected String internalBuildCommand() {
        return buildEnvironmentCommand() + buildBSubCommand();
    }

    protected String buildBSubCommand() {
        StringBuilder bSubCommand = new StringBuilder();
        bSubCommand.append(command_path);
        if (interactive.equals("true")) {
            bSubCommand.append(" -I");
        }
        bSubCommand.append(" -n " + processor + " -q " + queueName + " ");
        if (hostList != null) {
            bSubCommand.append("-m " + hostList + " ");
        }
        if (jobname != null) {
            bSubCommand.append("-J " + jobname + " ");
        }
        if (getCompositionType() == GIVE_COMMAND_AS_PARAMETER) {
            bSubCommand.append(getRes_requirement() + scriptLocation + " " + getTargetProcess().getCommand());
        }

        //logger.info("bsub command is "+bSubCommand.toString());
        return bSubCommand.toString();
    }

    protected String buildBJobsCommand() {
        return DEFAULT_BJOBPATH + " " + jobID;
    }

    /**
     * parses a message in order to find the job id of the
     * launched job.
     * we assume here that the jobid is displayed following this
     * convention :
     *    Job <...>
     */
    protected int parseJobID(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug("parseJobID analyzing " + message);
        }
        String beginJobIDMarkup = "Job <";
        String endJobIDMarkup = ">";
        int n1 = message.indexOf(beginJobIDMarkup);
        if (n1 == -1) {
            return 0;
        }
        int n2 = message.indexOf(endJobIDMarkup, n1 + beginJobIDMarkup.length());
        if (n2 == -1) {
            return 0;
        }
        String id = message.substring(n1 + beginJobIDMarkup.length(), n2);
        if (logger.isDebugEnabled()) {
            logger.debug("!!!!!!!!!!!!!! JOBID = " + id);
        }
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * parses the hostname from a string. We assume that the line
     * looks like that :
     *    191009 user  status  queue     fromHost     targetHost        *eep 10000 Jan 25 13:33
     * Where targetHost is the hostname we are looking for.
     * status could be at least
     *     - PEND for pending (means targethost is undetermined
     *     - anything else (means targethost is known
     * @param message the string that may contains the hostname
     * @return null if the message did not contains any hostname,
     * an empty string if the message did contains the target host but
     * was undertermined because the job was still pending. Return the
     * hostname if it is found.
     */
    protected String parseHostname(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug("parseHostname analyzing " + message);
        }
        java.util.StringTokenizer st = new java.util.StringTokenizer(message);
        if (st.countTokens() < 6) {
            return null; // we expect at least 6 tokens
        }
        try {
            int currentJobID = Integer.parseInt(st.nextToken());
            if (currentJobID != jobID) {
                return null; // not the same id
            }
        } catch (NumberFormatException e) {
            return null;
        }
        st.nextToken(); // ignore user
        String status = st.nextToken();
        if (status.equals("PEND")) {
            return ""; // not running yet
        }
        st.nextToken(); // ignore queue
        st.nextToken(); // ignore fromHost
        String hostname = st.nextToken();
        if (logger.isDebugEnabled()) {
            logger.debug("!!!!!!!!!!!!!! hostname = " + hostname);
        }
        logger.info("token " + st.countTokens());
        return hostname;
    }

    protected void sendJobDetailsCommand() {
        outputMessageSink.setMessage(buildBJobsCommand());
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    //
    // -- INNER CLASSES -----------------------------------------------
    //

    /**
     * Implementation of a RemoteProcessMessageLogger that look for the jobID of the launched job
     */
    public class ParserMessageLogger implements RemoteProcessMessageLogger, java.io.Serializable {
        private boolean foundJobID;
        private boolean foundHostname;

        public ParserMessageLogger() {
        }

        public void log(String message) {
            //int nbProcessor = (new Integer(processor)).intValue();
            //parseHostname(message);
            if (!foundJobID) {
                jobID = parseJobID(message);
                foundJobID = jobID != 0;
                if (foundJobID) {
                    sendJobDetailsCommand();
                }
            } else if (!foundHostname) {
                hostname = parseHostname(message);
                if (hostname != null) {
                    //int counter=1;
                    foundHostname = hostname.length() > 0;
                    //while(counter < nbProcessor){
                    //parseHostname(message);
                    //counter ++;
                    //}
                    if (foundHostname) {
                        // we are done
                        outputMessageSink.setMessage(null);
                    } else {
                        // send another command to fetch the hostname
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                        sendJobDetailsCommand();
                    }
                }
            }
        }

        public void log(Throwable t) {
        }

        public void log(String message, Throwable t) {
        }
    } // end inner class CompositeMessageLogger

    public String getJobname() {
        return jobname;
    }

    public void setJobname(String jobname) {
        this.jobname = jobname;
    }
}
