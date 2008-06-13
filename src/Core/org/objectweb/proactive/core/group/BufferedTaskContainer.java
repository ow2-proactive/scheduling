package org.objectweb.proactive.core.group;

/**
 * A wrapper for a task to be dispatched (here, task = instance of AbstractProcessForGroup). 
 * 
 * It notifies a monitoring object so that runtime information can be collected
 * about the execution and completion of the task.
 * 
 * Allocation of task to worker is predefined in the task itself and is not changed.
 * 
 * 
 * @author The ProActive Team
 *
 */
public class BufferedTaskContainer implements Runnable {

    AbstractProcessForGroup task;
    DispatchMonitor dispatchMonitor;

    public BufferedTaskContainer(AbstractProcessForGroup task, DispatchMonitor dispatchMonitor) {
        // this.listOfTasks = listOfTasks;
        this.task = task;
        this.dispatchMonitor = dispatchMonitor;
        // System.out.println("[job buffered] " + task.getIndex());
    }

    public void run() {
        if (dispatchMonitor != null) {
            // in other words, dispatch is dynamic
            if (task instanceof ProcessForAsyncCall) {
                ((ProcessForAsyncCall) task).setDispatchMonitor(dispatchMonitor);
            }
            // System.out.println("[job buffered] to " + task.getIndex() + "");
            // dispatcher.jobAllocation(task.getIndex());
            dispatchMonitor.dispatchedTask(task);
        }
        task.run();

    }

}
