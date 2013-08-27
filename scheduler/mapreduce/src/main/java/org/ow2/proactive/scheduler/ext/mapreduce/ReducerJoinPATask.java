package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.Serializable;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.DefaultLogger;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.Logger;


/**
 * The {@link ReducerJoinPATask} realizes the join of all the
 * {@link ReducerPATask} replicas. We must notice that this tasks has an
 * attached script
 * "$SCHEDULER/samples/jobs_descriptors/Workflow/mapreduce/reducerPATaskOutputFileTransfer.js"
 * to transfer the output files produced by the ReducerPATask tasks into the
 * output folder defined by the user
 *
 * @author The ProActive Team
 *
 */
public class ReducerJoinPATask extends JavaExecutable {

    protected static Logger logger = DefaultLogger.getInstance();

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        super.init(args);

        // initialize the logger
        boolean debugLogLevel = Boolean.parseBoolean((String) (args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key)));
        logger.setDebugLogLevel(debugLogLevel);
    }

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        return null;
    }
}
