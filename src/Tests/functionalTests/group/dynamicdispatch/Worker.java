package functionalTests.group.dynamicdispatch;

import org.objectweb.proactive.core.group.DispatchMode;
import org.objectweb.proactive.core.group.Dispatch;


public class Worker {

    int workerIndex;

    public Worker() {
    }

    public Worker(int index) {
        this.workerIndex = index;
    }

    public Task executeTask(Task t) {
        System.out.println("running worker " + workerIndex);
        t.execute(workerIndex);
        return t;
    }

    public void killWorker(int i) {
        if (workerIndex == i)
            System.exit(0);

    }

    @Dispatch(mode = DispatchMode.DYNAMIC)
    public Task executeDynamically(Task t) {
        return executeTask(t);
    }

}
