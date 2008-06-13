package org.objectweb.proactive.core.group;

import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveRuntimeException;


/**
 * Handles the allocation of tasks to workers.
 * 
 * It can work in a static mode or in a dynamic mode. 
 * 
 * In the static mode, task allocation to workers is predefined.
 * 
 * In the dynamic mode, workers buffers are filled first, and remaining tasks
 * are dynamically allocated to workers. These workers are selected based on their
 * previous performance. (This technique does not consider heterogeneity of tasks, but
 * copes with network latency).
 * 
 * @author The ProActive Team
 *
 */
public class Dispatcher {

    ThreadPoolExecutor threadPool;
    ProxyForGroup groupProxy;

    Body body;

    static int dispatcherIndex = 0;

    int nbAdditionalThreads = 3;

    int memberToThreadRatio = 4;

    public static volatile int counter = 0;

    // boolean dynamic = false;

    public Dispatcher(ProxyForGroup groupProxy, boolean dynamic, int bufferSize) {
        this.groupProxy = groupProxy;

        body = ProActive.getBodyOnThis();
        // thread pool is configurable
        threadPool = new ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
            GroupThreadFactory.instance());

    }

    protected int getOptimalPoolSize(int nbMembers) {
        // return (nbMembers == 0 ? 1 : nbMembers);
        // ((ThreadPoolExecutor) threadPool).setCorePoolSize(nbMembers);
        // if (nbMembers ==0) {
        // return 1;
        // }
        // return nbMembers+10;

        if (this.memberToThreadRatio != 0) {
            // TODO also depends on the nb of processors available
            int corePoolSize = ((int) Math.ceil(((float) nbMembers) / ((float) this.memberToThreadRatio))) +
                this.nbAdditionalThreads;
            return corePoolSize;
        } else {
            return nbAdditionalThreads;
        }
    }

    private void checkOptimalPoolSize() {
        threadPool.setCorePoolSize(getOptimalPoolSize(groupProxy.getMemberList().size()));
        threadPool.setMaximumPoolSize(getOptimalPoolSize(groupProxy.getMemberList().size()));
    }

    public void dispatchTasks(Queue<AbstractProcessForGroup> taskList, CountDownLatch doneSignal,
            Dispatch loadBalancingAnnotation) {
        checkOptimalPoolSize();

        int nbTasks = taskList.size();
        DispatchMode balancingMode;
        int bufferSize;
        if (loadBalancingAnnotation == null) {
            balancingMode = groupProxy.dispatchMode;
            bufferSize = groupProxy.bufferSize;
        } else {
            balancingMode = loadBalancingAnnotation.mode();
            bufferSize = loadBalancingAnnotation.bufferSize();
        }
        DispatchMonitor dispatchMonitor = (balancingMode.equals(DispatchMode.DYNAMIC) || balancingMode
                .equals(DispatchMode.STATIC_RANDOM)) ? new DispatchMonitor(groupProxy, dispatcherIndex++)
                : null;

        // BlockingQueue<AbstractProcessForGroup> spawnedTasks = new
        // LinkedBlockingQueue<AbstractProcessForGroup>();
        // TODO verify some stuff about nbTasks vs bufferSize vs nbMembers
        // if simple group call, do not use buffering
        for (int i = 0; (i < bufferSize && taskList.size() > 0); i++) {
            SortedSet<Integer> alreadyTargeted = new TreeSet<Integer>();

            // TODO NO NEED FOR ALREADY TARGETED : JUST USE RANDOMIZATION FOR
            // DYNAMIC DISPATCH!
            for (int j = 0; j < groupProxy.getMemberList().size(); j++) {

                boolean foundTaskWithAvailableTarget = false;
                int iterations = 0;
                while (!foundTaskWithAvailableTarget) {
                    if (iterations > taskList.size()) {
                        throw new ProActiveRuntimeException("" + "incorrect task allocation");
                    }
                    AbstractProcessForGroup task = taskList.poll();
                    if (task == null) {
                        // partial multicast or unicast: less tasks than workers
                        break;
                    }
                    if (alreadyTargeted.contains(task.getGroupIndex())) {
                        // re-enqueue
                        taskList.offer(task);
                    } else {
                        // group index and result index are kept as they have
                        // been generated (i.e. round robin or random for group
                        // index)
                        threadPool.execute(new BufferedTaskContainer(task, dispatchMonitor));
                        alreadyTargeted.add(task.getGroupIndex());
                        foundTaskWithAvailableTarget = true;
                    }
                    iterations++;
                }
            }
        }

        for (int i = (bufferSize * groupProxy.getMemberList().size()); i < nbTasks; i++) {
            AbstractProcessForGroup task = taskList.poll();
            // dynamic dispatch is set on a per-task basis
            threadPool.execute(task.isDynamicallyDispatchable() ? new DynamicTaskContainer(task,
                dispatchMonitor) : new BufferedTaskContainer(task, dispatchMonitor));
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Currently just for naming thread created by group communications. <br>
     * It may be extended by customizing the group threads in order to add other
     * features, such as logging, checkpointing, communication error management
     * etc...
     * 
     * @author The ProActive Team
     * 
     */
    protected static class GroupThreadFactory implements java.util.concurrent.ThreadFactory {

        private static GroupThreadFactory instance = null;

        private static final String genericPoolName = "PAGroup pool ";
        private static int poolIndex = 0;
        private final String poolName;

        private int threadIndex = 0;

        private GroupThreadFactory() {
            this.poolName = genericPoolName + poolIndex++;
        }

        public static GroupThreadFactory instance() {
            if (instance == null) {
                return instance = new GroupThreadFactory();
            } else {
                return instance;
            }
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, poolName + " - thread " + threadIndex++);
        }

    }

    public int getBufferSize() {
        return groupProxy.bufferSize;
    }

}
