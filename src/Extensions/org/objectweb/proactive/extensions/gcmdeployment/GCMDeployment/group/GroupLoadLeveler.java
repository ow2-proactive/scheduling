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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.gcmdeployment.Helpers;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;


public class GroupLoadLeveler extends AbstractGroup {

    private static final String LLTAG_QUEUE = "queue";
    private static final String DEFAULT_OUTPUTFILE_NAME = "proactive-loadleveler";
    public final static String DEFAULT_LLPATH = File.separator + "usr" + File.separator + "lpp" +
        File.separator + "LoadL" + File.separator + "full" + File.separator + "bin";

    //LoadLeveler configuration file attributes names.
    private static final String LLPREFIX = "#@";
    private static final String LLTAG_EXECUTABLE = "executable";
    private static final String LLTAG_INITIAL_DIR = "initialdir";
    private static final String LLTAG_ERROR_OUTPUT_FILENAME = "error";
    private static final String LLTAG_ENVIRONMENT = "environment";
    private static final String LLTAG_STD_OUTPUT_FILENAME = "output";
    private static final String LLTAG_WALL_CLOCK_LIMIT = "wall_clock_limit";
    private static final String LLTAG_RESOURCES = "resources";
    private static final String LLTAG_EXEC_ARGS = "arguments";

    // Load leveler task repartition
    private static final String LLTAG_TASK_GEOMETRY = "task_geometry";
    private static final String LLTAG_NODE = "node";
    private static final String LLTAG_TASKS_PER_NODE = "tasks_per_node";
    private static final String LLTAG_BLOCKING = "blocking";
    private static final String LLTAG_TOTAL_TASKS = "total_tasks";

    private static final String CMD_TAG = "CMD";

    /** Mandatory Field : Executable name. Could either be an absolute or relative path if directory is set.
     * By default set to /usr/bin/poe which is the responsible of starting parallel jobs
     */
    private PathElement executable = new PathElement("/usr/bin/poe");
    private String executable_as_str;

    /** Mandatory Field : if executable path is not absolute. It is also the default directory
     * to output the generated job configuration file and the
     * std out/err if their given path is not absolute */
    private String directory = System.getProperty("java.io.tmpdir");

    /** Executable standard output */
    private String stdout = null;

    /** Executable error output */
    private String stderr = null;

    /** Environment variable to transmit to the load leveler job */
    private String taskEnvironment = null;

    /** Resources needed to run the job */
    private String resources = null;

    /** Job's duration wall clock limit */
    private String maxTime = null;

    /** Arguments to be passed to the executable */
    private List<String> argumentList = null;

    /**
     * 'poe' takes as an argument the program to execute in parallel.
     * The program is specified as an argument. In our case it's a shell script
     * that will execute the gcm application. In order to make the gcm application
     * command line available to the script we set an environment variable. 
     */
    private String gcm_appli_command;

    // task submission mode
    private int blocking = -1;
    private int node = -1;
    private int totalTasks = -1;
    private int tasksPerNode = -1;
    private String taskGeometry = null;

    @Override
    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        final StringBuilder command = new StringBuilder();

        this.gcm_appli_command = Helpers.escapeCommand(commandBuilder.buildCommand(hostInfo, gcma));
        this.executable_as_str = this.executable.getFullPath(getHostInfo(), commandBuilder);

        command.append("echo -e ");
        command.append("\"");
        command.append(this.buildLoadLevelerJobDescription());
        command.append("\" | llsubmit -");

        final List<String> ret = new ArrayList<String>();
        ret.add(command.toString());

        return ret;
    }

    @Override
    public List<String> internalBuildCommands() {
        return null;
    }

    /*
     * Build command line utils
     */
    private String buildLoadLevelerJobDescription() {
        final StringBuilder builder = new StringBuilder();

        buildLoadLevelerJobConfiguration(builder);

        buildLoadLevelerTaskEnvironment(builder);

        buildLoadLevelerResources(builder);

        buildLoadLevelerTaskRepartition(builder);

        builder.append(LLPREFIX + LLTAG_QUEUE);

        return builder.toString();
    }

    private void buildLoadLevelerTaskRepartition(StringBuilder builder) {
        builder.append(buildLoadLevelerComment(" ---- Task Repartion ---- "));

        /* Only subsets of these keywords are valid to combine,
         * the schema should have filtered them, check load leveler documentation */
        if (this.blocking != -1) {
            builder.append(buildLoadLevelerProperty(LLTAG_BLOCKING, "" + this.blocking));
        }

        if (this.totalTasks != -1) {
            builder.append(buildLoadLevelerProperty(LLTAG_TOTAL_TASKS, "" + this.totalTasks));
        }

        if (this.tasksPerNode != -1) {
            builder.append(buildLoadLevelerProperty(LLTAG_TASKS_PER_NODE, "" + this.tasksPerNode));
        }

        if (this.node != -1) {
            builder.append(buildLoadLevelerProperty(LLTAG_NODE, "" + this.node));
        }

        if (this.taskGeometry != null) {
            builder.append(buildLoadLevelerProperty(LLTAG_TASK_GEOMETRY, this.taskGeometry));
        }

    }

    private void buildLoadLevelerResources(StringBuilder builder) {
        builder.append(buildLoadLevelerComment(" ---- Task Resources ---- "));

        if (this.maxTime != null) {
            builder.append(buildLoadLevelerProperty(LLTAG_WALL_CLOCK_LIMIT, this.maxTime));
        }

        /* Dealing with the consumable cpu option which is automatically
         * added in the simple task repartition mode.
         */
        String resourcesString = null;

        if (this.resources != null) {
            resourcesString = buildLoadLevelerProperty(LLTAG_RESOURCES, this.resources);
        }

        if (resourcesString != null) {
            builder.append(resourcesString);
        }
    }

    private void buildLoadLevelerTaskEnvironment(StringBuilder builder) {
        builder.append(buildLoadLevelerComment(" ---- Task Environment ---- "));

        // Reminder: the final process' command should not include the ';' character 
        // as it is used by load leveler to separate environment variables.
        String tmpTaskEnvironment = " " + CMD_TAG + "=" + this.gcm_appli_command;

        if (this.taskEnvironment != null) {
            // Prepending user defined environment
            tmpTaskEnvironment = this.taskEnvironment + " ; " + tmpTaskEnvironment;
        }
        builder.append(buildLoadLevelerProperty(LLTAG_ENVIRONMENT, tmpTaskEnvironment));
    }

    private void buildLoadLevelerJobConfiguration(StringBuilder builder) {
        if (stderr == null) {
            stderr = DEFAULT_OUTPUTFILE_NAME + ".err";
        }
        if (stdout == null) {
            stdout = DEFAULT_OUTPUTFILE_NAME + ".out";
        }

        builder
                .append(buildLoadLevelerComment(" LoadLeveler job description file generated by GCM deployment"));

        builder.append(buildLoadLevelerComment(" ---- Job Configuration ---- "));

        builder.append(buildLoadLevelerProperty(LLTAG_EXECUTABLE, executable_as_str));

        if (argumentList != null) {
            builder.append(buildLoadLevelerProperty(LLTAG_EXEC_ARGS, argumentList));
        }

        // the java.io.tmpdir by default
        if (directory != null) {
            builder.append(buildLoadLevelerProperty(LLTAG_INITIAL_DIR, directory));
        }

        if (stdout != null) {
            builder.append(buildLoadLevelerProperty(LLTAG_STD_OUTPUT_FILENAME, stdout));
        }

        if (stderr != null) {
            builder.append(buildLoadLevelerProperty(LLTAG_ERROR_OUTPUT_FILENAME, stderr));
        }
    }

    private String buildLoadLevelerProperty(String tag, List<String> value) {
        StringBuilder builder = new StringBuilder(LLPREFIX + tag + " = ");

        for (String string : value) {
            builder.append(string).append(" ");
        }

        builder.append("\\n");
        return builder.toString();
    }

    private String buildLoadLevelerProperty(String tag, String value) {

        /* LoadLeveler targets AIX thus \n as newline char is ok.
         * We need an additional \ so that java system exec does
         * not remove it while parsing args */
        return LLPREFIX + tag + " = " + value + "\\n";
    }

    private String buildLoadLevelerComment(String comment) {

        /* LoadLeveler targets AIX thus \n as newline char is ok.
         * We need an additional \ so that java system exec does
         * not remove it while parsing args */
        return "#" + comment + "\\n";
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

    /*
     * Setters and getters
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public void setMaxTime(String maxTime) {
        this.maxTime = maxTime;
    }

    public void setArgumentList(List<String> argumentList) {
        this.argumentList = argumentList;
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

    @Override
    public void setScriptPath(PathElement scriptPath) {
        this.executable = scriptPath;
        super.setScriptPath(scriptPath);
    }
}
