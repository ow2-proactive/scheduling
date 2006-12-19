package org.objectweb.proactive.scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.nt.NTEventLogAppender;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This is the main class used to communicate with the scheduler daemon to submit the
 * commands to the scheduler like the submission, deletion and statistics of any job
 * and the nodes status command.
 * @author cjarjouh
 *
 */
public class Communicator {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER_COMMAND_LISTENER);

    /* Logging */
    private static final String LOG_DIR = "." + File.separator + "logs" +
        File.separator;
    private static final String MAX_SIZE = "100KB";
    private static final String LOG_PATTERN = "%d %c %x %m\n\n";
    private static WriterAppender writerAppender;
    private static String hostname = null;

    static {
        configureLogging();
    }

    /* These are the constants namely the commands that are used */
    private static final String LOG_HEADER = "[Scheduler] ";
    private static final String SUB_CMD = "sub";
    private static final String STAT_CMD = "stat";
    private static final String DEL_CMD = "del";
    private static final String NODES_CMD = "nodes";
    private static final String KILL_CMD = "kill";
    private static final String EXIT_CMD = "exit";
    private static final String SUB_PROTO = SUB_CMD + " XMLDescriptorOfTheJob";
    private static final String STAT_PROTO = STAT_CMD + " [jobId]";
    private static final String DEL_PROTO = DEL_CMD + " jobId";
    private static final String NODES_PROTO = NODES_CMD + " [nodeURL]";
    private static final String KILL_PROTO = KILL_CMD;
    private static final String EXIT_PROTO = EXIT_CMD;
    private Scheduler scheduler;

    /**
     * Tries to establish a connection with the scheduler daemon and to get the
     * scheduler object before we begin with the submission of commands
     * @param schedulerURL the scheduler url upon wich we shall connect to the scheduler
     */
    public Communicator(String schedulerURL) {
        try {
            this.scheduler = Scheduler.connectTo(schedulerURL);
            startCommandListener();
        } catch (Exception e) {
            flush("Cannot create command listener");
            System.exit(1);
        }
    }

    /**
     * Lanches the communicator program
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub	
        String schedulerURL = args[0];
        new Communicator(schedulerURL);
    }

    /**
     * This function is used to make the String right-justified. If the String is bigger
     * then pad_len then this function will add blanks in the beginning to make sure
     * that the String is right-justified over the pad_len space else it will return the
     * String unchanged.
     * @param s the string that we're trying to justify
     * @param pad_len the size in character numbers of the space
     * @return the justified String
     */
    private String pad(String s, int pad_len) {
        if (s.length() >= pad_len) {
            return (s);
        } else {
            int nblanks = pad_len - s.length();
            StringBuilder blanks = new StringBuilder(nblanks);

            blanks.setLength(nblanks);
            for (int k = 0; k < blanks.length(); ++k)
                blanks.setCharAt(k, ' ');
            return (blanks + s);
        }
    }

    /**
     * This function is used to make the String center-justified. If the String is smaller
     * then pad_len then this function will add blanks in the beginning and in the end
     * to make sure that the String is right-justified over the pad_len space else
     * it will return the String unchanged.
     * @param s the string that we're trying to justify
     * @param pad_len the size in character numbers of the space
     * @return the justified String
     */
    private String center(String s, int pad_len) {
        if (s.length() >= pad_len) {
            return (s);
        } else {
            int nblanks = pad_len - s.length();
            int half = nblanks / 2;
            StringBuilder blanks = new StringBuilder(half);

            blanks.setLength(half);
            for (int k = 0; k < blanks.length(); ++k)
                blanks.setCharAt(k, ' ');

            String end = "";
            if ((nblanks % 2) == 1) {
                end = " ";
            }

            return (blanks + s + blanks + end);
        }
    }

    /**
     * Logs the message either as a normal message or as an error depending on the
     * isError type.
     * @param msg the message to be added to the log file
     * @param isError if true then the message is an error message else it is a normal one.
     */
    private static void log(String msg, boolean isError) {
        msg = LOG_HEADER + msg;

        try {
            if (isError) {
                logger.error(msg);
            } else {
                logger.info(msg);
            }
        } catch (Exception e) {
            /* Log the logging exception ;-) */
            System.out.println(e.getMessage() + " when logging : " + msg);
        }
    }

    /**
     * Gets the localHostName
     * @return the local host name
     */
    public static String getLocalHostName() {
        if (hostname == null) {
            try {
                hostname = InetAddress.getLocalHost().getCanonicalHostName()
                                      .toLowerCase();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                hostname = "localhost";
            }
        }

        return hostname;
    }

    /**
     * Configures the logging parameters and launches the logger.
     *
     */
    private static void configureLogging() {
        Appender appender;

        try {
            appender = new NTEventLogAppender("ProActiveScheduler");
        } catch (java.lang.UnsatisfiedLinkError e) {
            String hostname = getLocalHostName();

            Layout layout = new PatternLayout(LOG_PATTERN);
            String filename = LOG_DIR + hostname;
            RollingFileAppender rfa;

            try {
                new File(LOG_DIR).mkdir();
                rfa = new RollingFileAppender(layout, filename, true);
            } catch (IOException ioe) {
                ioe.printStackTrace();

                return;
            }

            rfa.setMaxBackupIndex(0);
            rfa.setMaxFileSize(MAX_SIZE);
            rfa.setImmediateFlush(false);
            writerAppender = rfa;
            appender = rfa;
        }

        Logger root = Logger.getRootLogger();
        root.addAppender(appender);

        /* First message :) */
        log("Starting Scheduler command listener", false);
    }

    /**
     * sets an immediate flush of the normal message.
     * @param message is the message that we want to add.
     */
    private static void flush(String message) {
        if (writerAppender != null) {
            writerAppender.setImmediateFlush(true);
        }

        log(message, false);

        if (writerAppender != null) {
            writerAppender.setImmediateFlush(false);
        }
    }

    /**
     * Is the help console. Here we can either set specific help for a specific command
     * or we can add the command name of the newly created command
     * @param command the command in forms of "? [commandName]" meaning that we can either
     *                 ask for a short description of the commands or we can always a specific help
     *                 for a specific command.
     */
    public void helpScreen(String command) {
        String result = "";

        if (!command.endsWith(SUB_CMD) && !command.endsWith(STAT_CMD) &&
                !command.endsWith(DEL_CMD) && !command.endsWith(NODES_CMD) &&
                !command.endsWith(KILL_CMD) && !command.endsWith(EXIT_CMD) &&
                !command.equals("?")) {
            System.out.println("No such command: " + command.substring(1));
            log("No help available for " + command, true);

            return;
        }

        result = "\n";

        if (!command.equals("?")) {
            String keyword = command.substring(2);

            result += "This command's prototype is: ";

            if (keyword.equals(SUB_CMD)) {
                result += SUB_PROTO;
                result += "\n\n";
                result += "This command is used to submit a job to the scheduler.\n\n";
                result += ("XMLDescriptorOfTheJob is the absolute path to the " +
                "XML Deployment Descriptor of the job to be submitted\n");
            } else if (keyword.equals(STAT_CMD)) {
                result += STAT_PROTO;
                result += "\n\n";
                result += ("This command is used to get additionnal information on one " +
                "or all the jobs submitted.\n\n");
                result += ("jobId is the id of the job that we want to know its " +
                "caracteristics. This argument is optional and if not submitted " +
                "this means that we need to get the information on all the jobs\n");
            } else if (keyword.equals(DEL_CMD)) {
                result += DEL_PROTO;
                result += "\n\n";
                result += ("This command is used to delete a specific job. " +
                "If the job is already deployed this may cause a brutal interruption " +
                "of the job.\n\n");
                result += "jobId is the id of the job that we want to delete.\n";
            } else if (keyword.equals(NODES_CMD)) {
                result += NODES_PROTO;
                result += "\n\n";
                result += ("This command is used to get information of one or all " +
                "the nodes that have been submitted.\n\n");
                result += ("nodeURL is the URL of the node that we need to fetch it's " +
                "characteristics. This argument is optional and if not submitted " +
                "this means that we need to get the information on all the jobs\n");
            } else if (keyword.equals(KILL_CMD)) {
                result += KILL_PROTO;
                result += "\n\n";
                result += "This command is used kill the scheduler abruptly.\n";
            } else if (keyword.equals(EXIT_CMD)) {
                result += EXIT_PROTO;
                result += "\n\n";
                result += "This command is used to exit this session.\n";
            }

            result += "\n";
        } else {
            result += ("The commands available are: " + SUB_CMD + ", " +
            STAT_CMD + ", " + DEL_CMD + ", " + NODES_CMD + ", " + KILL_CMD +
            ", " + EXIT_CMD);
        }

        result += "\n";
        System.out.println(result);
    }

    /**
     * Here we shall handle the submitted command, check the validity of the command
     * then call the related method or subroutine to launch the command.
     * @param command is the command that we shall handle
     * @return true if the execution of the command occured normaly, flase otherwise.
     */
    private boolean handleCommand(String command) {
        if (command.equals(EXIT_CMD)) {
            System.exit(0);
        }
        if (command.equals("")) {
            return true;
        }

        if (command.startsWith("?")) {
            this.helpScreen(command);
            return true;
        }

        if (!command.startsWith(SUB_CMD) && !command.startsWith(STAT_CMD) &&
                !command.startsWith(DEL_CMD) && !command.startsWith(NODES_CMD) &&
                !command.startsWith(KILL_CMD)) {
            System.out.println("UNKNOWN COMMAND!!...");
            log("unknown command submitted: " + command, true);

            return false;
        }

        String error = null;

        if (command.startsWith(SUB_CMD)) {
            flush(command);

            if (command.equals(SUB_CMD)) {
                error = SUB_PROTO + "\n";
            } else {
                //                System.out.println("SUB command invoked ....");
                String XMLDescriptorFile = command.substring(command.indexOf(
                            ' ') + 1);
                this.scheduler.fetchJobDescription(XMLDescriptorFile);
            }
        }

        if (command.startsWith(STAT_CMD)) {
            flush(command);

            String jobId = null;
            int indexOfWhite = command.indexOf(" ");

            // if index = -1, then the command submitted is "nodes" without parameters
            // else we have to extract the node ID
            if (indexOfWhite != -1) {
                jobId = command.substring(indexOfWhite + 1);
            }

            Vector result = this.scheduler.stat(jobId);
            this.viewJobs(result, (jobId != null));

            //            System.out.println("STAT command invoked ....");
        }

        if (command.startsWith(DEL_CMD)) {
            flush(command);

            if (command.equals(DEL_CMD)) {
                error = DEL_PROTO + "\n";
            } else {
                System.out.println("DEL command invoked ....");
                String jobID = command.substring(command.indexOf(' ') + 1);
                this.scheduler.del(jobID);
            }

            //            this.scheduler.del(null);
        }

        if (command.startsWith(NODES_CMD)) {
            flush(command);

            String nodeURL = null;
            int indexOfWhite = command.indexOf(" ");

            // if index = -1, then the command submitted is "nodes" without parameters
            // else we have to extract the node ID
            if (indexOfWhite != -1) {
                nodeURL = command.substring(indexOfWhite + 1);
            }

            Vector result = this.scheduler.nodes(nodeURL);
            this.viewNodes(result, (nodeURL != null));

            //            System.out.println("NODES command invoked ....");
        }

        if (error != null) {
            System.out.println("Error using the command. Usage: " + error);
            log("Error using the command " +
                error.substring(0, error.indexOf(' ')), true);
            return false;
        }

        if (KILL_CMD.equals(command)) {
            flush(command);
            System.exit(0);
        }

        return true;
    }

    /**
     * Starts the command listener object and begins with to take the commands
     *
     */
    private void startCommandListener() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(isr);

        for (;;) {
            try {
                System.out.print(" > ");
                String line = reader.readLine();
                handleCommand(line);
            } catch (IOException ioe) {
                // ususally there are no error from the keyboard ... 
                ioe.printStackTrace();
            }
        }
    }

    /**
     * This method is used to display the descriptions of all the nodes or
     * of a specific node on the shell prompt.
     * @param nodes a vector of all the nodes or of the specified node
     * @param specific if true means that we are about to display the information
     *                 of a specific node else it means that we are displaying the information
     *                 of all the nodes.
     */
    public void viewNodes(Vector nodes, boolean specific) {
        Iterator iterator = nodes.iterator();
        int nbOfNodes = 0;
        int nbOfBusyNodes = 0;
        int nbOfReservedNodes = 0;
        String result = "";
        String header = this.center("Node URL", 45) + this.center("JobId", 74) +
            this.pad("status", 14) + "\n" +
            "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" +
            " - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - \n";

        result += header;

        while (iterator.hasNext()) {
            Node node = (Node) iterator.next();

            NodeInformation nodeInformation = node.getNodeInformation();
            String jobId = nodeInformation.getJobID();
            String nodeURL = nodeInformation.getURL();
            String status = "";
            nbOfNodes++;

            try {
                if (jobId.equals("-")) {
                    status = "free";
                } else {
                    int aoNb = node.getNumberOfActiveObjects();
                    if (aoNb == 0) {
                        status = "reserved";
                        nbOfReservedNodes++;
                    } else {
                        status = "busy";
                        nbOfBusyNodes++;
                    }
                }
            } catch (NodeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            result += (this.center(nodeURL, 45) + this.center(jobId, 74) +
            this.center(status, 22) + "\n");
        }

        String addition = "\nDisplaying all nodes information: ";
        if (!specific) {
            addition += (nbOfBusyNodes + "/" + nbOfNodes + " busy nodes and " +
            nbOfReservedNodes + "/" + nbOfNodes + " reserved nodes");
        }
        addition += "\n\n";
        result = addition + result;

        System.out.println(result);
    }

    /**
     * This method is used to display the descriptions of all the jobs or
     * of a specific job on the shell prompt.
     * @param Jobs a vector of all the jobs or of the specified job
     * @param specific if true means that we are about to display the information
     *                 of a specific job else it means that we are displaying the information
     *                 of all the jobs.
     */
    public void viewJobs(Vector jobs, boolean specific) {
        Iterator iterator = jobs.iterator();
        String result = "";
        String header = this.center("Job ID", 74) +
            this.center("submit date", 30) + this.center("estimated time", 18) +
            this.center("ressource number", 20) + this.center("status", 10) +
            "\n" +
            "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" +
            " - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - \n";

        result += header;

        while (iterator.hasNext()) {
            GenericJob jobDescription = (GenericJob) iterator.next();

            String jobId = jobDescription.getJobID();
            Date submitDate = jobDescription.getSubmitDate();
            int ressourceNb = jobDescription.getRessourceNb();
            int estimatedTime = jobDescription.getEstimatedTime();
            String jobStatus = jobDescription.getJobStatus();

            result += (this.center(jobId, 74) +
            this.center(String.valueOf(submitDate), 30) +
            this.center(String.valueOf(estimatedTime), 18) +
            this.center(String.valueOf(ressourceNb), 20) +
            this.center(jobStatus, 10) + "\n");
        }

        String addition = "\n\n";
        result = addition + result;

        System.out.println(result);
    }
}
