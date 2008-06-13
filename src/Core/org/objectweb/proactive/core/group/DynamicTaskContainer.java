package org.objectweb.proactive.core.group;

/**
 * A wrapper for a task to be dispatched (here, task = instance of AbstractProcessForGroup). 
 * 
 * Compared to {@link BufferedTaskContainer}, it not only notifies a monitoring object
 * about execution and completion of the task, it also dynamically allocates the task to
 * a worker.
 * 
 * 
 * 
 * @author The ProActive Team
 *
 */
public class DynamicTaskContainer implements Runnable {

    AbstractProcessForGroup task;
    DispatchMonitor dispatchMonitor;

    public DynamicTaskContainer(AbstractProcessForGroup task, DispatchMonitor dispatchMonitor) {
        this.task = task;
        this.dispatchMonitor = dispatchMonitor;
    }

    public void run() {
        Integer slot;
        // index assigned to task is reassigned for dynamic behavior
        slot = dispatchMonitor.getWorker();
        // System.out.println("got " + slot);
        task.setGroupIndex(slot);
        if (task instanceof ProcessForAsyncCall) {
            ((ProcessForAsyncCall) task).setDispatchMonitor(dispatchMonitor);
        }
        // System.out.println("retreived [" + job.getIndex() + "]");
        dispatchMonitor.dispatchedTask(task);
        task.run();
        // System.out.print("...[" + dispatcher.getAvailableSlots().size() + "]
        // " + DispatcherBasic.counter++ +"...");

    }
}
