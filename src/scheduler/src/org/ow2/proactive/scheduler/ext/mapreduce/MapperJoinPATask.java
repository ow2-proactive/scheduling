package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.DefaultLogger;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.Logger;


/**
 * The {@link MapperJoinPATask} realizes the join of all the MapperPATask
 * replicas. We must notice that this tasks has an attached script
 * "$SCHEDULER/samples/jobs_descriptors/Workflow/mapreduce/replicateReducerPATask.js"
 * to replicate the ReducerPATask
 */
public class MapperJoinPATask extends JavaExecutable {

    Logger logger = DefaultLogger.getInstance();
    String outputFolderName = null;

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        super.init(args);

        // initialize the logger
        boolean debugLogLevel = Boolean.parseBoolean((String) (args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key)));
        logger.setDebugLogLevel(debugLogLevel);

        outputFolderName = (String) args
                .get(PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_OUTPUT_DIRECTORY_PROPERTY_NAME.key));
        logger.debug("The name of the output folder to create is: " + outputFolderName);
    }

    @Override
    public Serializable execute(TaskResult... taskResults) throws Throwable {

        /*
         * We must notice that the path of the output directory in which the
         * ReducerPATask must store its output is compliant to the following
         * format:
         * "$OUTPUT_DATASPACE/output/_temporary/_attempt__0007_r_070113_0/part-r-70113"
         * where the $OUTPUT_DATASPACE is the dataspace the user defined as the
         * output dataspace of the ProActive MapReduce workflow. The rest of the
         * path of the output folder to create is composed of other two parts:
         * one that is ReducerPATasks replica independent ("output/_temporary")
         * and another one that is ReducerPATask replica dependent
         * ("_attempt__0007_r_070113_0/part-r-70113"). ReducerPATask dependent
         * means that in the name of the folder to create the id of the
         * ReducerPATask replica appears, so that only the ReducerPATask
         * replica, whose id is equal to the one that appears in the folder to
         * create, can create that folder. Usually the output directory must be
         * created by the ReducerPATask. But the ReducerPATask is replicated.
         * This means that each replica tries to create the output directory.
         * This can lead to synchronization problems and one of the replica can
         * throw the following exception: Caused by:
         * org.apache.commons.vfs.FileSystemException: << Could not create
         * folder
         * "file:///auto/sop-nas2a/u/sop-nas2a/vol/home_oasis/eborelli/mapreduce/data/output/output"
         * . at org.apache.commons.vfs.provider.AbstractFileObject.createFolder(
         * AbstractFileObject.java:933)) >> To avoid this exception we must
         * create the ReducerPATask replica independent part of the output path
         * in this MapperJoinPATask while each ReducerPATask replica will create its own
         * ReducerPATask replica independent part of the output path. This means
         * this MapperJoinPATask must create the folder "output/_temporary" in
         * the ProActive MapReduce workflow output space.
         */
        if ((outputFolderName != null) && (!outputFolderName.trim().equalsIgnoreCase(""))) {
            String folderToCreate = outputFolderName + File.separator +
                PAMapReduceFramework.FILE_NAME_CONCATENATOR + PAMapReduceFramework.TEMPORARY;
            logger.debug("The folder to create is: " + folderToCreate);
            DataSpacesFileObject folderToCreateDataSpacesFileObject = getOutputSpace().resolveFile(
                    folderToCreate);
            if (!folderToCreateDataSpacesFileObject.exists()) {
                folderToCreateDataSpacesFileObject.createFolder();
                logger.debug("The output folder did not exist and it was created");
            }
        }

        /*
         * this task has not to generate any task result
         */
        return null;
    }

}
