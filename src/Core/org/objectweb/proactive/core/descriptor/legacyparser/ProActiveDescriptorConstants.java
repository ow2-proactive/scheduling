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
package org.objectweb.proactive.core.descriptor.legacyparser;

/**
 * Defines many constants useful across ProActive
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface ProActiveDescriptorConstants {
    public static final String PROACTIVE_DESCRIPTOR_TAG = "ProActiveDescriptor";
    public static final String MAIN_DEFINITION_TAG = "mainDefinition";
    public static final String VARIABLES_TAG = "variables";
    public static final String VARIABLES_DESCRIPTOR_TAG = "descriptorVariable";
    public static final String VARIABLES_PROGRAM_TAG = "programVariable";
    public static final String VARIABLES_DESCRIPTOR_DEFAULT_TAG = "descriptorDefaultVariable";
    public static final String VARIABLES_PROGRAM_DEFAULT_TAG = "programDefaultVariable";
    public static final String VARIABLES_JAVAPROPERTY_TAG = "javaPropertyVariable";
    public static final String VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG = "javaPropertyDescriptorDefault";
    public static final String VARIABLES_JAVAPROPERTY_PROGRAM_TAG = "javaPropertyProgramDefault";
    public static final String VARIABLES_INCLUDE_XML_FILE_TAG = "includeXMLFile";
    public static final String VARIABLES_INCLUDE_PROPERTY_FILE_TAG = "includePropertyFile";
    public static final String DEPLOYMENT_TAG = "deployment";
    public static final String INFRASTRUCTURE_TAG = "infrastructure";
    public static final String COMPONENT_DEFINITION_TAG = "componentDefinition";
    public static final String ARG_TAG = "arg";
    public static final String MAP_TO_VIRTUAL_NODE_TAG = "mapToVirtualNode";
    public static final String VIRTUAL_NODES_DEFINITION_TAG = "virtualNodesDefinition";
    public static final String VIRTUAL_NODES_ACQUISITION_TAG = "virtualNodesAcquisition";
    public static final String VIRTUAL_NODE_TAG = "virtualNode";
    public static final String REGISTER_TAG = "register";
    public static final String MAPPING_TAG = "mapping";
    public static final String MAP_TAG = "map";
    public static final String JVMSET_TAG = "jvmSet";
    public static final String VMNAME_TAG = "vmName";
    public static final String CURRENTJVM_TAG = "currentJVM";
    public static final String LOOKUP_TAG = "lookup";
    public static final String JVMS_TAG = "jvms";
    public static final String JVM_TAG = "jvm";
    public static final String ACQUISITION_TAG = "acquisition";
    public static final String CREATION_PROCESS_TAG = "creation";
    public static final String PROCESS_TAG = "process";
    public static final String PROCESS_DEFINITION_TAG = "processDefinition";
    public static final String SERVICE_DEFINITION_TAG = "serviceDefinition";
    public static final String JVM_PROCESS_TAG = "jvmProcess";
    public static final String RSH_PROCESS_TAG = "rshProcess";
    public static final String PROCESS_LIST_TAG = "processList";
    public static final String PROCESS_LIST_BYHOST_TAG = "processListbyHost";
    public static final String MAPRSH_PROCESS_TAG = "maprshProcess";
    public static final String SSH_PROCESS_TAG = "sshProcess";
    public static final String RLOGIN_PROCESS_TAG = "rloginProcess";
    public static final String BSUB_PROCESS_TAG = "bsubProcess";
    public static final String LOADLEVELER_PROCESS_TAG = "loadLevelerProcess";
    public static final String GLOBUS_PROCESS_TAG = "globusProcess";
    public static final String PRUN_PROCESS_TAG = "prunProcess";
    public static final String PBS_PROCESS_TAG = "pbsProcess";
    public static final String OAR_PROCESS_TAG = "oarProcess";
    public static final String CLUSTERFORK_PROCESS_TAG = "clusterForkProcess";
    public static final String GLITE_PROCESS_TAG = "gLiteProcess";
    public static final String DEPENDENT_PROCESS_SEQUENCE_TAG = "dependentProcessSequence";
    public static final String SEQUENTIAL_PROCESS_TAG = "independentProcessSequence";
    public static final String MPI_PROCESS_TAG = "mpiProcess";
    public static final String MPI_PROCESS_OPTIONS_TAG = "mpiOptions";
    public static final String MPI_LOCAL_PATH_TAG = "localRelativePath";
    public static final String MPI_REMOTE_PATH_TAG = "remoteAbsolutePath";
    public static final String MPI_NOLOCAL = "noLocal";
    public static final String MPI_PROCESS_NUMBER_TAG = "processNumber";
    public static final String NG_PROCESS_TAG = "ngProcess";
    public static final String OARGRID_PROCESS_TAG = "oarGridProcess";
    public static final String HIERARCHICAL_PROCESS_TAG = "hierarchicalProcess";
    public static final String GRID_ENGINE_PROCESS_TAG = "gridEngineProcess";
    public static final String GLITE_PROCESS_OPTIONS_TAG = "gLiteOptions";
    public static final String GRID_ENGINE_OPTIONS_TAG = "gridEngineOption";
    public static final String PROCESSES_TAG = "processes";
    public static final String SERVICES_TAG = "services";
    public static final String EXTENDED_JVM_TAG = "extendedJvm";
    public static final String PROCESS_REFERENCE_TAG = "processReference";
    public static final String SERVICE_REFERENCE_TAG = "serviceReference";
    public static final String HIERARCHICIAL_REFERENCE_TAG = "hierarchicalReference";
    public static final String COMMAND_PATH_TAG = "commandPath";
    public static final String ENVIRONMENT_TAG = "environment";
    public static final String HOST_LIST_TAG = "hostlist";
    public static final String BSUB_OPTIONS_TAG = "bsubOption";

    //LoadLeveler XML tags
    public static final String LOADLEVELER_OPTIONS_TAG = "loadLevelerJobOptions";
    public static final String LOADLEVELER_TASK_REPARTITION_TAG = "loadLevelerTaskRepartition";
    public static final String LOADLEVELER_TASK_REPARTITION_TAG_SIMPLE = "simple";
    public static final String LOADLEVELER_TASK_REPARTITION_TAG_ADVANCED = "advanced";
    public static final String LL_OPT_WALL_CLOCK_LIMIT = "wallClockLimit";
    public static final String LL_OPT_RESOURCES = "resources";
    public static final String LL_OPT_INITIAL_DIR = "initialDir";
    public static final String LL_OPT_JOB_SUBMISSION_SCRIPT = "jobSubmissionScript";
    public static final String LL_OPT_EXECUTABLE = "executable";
    public static final String LL_OPT_ARGUMENTS = "arguments";
    public static final String LL_OPT_OUTPUT = "outputFile";
    public static final String LL_OPT_ERROR = "errorFile";
    public static final String LL_OPT_ENVIRONMENT = "taskEnvironment";
    public static final String LL_TASK_REP_BLOCKING = "blocking";
    public static final String LL_TASK_REP_NODE = "node";
    public static final String LL_TASK_REP_TASKS_PER_NODE = "tasks_per_node";
    public static final String LL_TASK_REP_TASK_GEOMETRY = "task_geometry";
    public static final String LL_TASK_REP_TOTAL_TASKS = "total_tasks";
    public static final String LL_TASK_REP_NBTASKS = "nbTasks";
    public static final String LL_TASK_REP_CPUS_PER_TASKS = "cpusPerTasks";
    public static final String LL_TASK_REP_TASKS_PER_HOSTS = "tasksPerHosts";
    public static final String RES_REQ_TAG = "resourceRequirement";
    public static final String SCRIPT_PATH_TAG = "scriptPath";
    public static final String GLOBUS_OPTIONS_TAG = "globusOption";
    public static final String COUNT_TAG = "count";
    public static final String GLOBUS_MAXTIME_TAG = "maxTime";
    public static final String PRUN_OPTIONS_TAG = "prunOption";
    public static final String PROCESSOR_TAG = "processor";
    public static final String HOSTS_NUMBER_TAG = "hostsNumber";
    public static final String PROCESSOR_PER_NODE_TAG = "processorPerNode";
    public static final String BOOKING_DURATION_TAG = "bookingDuration";
    public static final String QUEUE_NAME_TAG = "queueName";
    public static final String PARALLEL_ENVIRONMENT_TAG = "parallelEnvironment";
    public static final String OUTPUT_FILE = "outputFile";
    public static final String ERROR_FILE = "errorFile";
    public static final String PBS_OPTIONS_TAG = "pbsOption";
    public static final String OAR_OPTIONS_TAG = "oarOption";
    public static final String OARGRID_OPTIONS_TAG = "oarGridOption";
    public static final String OAR_RESOURCE_TAG = "resources";
    public static final String OARGRID_WALLTIME_TAG = "walltime";
    public static final String NG_OPTIONS_TAG = "ngOption";
    public static final String EXECUTABLE_TAG = "executable";

    //public static final String OAR_PROPERTY_TAG="properties";
    public static final String VARIABLE_TAG = "variable";
    public static final String CLASSPATH_TAG = "classpath";
    public static final String BOOT_CLASSPATH_TAG = "bootclasspath";
    public static final String JAVA_PATH_TAG = "javaPath";
    public static final String POLICY_FILE_TAG = "policyFile";
    public static final String LOG4J_FILE_TAG = "log4jpropertiesFile";
    public static final String PROACTIVE_PROPS_FILE_TAG = "ProActiveUserPropertiesFile";
    public static final String CLASSNAME_TAG = "classname";
    public static final String PARAMETERS_TAG = "parameters";
    public static final String ABS_PATH_TAG = "absolutePath";
    public static final String REL_PATH_TAG = "relativePath";
    public static final String GLITE_PATH_TAG = "JDLFilePath";
    public static final String GLITE_REMOTE_PATH_TAG = "JDLRemoteFilePath";
    public static final String GLITE_ARGUMENTS_TAG = "arguments";
    public static final String GLITE_INPUTSANDBOX_TAG = "inputSandbox";
    public static final String GLITE_OUTPUTSANDBOX_TAG = "outputSandbox";
    public static final String GLITE_ENVIRONMENT_TAG = "environment";
    public static final String GLITE_REQUIREMENTS_TAG = "requirements";
    public static final String GLITE_RANK_TAG = "rank";
    public static final String GLITE_CONFIG_TAG = "configFile";
    public static final String GLITE_INPUTDATA_TAG = "inputData";
    public static final String JVMPARAMETERS_TAG = "jvmParameters";
    public static final String JVMPARAMETER_TAG = "parameter";
    public static final String SECURITY_TAG = "security";
    public static final String SECURITY_FILE_TAG = "file";
    public static final String RMI_LOOKUP_TAG = "RMIRegistryLookup";
    public static final String P2P_SERVICE_TAG = "P2PService";
    public static final String PEERS_SET_TAG = "peerSet";
    public static final String PEER_TAG = "peer";
    public static final String FT_CONFIG_TAG = "faultTolerance";
    public static final String FT_CKPTSERVER_TAG = "checkpointServer";
    public static final String FT_RECPROCESS_TAG = "recoveryProcess";
    public static final String FT_LOCSERVER_TAG = "locationServer";
    public static final String FT_RESSERVER_TAG = "resourceServer";
    public static final String FT_GLOBALSERVER_TAG = "globalServer";
    public static final String FT_TTCVALUE_TAG = "ttc";
    public static final String FT_PROTO_TAG = "protocol";
    public static final String UNICORE_PROCESS_TAG = "unicoreProcess";
    public static final String UNICORE_OPTIONS_TAG = "unicoreOption";
    public static final String UNICORE_DIR_PATH_TAG = "unicoreDirPath";
    public static final String UNICORE_KEYFILE_PATH_TAG = "keyFilePath";
    public static final String UNICORE_USITE_TAG = "usite";
    public static final String UNICORE_VSITE_TAG = "vsite";
    public static final String FILE_TRANSFER_DEFINITIONS_TAG = "fileTransferDefinitions";
    public static final String FILE_TRANSFER_TAG = "fileTransfer";
    public static final String FILE_TRANSFER_FILE_TAG = "file";
    public static final String FILE_TRANSFER_DIR_TAG = "dir";
    public static final String FILE_TRANSFER_DEPLOY_TAG = "fileTransferDeploy";
    public static final String FILE_TRANSFER_RETRIEVE_TAG = "fileTransferRetrieve";
    public static final String FILE_TRANSFER_COPY_PROTOCOL_TAG = "copyProtocol";
    public static final String FILE_TRANSFER_SRC_INFO_TAG = "sourceInfo";
    public static final String FILE_TRANSFER_DST_INFO_TAG = "destinationInfo";
    public static final String FILE_TRANSFER_IMPLICT_KEYWORD = "implicit";
    public static final String TECHNICAL_SERVICE_ID = "technicalServiceId";
    public static final String TECHNICAL_SERVICES_TAG = "technicalServices";
    public static final String TECHNICAL_SERVICES_DEF_TAG = "technicalServiceDefinition";
    public static final String TECHNICAL_SERVICE_ARG_TAG = "arg";
}
