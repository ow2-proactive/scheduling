package org.objectweb.proactive.core.process.pbs;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.MessageSink;
import org.objectweb.proactive.core.util.MessageLogger;

import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * @author fhuet
 */
public class PBSSubProcess extends AbstractExternalProcessDecorator {
    private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");
    public final static String DEFAULT_QSUBPATH = "/usr/local/bin/qsub";
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty(
            "user.home") + FILE_SEPARATOR + "workProActive/ProActive" +
        FILE_SEPARATOR + "scripts" + FILE_SEPARATOR + "unix" + FILE_SEPARATOR +
        "cluster" + FILE_SEPARATOR + "pbsStartRuntime.sh ";
    protected static final String DEFAULT_HOSTS_NUMBER = "1";
    protected static final String DEFAULT_PROCESSOR_NUMBER = "1";
    protected static final String DEFAULT_BOOKING_DURATION = "00:01:00";
    protected String hosts = DEFAULT_HOSTS_NUMBER;
    protected String processorPerNode = DEFAULT_PROCESSOR_NUMBER;
    protected String bookingDuration = DEFAULT_BOOKING_DURATION;
    protected String interactive = "false";
    protected String outputFile;
    protected int jobID;
    protected String queueName;
    protected String hostList;
    protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;

    public PBSSubProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.hostname = null;
    }

    public PBSSubProcess(ExternalProcess targetProcess) {
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

    protected void internalStartProcess(String commandToExecute) {
    	//java does not seem to be able to deal with command
    	//where there are quotation marks
    	//thus we have to divide our command into tokens 
    	//and pass them to the runtime
        StringTokenizer st = new StringTokenizer(commandToExecute, " \"", true);
        String token;
        ArrayList al = new ArrayList();
        int quotationFound = 0;
        boolean commandFound = false;
        StringBuffer buff = new StringBuffer();
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

    protected String buildCommand() {
        StringBuffer qsubCommand = new StringBuffer();
        qsubCommand.append(DEFAULT_QSUBPATH).append(" ");
        if (interactive.equals("true")) {
            qsubCommand.append(" -I");
        }

        qsubCommand.append(buildResourceString());
        if (outputFile != null) {
            qsubCommand.append(" -o ").append(outputFile).append(" ");
        }
        qsubCommand.append(" -v ").append("PROACTIVE_COMMAND=\"")
                   .append(targetProcess.getCommand()).append("\"");
        qsubCommand.append(scriptLocation);

        //	        
        if (queueName != null) {
            qsubCommand.append(" -q " + queueName + " ");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("qsub command is " + qsubCommand.toString());
        }
        return qsubCommand.toString();
    }

    protected StringBuffer buildResourceString() {
        StringBuffer rs = new StringBuffer();
        rs.append(" -l walltime=").append(bookingDuration).append(",");
        //to specify nodes and processor per nodes, the syntax is different from
        //other resources
        rs.append("nodes=").append(hosts).append(":");
        rs.append("ppn=").append(processorPerNode);
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
            //System.out.println(" >>> log :" +message);
            int nbProcessor = (new Integer(hosts)).intValue();
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
