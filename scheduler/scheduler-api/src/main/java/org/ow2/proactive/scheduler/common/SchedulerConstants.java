/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Constant types in the Scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 *
 */
@PublicAPI
public class SchedulerConstants {

    /** Default scheduler node name */
    public static final String SCHEDULER_DEFAULT_NAME = "SCHEDULER";

    /** Default name for store active object binding **/
    public static final String SYNCHRONIZATION_DEFAULT_NAME = "SYNCHRONIZATION";

    /** Default job name */
    public static final String JOB_DEFAULT_NAME = "NOT SET";

    /** Default task name */
    public static final String TASK_DEFAULT_NAME = "NOT SET";

    /** If the task name is not set, this is the generated one */
    public static final String TASK_NAME_IFNOTSET = "task_";

    /** Name of the environment variable for windows home directory on the common file system. */
    public static final String WINDOWS_HOME_ENV_VAR = "WINDOWS_HOME";

    /** Name of the environment variable for unix home directory on the common file system. */
    public static final String UNIX_HOME_ENV_VAR = "UNIX_HOME";

    /** Name of the GlobalSpace for DataSpaces registration */
    public static final String GLOBALSPACE_NAME = "GLOBALSPACE";

    /** Name of the UserSpace for DataSpaces registration */
    public static final String USERSPACE_NAME = "USERSPACE";

    /** Default taskid directory name (used in TaskLauncher) */
    public static final String TASKID_DIR_DEFAULT_NAME = "TASKID";

    /** in a multi-node execution, a list of additional nodes reserved for the task, but not containing "exclusive" nodes i.e. single-host-exclusive topology */
    public static final String MULTI_NODE_TASK_NODESURL_BINDING_NAME = "nodesurl";

    /** in a multi-node execution, a set of all nodes reserved for the task, including the node running the task, additional nodes and also exclusive nodes reserved by the single-host-exclusive topology */
    public static final String MULTI_NODE_TASK_ALL_NODESURL_BINDING_NAME = "allnodesurl";

    public static final String VARIABLES_BINDING_NAME = "variables";

    public static final String GENERIC_INFO_BINDING_NAME = "genericInformation";

    public static final String RESULT_MAP_BINDING_NAME = "resultMap";

    public static final String DS_SCRATCH_BINDING_NAME = "localspace";

    public static final String DS_CACHE_BINDING_NAME = "cachespace";

    public static final String DS_INPUT_BINDING_NAME = "inputspace";

    public static final String DS_OUTPUT_BINDING_NAME = "outputspace";

    public static final String DS_GLOBAL_BINDING_NAME = "globalspace";

    public static final String DS_USER_BINDING_NAME = "userspace";

    public static final String DS_GLOBAL_API_BINDING_NAME = "globalspaceapi";

    public static final String DS_USER_API_BINDING_NAME = "userspaceapi";

    public static final String FORK_ENVIRONMENT_BINDING_NAME = "forkEnvironment";

    /**
     * The variable containing a proxy to the scheduler server
     */
    public static final String SCHEDULER_CLIENT_BINDING_NAME = "schedulerapi";

    public static final String RM_CLIENT_BINDING_NAME = "rmapi";

    /**
     * The variable containing a proxy to the key/value store
     */
    public static final String SYNCHRONIZATION_API_BINDING_NAME = "synchronizationapi";

    /**
     * The variable containing a proxy to the signal API
     */
    public static final String SIGNAL_API_BINDING_NAME = "signalapi";

    /**
     * Marker in the task output to locate the remote connection hint
     *  
     * for the hint to be detected client side, the following String has 
     * to be printed to the task log :
     * 
     *  PA_REMOTE_CONNECTION;TaskId;type;url
     *  
     *  example : 'PA_REMOTE_CONNECTION;10005;vnc;localhost:5901'
     */
    public static final String REMOTE_CONNECTION_MARKER = "PA_REMOTE_CONNECTION";

    /** Separator character for the String located by the {@link #REMOTE_CONNECTION_MARKER} */
    public static final char REMOTE_CONNECTION_SEPARATOR = ';';

    /** Attribute name in task the generic information indicating that the task requires a node protected by token */
    public static final String NODE_ACCESS_TOKEN = "NODE_ACCESS_TOKEN";

    /** Attribute name in task the generic information indicating that the task requires a node belonging to a specific node source */
    public static final String NODE_SOURCE_GENERIC_INFO = "NODE_SOURCE";

    /** Generic information containing the parent job id */
    public static final String PARENT_JOB_ID = "PARENT_JOB_ID";

    /** Generic information used by the job planner as the next execution schedule */
    public static final String NEXT_EXECUTION = "next.execution";

    /** Generic information containing the bucket name */
    public static final String BUCKET_NAME = "bucketName";

    /**
     * The variable name to access results from dependent tasks (an array).
     */
    public static final String RESULTS_VARIABLE = "results";

    /**
     * The variable name to access result metadata from dependent tasks (a map).
     */
    public static final String RESULT_METADATA_VARIABLE = "resultMetadata";

    /**
     * Specific metadata values
     */
    public static final String METADATA_CONTENT_TYPE = "content.type";

    public static final String METADATA_FILE_NAME = "file.name";

    public static final String METADATA_FILE_EXTENSION = "file.extension";

    /**
     * The variable name to access the user's third party credentials.
     */
    public static final String CREDENTIALS_VARIABLE = "credentials";

    /**
     * The variable used to get or set the task progress
     */
    public static final String PROGRESS_BINDING_NAME = "progress";

    /**
     * The Application ID used by the scheduler for local Dataspaces
     */
    public static String SCHEDULER_DATASPACE_APPLICATION_ID = "0";

    /**
     * This generic information can be used to disable Process Tree Killer execution
     */
    public static String DISABLE_PROCESS_TREE_KILLER_GENERIC_INFO = "DISABLE_PTK";

    /**
     * This generic information can be used to configure a task walltime
     */
    public static String TASK_WALLTIME_GENERIC_INFO = "WALLTIME";

    /**
     * Submission mode for workflows submitted from scheduler api
     */
    public static String SUBMISSION_MODE_WORKFLOW_API = "workflow-api";

    /**
     * Submission mode for workflows submitted from cli
     */
    public static String SUBMISSION_MODE_CLI = "cli";

    /**
     * Default submission mode
     */
    public static String SUBMISSION_MODE_REST_API = "rest-api";

    /**
     * This generic information can be used to set the portal name via the job was submitted
     */
    public static String SUBMISSION_MODE = "submission.mode";

}
