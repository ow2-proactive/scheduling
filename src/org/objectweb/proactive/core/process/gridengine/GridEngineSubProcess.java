package org.objectweb.proactive.core.process.gridengine;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.MessageSink;
import org.objectweb.proactive.core.util.MessageLogger;


/**
 * @author fhuet
 */
public class GridEngineSubProcess extends AbstractExternalProcessDecorator {
    private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");
    public final static String DEFAULT_QSUBPATH = "/opt/gridengine/bin/glinux/qsub";
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty(
            "user.home") + FILE_SEPARATOR + "workProActive/ProActive" +
        FILE_SEPARATOR + "scripts" + FILE_SEPARATOR + "unix" + FILE_SEPARATOR +
        "cluster" + FILE_SEPARATOR + "gridEngineStartRuntime.sh ";
    protected static final String DEFAULT_HOSTS_NUMBER = "1";
    protected static final String DEFAULT_PARALLEL_ENVIRONMENT = "make";
    protected static final String DEFAULT_BOOKING_DURATION = "00:01:00";
    protected String hosts = DEFAULT_HOSTS_NUMBER;
    protected String parallelEnvironment = DEFAULT_PARALLEL_ENVIRONMENT;
    protected String bookingDuration = DEFAULT_BOOKING_DURATION;
    protected String interactive = "false";
    protected String outputFile;
    protected int jobID;
    protected String queueName;
    protected String hostList;
    protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;

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

    public void setParallelEnvironment(String p) {
        this.parallelEnvironment = p;
    }

    public String getParallelEnvironment() {
        return this.parallelEnvironment;
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

    protected String buildCommand() {
        StringBuffer qsubCommand = new StringBuffer();
        qsubCommand.append(command_path).append(" -S /bin/bash  ");

        qsubCommand.append("-pe ").append(parallelEnvironment).append(" ");
        qsubCommand.append(hosts).append(" ");
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
        rs.append("nodes=").append(hosts).append(":");

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
