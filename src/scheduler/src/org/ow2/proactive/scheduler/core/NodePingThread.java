package org.ow2.proactive.scheduler.core;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


class NodePingThread extends Thread {

    private final SchedulingService service;

    static final Logger logger = Logger.getLogger(SchedulingService.class);

    private static final long SCHEDULER_NODE_PING_FREQUENCY = PASchedulerProperties.SCHEDULER_NODE_PING_FREQUENCY
            .getValueAsInt() * 1000;

    NodePingThread(SchedulingService service) {
        super("NodePingThread");
        this.service = service;
    }

    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(SCHEDULER_NODE_PING_FREQUENCY);
                for (final RunningTaskData taskData : service.jobs.getRunningTasks()) {
                    service.getInfrastructure().submit(new Runnable() {
                        public void run() {
                            service.pingTaskNode(taskData);
                        }
                    });
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                logger.info("Nodes pingining failed", e);
            }
        }
    }

}
