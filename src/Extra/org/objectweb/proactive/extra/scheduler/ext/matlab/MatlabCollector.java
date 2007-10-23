package org.objectweb.proactive.extra.scheduler.ext.matlab;

import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


public class MatlabCollector extends SimpleMatlab {
    private static AOMatlabCollector collectorWorker = null;

    /**
     *
     */
    private static final long serialVersionUID = 8172262790006689713L;

    public MatlabCollector() {
    }

    @Override
    protected Object executeInternal(String uri, TaskResult... results)
        throws Throwable {
        System.out.println("[" + host +
            " MATLAB TASK] Deploying Worker (MatlabCollector)");
        collectorWorker = (AOMatlabCollector) deploy(uri,
                AOMatlabCollector.class.getName(), matlabCommandName,
                inputScript, scriptLines);
        System.out.println("[" + host + " MATLAB TASK] Executing (Collector)");
        Object res = collectorWorker.execute(results);
        res = ProFuture.getFutureValue(res);
        collectorWorker.terminate();
        return res;
    }
}
