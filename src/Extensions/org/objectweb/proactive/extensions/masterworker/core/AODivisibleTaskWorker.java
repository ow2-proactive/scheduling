package org.objectweb.proactive.extensions.masterworker.core;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.WorkerMaster;

import java.io.Serializable;
import java.util.Map;


/**
 * A worker specialized in dealing with divisible tasks
 * (it will basically execute only one task and die)
 * @author The ProActive Team
 */
public class AODivisibleTaskWorker extends AOWorker implements RunActive, InitActive {

    /**
    * log4j logger of the worker
    */
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.MASTERWORKER_WORKERS);
    private static final boolean debug = logger.isDebugEnabled();

    private SubMasterImpl submaster;
    private TaskIntern<Serializable> task;
    private String parentName;

    /**
     * ProActive no-arg constructor
     */
    @Deprecated
    public AODivisibleTaskWorker() {

    }

    public void initActivity(final Body body) {
        // Do nothing, overrides super class init activity
    }

    /**
     * Creates a worker with the given name
     * @param name name of the worker
     * @param provider the entity which will provide tasks to the worker
     * @param initialMemory initial memory of the worker
     */
    public AODivisibleTaskWorker(final String name, final WorkerMaster provider,
            final Map<String, Serializable> initialMemory, final TaskIntern<Serializable> task) {
        super(name, provider, initialMemory);
        this.submaster = new SubMasterImpl(provider, name);
        this.task = task;

    }

    public void readyToLive() {
        // do nothing it's just for synchronization
    }

    public void runActivity(Body body) {
        Service service = new Service(body);

        // Synchronization with the parent worker
        service.waitForRequest();
        service.serveOldest();

        // The single activity of this worker
        handleTask();

    }

    public void handleTask() {
        Serializable resultObj = null;
        boolean gotCancelled = false;
        ResultInternImpl result = new ResultInternImpl(task);
        // We run the task and listen to exception thrown by the task itself
        try {
            if (debug) {
                logger.debug(name + " runs task " + task.getId() + "...");
            }

            resultObj = task.run(memory, submaster);
        } catch (IsClearingException ex) {
            gotCancelled = true;

        } catch (Exception e) {
            result.setException(e);
        }
        if (!gotCancelled) {

            // We store the result inside our internal version of the task
            result.setResult(resultObj);
            if (debug) {
                logger
                        .debug(name + " sends the result of task " + result.getId() +
                            " and asks a new task...");
            }

            // We send the result back to the master

            BooleanWrapper wrap = provider.sendResult(result, name);
            // We synchronize the answer to avoid a BodyTerminatedException (the AO terminates right after this call)
            PAFuture.waitFor(wrap);
        }
    }
}
