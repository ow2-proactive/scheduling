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
package org.objectweb.proactive.core.process.loadleveler;

import java.io.File;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;


public class LoadLevelerProcess extends AbstractExternalProcessDecorator {
    private static final String DEFAULT_OUTPUTFILE_NAME = "proactive-loadleveler";
    public final static String DEFAULT_LLPATH = File.separator + "usr" +
        File.separator + "lpp" + File.separator + "LoadL" + File.separator +
        "full" + File.separator + "bin";

    /**
     * LoadLeveler configuration file attributes names.
     */
    private static final String LLPREFIX = "#@";
    private static final String LLTAG_EXECUTABLE = "executable";
    private static final String LLTAG_INITIAL_DIR = "initialdir";
    private static final String LLTAG_ERROR_OUTPUT_FILENAME = "error";
    private static final String LLTAG_ERROR_ENVIRONMENT = "environment";
    private static final String LLTAG_STD_OUTPUT_FILENAME = "output";
    private static final String LLTAG_WALL_CLOCK_LIMIT = "wall_clock_limit";
    private static final String LLTAG_RESOURCES = "resources";
    private static final String LLTAG_EXEC_ARGS = "arguments";
    private static final String LLTAG_CONSUMABLE_CPUS = "ConsumableCpus";

    // Load leveler task repartition
    private static final String LLTAG_TASK_GEOMETRY = "task_geometry";
    private static final String LLTAG_NODE = "node";
    private static final String LLTAG_TASKS_PER_NODE = "tasks_per_node";
    private static final String LLTAG_BLOCKING = "blocking";
    private static final String LLTAG_TOTAL_TASKS = "total_tasks";

    /* The proactive cmd variable name used in the loadlLevelerStartRuntime.sh */
    private static final String PROACTIVE_CMD_TAG = "PROACTIVE_CMD";

    /**
     * These are the most frequently used LoadLeveler Options
     **/

    /* Mandatory Field : Executable name. Could either be an absolute or relative path if initialDir is set.
     * By default setted to /usr/bin/poe which is the responsible of starting parallel jobs
     */
    private String executable = "/usr/bin/poe";

    /* Mandatory Field : if executable path is not absolute. Is also the default directory
     * to output the generated job configuration file and the
     * std out/err if their given path is not absolute */
    private String initialDir = System.getProperty("java.io.tmpdir");

    /* Executable standard output */
    private String outputFile = null;

    /* Executable error output */
    private String errorFile = null;

    /* Arguments to be passed to the executable, by default
     * the path to the loadLevelerStartRuntime shell script
     */
    private String arguments = null;

    /* Environment variable to transmit to the load leveler job */
    private String taskEnvironment = null;

    // advanced task submission mode
    private int blocking = -1;
    private int node = -1;
    private int totalTasks = -1;
    private int tasksPerNode = -1;
    private String taskGeometry = null;

    // simple task submission mode
    private int nbTasks = -1;
    private int tasksPerHosts = -1;
    private int cpusPerTasks = -1;

    /** Setted in the deployment descriptor **/
    /* Resources needed to run the job */
    private String resources = null;

    /* Job's duration wall clock limit */
    private String wallClockLimit = null;
    private String jobSubmissionScriptPath;

    /* The job id returned by load leveler on successfull job submission */
    private String jobId = null;

    public LoadLevelerProcess() {
        super();
        this.setCompositionType(ExternalProcessDecorator.SEND_TO_OUTPUT_STREAM_COMPOSITION);
        this.command_path = DEFAULT_LLPATH;
        RemoteProcessMessageLogger logger = new LoadLevelerParserMessageLogger();
        setInputMessageLogger(logger);
        setErrorMessageLogger(logger);
        setOutputMessageSink(new SimpleMessageSink());
    }

    //
    // -- LOADLEVELER PROCESS CONFIG PART -----------------------------------------------
    //
    @Override
    protected String internalBuildCommand() {
        return buildEnvironmentCommand() + buildLoadLevelerCommand() + " ";
    }

    protected String buildLoadLevelerCommand() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.buildLoadLevelerSubmitCmd());
        builder.append(" '");
        builder.append(this.buildLoadLevelerParameters());
        builder.append("'");
        return builder.toString();
    }

    private String buildLoadLevelerSubmitCmd() {
        return this.getJobSubmissionScriptPath();
    }

    private String buildLoadLevelerParameters() {
        StringBuilder builder = new StringBuilder();

        if (this.getErrorFile() == null) {
            this.setErrorFile(DEFAULT_OUTPUTFILE_NAME + ".err");
        }
        if (this.getOutputFile() == null) {
            this.setOutputFile(DEFAULT_OUTPUTFILE_NAME + ".out");
        }

        builder.append(buildLoadLevelerComment(
                " LoadLeveler job description file generated by ProActive"));

        builder.append(buildLoadLevelerComment(" ---- Job Configuration ---- "));

        // usr/bin/poe by default
        builder.append(buildLoadLevelerProperty(LLTAG_EXECUTABLE,
                this.getExecutable()));

        if (this.getArguments() != null) {
            builder.append(buildLoadLevelerProperty(LLTAG_EXEC_ARGS,
                    this.getArguments()));
        }

        // the java.io.tmpdir by default
        if (this.getInitialDir() != null) {
            builder.append(buildLoadLevelerProperty(LLTAG_INITIAL_DIR,
                    this.getInitialDir()));
        }

        if (this.getOutputFile() != null) {
            builder.append(buildLoadLevelerProperty(LLTAG_STD_OUTPUT_FILENAME,
                    this.getOutputFile()));
        }

        if (this.getErrorFile() != null) {
            builder.append(buildLoadLevelerProperty(
                    LLTAG_ERROR_OUTPUT_FILENAME, this.getErrorFile()));
        }

        builder.append(buildLoadLevelerComment(" ---- Task Environment ---- "));

        // Reminder: the final process' command should not include the ';' character 
        // as it is used by load leveler to separate environment variables.
        String tmpTaskEnvironment = " $" + PROACTIVE_CMD_TAG + "=" +
            this.targetProcess.getCommand();

        if (this.getTaskEnvironment() != null) {
            // Prepending user define environement
            tmpTaskEnvironment = this.getTaskEnvironment() + " ; " +
                tmpTaskEnvironment;
        }
        builder.append(buildLoadLevelerProperty(LLTAG_ERROR_ENVIRONMENT,
                tmpTaskEnvironment));

        builder.append(buildLoadLevelerComment(" ---- Task Resources ---- "));

        if (this.getWallClockLimit() != null) {
            builder.append(buildLoadLevelerProperty(LLTAG_WALL_CLOCK_LIMIT,
                    this.getWallClockLimit()));
        }

        /* Dealing with the consumable cpu option which is automatically
             * added in the simple task repartition mode.
             */
        String resourcesString = null;

        if (this.getResources() != null) {
            resourcesString = buildLoadLevelerProperty(LLTAG_RESOURCES,
                    this.getResources());
        }

        if (this.isSimpleTaskRepartition()) {
            String consumableCpu = " " + LLTAG_CONSUMABLE_CPUS + "(" +
                this.cpusPerTasks + ")";
            if (resourcesString == null) {
                resourcesString = buildLoadLevelerProperty(LLTAG_RESOURCES,
                        consumableCpu);
            } else {
                resourcesString += consumableCpu;
            }
        }

        if (resourcesString != null) {
            builder.append(resourcesString);
        }

        builder.append(buildLoadLevelerComment(" ---- Task Repartion ---- "));

        if (this.isSimpleTaskRepartition()) {
            builder.append(buildLoadLevelerProperty(LLTAG_TASK_GEOMETRY,
                    this.buildSimpleTaskAsTaskGeometry(this.nbTasks,
                        this.tasksPerHosts)));
        } else {

            /* Only subsets of these keywords are valid to combine,
                 * the schema should have filtered them, check loadleveler documentation */
            if (this.getBlocking() != -1) {
                builder.append(buildLoadLevelerProperty(LLTAG_BLOCKING,
                        "" + this.getBlocking()));
            }

            if (this.getTotalTasks() != -1) {
                builder.append(buildLoadLevelerProperty(LLTAG_TOTAL_TASKS,
                        "" + this.getTotalTasks()));
            }

            if (this.getTasksPerNode() != -1) {
                builder.append(buildLoadLevelerProperty(LLTAG_TASKS_PER_NODE,
                        "" + this.getTasksPerNode()));
            }

            if (this.getNode() != -1) {
                builder.append(buildLoadLevelerProperty(LLTAG_NODE,
                        "" + this.getNode()));
            }

            if (this.getTaskGeometry() != null) {
                builder.append(buildLoadLevelerProperty(LLTAG_TASK_GEOMETRY,
                        this.getTaskGeometry()));
            }
        }

        builder.append(LLPREFIX + "queue");

        return builder.toString();
    }

    private String buildSimpleTaskAsTaskGeometry(int nbTasks, int tasksPerHosts) {
        String res = "";
        int i = tasksPerHosts;
        int task_cpt = 0;

        while (nbTasks > 0) {
            res += ("(" + task_cpt++);
            while (i > 1) {
                res += ("," + task_cpt);
                task_cpt++;
                i--;
            }
            res += ")";
            i = tasksPerHosts;
            nbTasks--;
        }

        return "{" + res + "}";
    }

    protected String buildLoadLevelerProperty(String tag, String value) {

        /* LoadLeveler targets AIX thus \n as newline char is ok.
         * We need an additionnal \ so that java system exec does
         * not remove it while parsing args */
        return LLPREFIX + tag + " = " + value + "\\n";
    }

    protected String buildLoadLevelerComment(String comment) {

        /* LoadLeveler targets AIX thus \n as newline char is ok.
         * We need an additionnal \ so that java system exec does
         * not remove it while parsing args */
        return "#" + comment + "\\n";
    }

    /**
    * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
    */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        // Returns the jvm declaration associated to this load leveler process
        return targetProcess.getFinalProcess();
    }

    /**
     * This function role is somehow unclear.
     * Its name suggest to return the number of node to be created by this process.
     * However if you check @see {@link VirtualNodeImpl} you will see that the
     * method getNbMappedNodes() multiply this number by the
     * expected number of node to be created by the jvm.
     *
     * @return Thus it returns the number of runtimes to be created by the load leveler process.
     */
    public int getNodeNumber() {
        if (this.isSimpleTaskRepartition()) {
            return this.nbTasks * this.tasksPerHosts;
        } else {
            if (this.isAdvancedBlockingMode()) {
                return this.totalTasks;
            } else if (this.isAdvancedTasksPerNodeMode()) {
                return this.tasksPerNode * this.node;
            } else if (this.isAdvancedTotalTasksMode()) {
                return this.totalTasks;
            } else if (this.isAdvancedTaskGeometryMode()) {
                return this.getNodeNumberFromTaskGeometry(this.taskGeometry);
            }
        }

        return -1;
    }

    private int getNodeNumberFromTaskGeometry(String taskGeometry2) {
        StringTokenizer st = new StringTokenizer(taskGeometry2, ",)");
        int i = -1;
        while (st.hasMoreTokens()) {
            i++;
            st.nextToken();
        }

        return i;
    }

    public String getTaskEnvironment() {
        return this.taskEnvironment;
    }

    public void setTaskEnvironment(String taskEnvironment) {
        this.taskEnvironment = taskEnvironment;
    }

    public boolean isAdvancedBlockingMode() {
        return ((this.blocking != -1) && (this.totalTasks != -1));
    }

    public boolean isAdvancedTasksPerNodeMode() {
        return ((this.tasksPerNode != -1) && (this.node != -1));
    }

    public boolean isAdvancedTotalTasksMode() {
        return ((this.totalTasks != -1) && (this.node != -1));
    }

    public boolean isAdvancedTaskGeometryMode() {
        return (this.taskGeometry != null);
    }

    public String getProcessId() {
        // Vv don't know exactly why we need this
        return "LL_" + targetProcess.getProcessId();
    }

    //
    // -- LOADLEVELER PROCESS LAUNCH PART -----------------------------------------------
    //
    protected boolean isSimpleTaskRepartition() {
        // if simple task repartition is used then the schema forces definition in [0..N]
        return ((this.getNbTasks() != -1) && (this.getTasksPerHosts() != -1) &&
        (this.getCpusPerTasks() != -1));
    }

    protected boolean isAdvancedTaskRepartition() {
        return !isSimpleTaskRepartition();
    }

    //
    // -- GETTERS AND SETTERS -----------------------------------------------
    //
    protected void setJobId(String id) {
        System.out.println("Job Id extracted " + id); //Vv debug to remove 
        this.jobId = id;
    }

    protected String getJobId() {
        return this.jobId;
    }

    public String getErrorFile() {
        return errorFile;
    }

    public void setErrorFile(String errorFile) {
        this.errorFile = errorFile;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getInitialDir() {
        return initialDir;
    }

    public void setInitialDir(String initialDir) {
        this.initialDir = initialDir;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public String getWallClockLimit() {
        return wallClockLimit;
    }

    public void setWallClockLimit(String wallClockLimit) {
        this.wallClockLimit = wallClockLimit;
    }

    public void setNbTasks(String nbTasks) {
        this.nbTasks = Integer.parseInt(nbTasks);
    }

    public void setCpusPerTasks(String cpusPerTasks) {
        this.cpusPerTasks = Integer.parseInt(cpusPerTasks);
    }

    public void setTasksPerHosts(String tasksPerHosts) {
        this.tasksPerHosts = Integer.parseInt(tasksPerHosts);
    }

    public void setBlocking(String nodeValue) {
        this.blocking = Integer.parseInt(nodeValue);
    }

    public void setNode(String nodeValue) {
        this.node = Integer.parseInt(nodeValue);
    }

    public void setTasksPerNode(String nodeValue) {
        this.tasksPerNode = Integer.parseInt(nodeValue);
    }

    public void setTaskGeometry(String nodeValue) {
        this.taskGeometry = nodeValue;
    }

    public void setTotalTasks(String nodeValue) {
        this.totalTasks = Integer.parseInt(nodeValue);
    }

    public void setJobSubmissionScriptPath(String path) {
        this.jobSubmissionScriptPath = path;
    }

    public String getJobSubmissionScriptPath() {
        return this.jobSubmissionScriptPath;
    }

    public int getBlocking() {
        return blocking;
    }

    public int getCpusPerTasks() {
        return cpusPerTasks;
    }

    public int getNbTasks() {
        return nbTasks;
    }

    public int getNode() {
        return node;
    }

    public String getTaskGeometry() {
        return taskGeometry;
    }

    public int getTasksPerNode() {
        return tasksPerNode;
    }

    public int getTasksPerHosts() {
        return tasksPerHosts;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //

    /**
     * Implementation of a RemoteProcessMessageLogger that look for the jobID of the launched job
     */
    private class LoadLevelerParserMessageLogger
        implements RemoteProcessMessageLogger, java.io.Serializable {
        public LoadLevelerParserMessageLogger() {
        }

        public void log(String message) {
            System.out.println("LoadLevelerParser received -> " + message);

            if (LoadLevelerProcess.this.getJobId() == null) {
                this.extractJobId(message);
            }
        }

        public void log(Throwable t) {
            //Vv to implements
        }

        public void log(String message, Throwable t) {
            //Vv to implements
        }

        /**
         * Extracts the load leveler job id from an output str
         * Subclass if your load leveler has a different output.
         * @param message
         */
        protected void extractJobId(String message) {

            /* Output pattern on successful submission:
                 * llsubmit: The job "mesocentre.2172" has been submitted.
                 */
            Pattern p = Pattern.compile(".*\".+\".*has been submitted.*");
            Matcher m = p.matcher(message);

            if (m.matches()) {
                LoadLevelerProcess.this.setJobId(message.substring(message.indexOf(
                            "\"") + 1, message.lastIndexOf("\"")));
            }

            /* Output pattern on error:
                 * llsubmit: This job has not been submitted to LoadLeveler.
                 */
            p = Pattern.compile(".*has not been submitted.*");
            m = p.matcher(message);
            if (m.matches()) {
                // Process start failed
                //Vv what do we want to do ?
                System.out.println("DEBUG Submission failed ");
            }
        }
    } // end inner class LoadLevelerParserMessageLogger
}
