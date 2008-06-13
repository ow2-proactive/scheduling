package org.objectweb.proactive.core.group;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * Keeps track of dispatched tasks. 
 * 
 * Maintains a ranking among workers.
 * 
 * Provides the dispatcher with the most appropriate worker depending
 * on this ranking. 
 * 
 * By default, "best" workers are those that: have not been allocated tasks,
 * have empty buffers, are faster to complete tasks (independently of the size 
 * of the task).
 * 
 * 
 * @author The ProActive Team
 *
 */
public class DispatchMonitor {

    int instance;

    public static final int WORKER_LOAD_WEIGHT = 0;
    public static final int WORKER_SPEED_WEIGHT = 10;

    BlockingQueue<Integer> availableSlots = new LinkedBlockingQueue<Integer>();

    ProxyForGroup groupProxy;
    Map<Integer, Worker> dispatched = new ConcurrentHashMap<Integer, Worker>();
    Map<Worker, Worker> replicated = new ConcurrentHashMap<Worker, Worker>();

    LinkedList<Worker> rankedWorkers = new LinkedList<Worker>();

    public DispatchMonitor(ProxyForGroup groupProxy, int instance) {
        this.instance = instance;
        this.groupProxy = groupProxy;
        int index = 0;
        for (Iterator iterator = groupProxy.iterator(); iterator.hasNext();) {
            Object member = (Object) iterator.next();
            Worker worker = new Worker(index, groupProxy.bufferSize);
            rankedWorkers.add(worker);
            dispatched.put(index, worker);
            index++;
        }

    }

    public void dispatchedTask(AbstractProcessForGroup task) {
        DispatchedJob job = new DispatchedJob(task.groupIndex, System.currentTimeMillis(), task);
        dispatched.get(task.getGroupIndex()).addDispatchedJob(job);
        //		System.err.println(" -- added job for worker " + job.workerIndex + " : (" + instance + " ) " + rankedWorkers);
    }

    // could add history of completed tasks: index, worker index, time, current
    // avgt time, variance

    public void jobCompleted(Integer slot) {
        // update completed list
        Worker worker = dispatched.get(slot);
        worker.completedJob();
        availableSlots.offer(slot);
    }

    public Integer getWorker() {

        // can use custom algo:
        // pick fastest worker among available slots
        // pick a worker that has an emptied queue
        try {
            availableSlots.take();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        synchronized (rankedWorkers) {
            Collections.sort(rankedWorkers);
            return rankedWorkers.getFirst().getIndex();
        }

    }

    private static class DispatchedJob {

        int workerIndex;
        long initTime;
        long endTime;
        AbstractProcessForGroup task;

        public DispatchedJob(int workerIndex, long currentTime, AbstractProcessForGroup task) {
            this.workerIndex = workerIndex;
            this.initTime = currentTime;
            this.task = task;
        }

        public void setEndTime(long currentTime) {
            this.endTime = currentTime;
        }

        public String toString() {
            return ("job: " + task + " start time = " + initTime + " ; end time = " + endTime);
        }
    }

    private static class Worker implements Comparable<Worker> {
        int index;
        int bufferSize = 0;
        //	      Stats stats;
        Queue<DispatchedJob> dispatchedJobs = new ConcurrentLinkedQueue<DispatchedJob>();
        Queue<DispatchedJob> completedJobs = new ConcurrentLinkedQueue<DispatchedJob>();

        public Worker(int index, int bufferSize) {
            this.index = index;
            this.bufferSize = bufferSize;
        }

        public int compareTo(Worker w) {
            // faster workers ranked first
            // faster means : 
            // 1. available worker
            // 2. buffer available
            // 3. more jobs completed

            if (this.equals(w)) {
                return 0;
            }

            // favor filling buffers first
            int mySize = dispatchedJobs.size();
            int itsSize = w.dispatchedJobs.size();
            if (mySize == 0 && itsSize > 0) {
                return -1;
            } else if (itsSize == 0) {
                return +1;
            }
            // compare buffers
            if (mySize < bufferSize) {
                if (itsSize < bufferSize) {
                    if (mySize < itsSize) {
                        return +1;
                    } else if (mySize == itsSize) {
                        return Long.valueOf(averageCompletionTime()).compareTo(
                                Long.valueOf(w.averageCompletionTime()));
                    } else if (mySize > itsSize) {
                        return -1;
                    }
                } else {
                    return -1;
                }
            } else if (itsSize < bufferSize) {
                return +1;
            }
            if (completedJobs.size() > w.completedJobs.size()) {
                return -1;
            } else if (completedJobs.size() < w.completedJobs.size()) {
                return +1;
            } else {
                if (completedJobs.size() == 0) {
                    if (dispatchedJobs.size() > w.dispatchedJobs.size()) {
                        return +1;
                    } else if (dispatchedJobs.size() < w.dispatchedJobs.size()) {
                        return -1;
                    } else {
                        //                        return 0;
                        // do not return 0 as it would violate compareTo spec (see javadoc)
                        return -1; // TODO find a comparable constant identifier for workers instead
                    }
                } else {
                    // same size for completed jobs. 
                    return Long.valueOf(averageCompletionTime()).compareTo(
                            Long.valueOf(w.averageCompletionTime()));
                }
            }

        }

        public int getIndex() {
            return index;
        }

        public void addDispatchedJob(DispatchedJob job) {
            dispatchedJobs.add(job);

        }

        public void completedJob() {
            try {

                DispatchedJob job = dispatchedJobs.remove();
                job.setEndTime(System.currentTimeMillis());
                completedJobs.add(job);
            } catch (NoSuchElementException e) {
                throw new RuntimeException(e);
            }
        }

        long averageCompletionTime() {
            if (completedJobs.size() == 0) {
                return 0;
            }
            long sum = 0;
            for (Iterator iterator = completedJobs.iterator(); iterator.hasNext();) {
                DispatchedJob job = (DispatchedJob) iterator.next();
                sum += (job.endTime - job.initTime);
            }
            return (sum / completedJobs.size());
        }

        public String toString() {
            return "[worker " + index + "] - completed=" + completedJobs.size() + " - dispatched=" +
                dispatchedJobs.size();
        }

    }

    public void updatedResult(Proxy originatingProxy) {

        boolean refIsBodyProxy = (originatingProxy instanceof BodyProxy);
        //		boolean refIsBodyAdapterImpl = (originatingProxy instanceof BodyAdapterImpl);
        boolean refIsBodyAdapterImpl = false;
        for (int i = 0; i < groupProxy.getMemberList().size(); i++) {
            BodyProxy groupMemberProxy = (BodyProxy) ((StubObject) groupProxy.getMemberList().get(i))
                    .getProxy();
            // need some workaround because getBodyID is not part of Proxy
            // interface
            if (refIsBodyProxy) {
                if (groupMemberProxy.getBodyID().equals(((BodyProxy) originatingProxy).getBodyID())) {
                    updatedResult(i);
                    return;
                }
            }
        }
        throw new ProActiveRuntimeException("could not find proxy to set updated result!!!!");
    }

    public void updatedResult(int index) {
        jobCompleted(index);

    }

}
