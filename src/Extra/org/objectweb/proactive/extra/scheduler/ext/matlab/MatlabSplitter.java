package org.objectweb.proactive.extra.scheduler.ext.matlab;

import java.util.Map;

import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


public class MatlabSplitter extends SimpleMatlab {

    /**
     *
     */
    private static final long serialVersionUID = -5667273368988117301L;
    private int numberOfChildren;
    private static AOMatlabSplitter splitterWorker = null;

    public MatlabSplitter() {
    }

    public void init(Map<String, Object> args) throws Exception {
        super.init(args);
        Object nb = args.get("number_of_children");
        if (nb == null) {
            throw new IllegalArgumentException(
                "\"number_of_children\" must be specified.");
        }
        numberOfChildren = Integer.parseInt((String) nb);
    }

    @Override
    protected Object executeInternal(String uri, TaskResult... results)
        throws Throwable {
        System.out.println("[" + host +
            " MATLAB TASK] Deploying Worker (MatlabSplitter)");
        splitterWorker = (AOMatlabSplitter) deploy(uri,
                AOMatlabSplitter.class.getName(), matlabCommandName,
                inputScript, scriptLines, numberOfChildren);
        System.out.println("[" + host +
            " MATLAB TASK] Executing (MatlabSplitter)");
        Object res = splitterWorker.execute(results);
        res = ProFuture.getFutureValue(res);
        splitterWorker.terminate();
        return res;
    }
}
