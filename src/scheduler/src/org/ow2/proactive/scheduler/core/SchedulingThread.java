package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


class SchedulingThread extends Thread {

    static final int SCHEDULER_TIME_OUT = PASchedulerProperties.SCHEDULER_TIME_OUT.getValueAsInt();

    private final SchedulingMethod schedulingMethod;

    private final SchedulingService service;

    SchedulingThread(SchedulingMethod schedulingMethod, SchedulingService service) {
        super("SchedulingThread");
        this.schedulingMethod = schedulingMethod;
        this.service = service;
    }

    public void run() {
        boolean tasksStarted;

        while (!isInterrupted()) {
            try {
                tasksStarted = false;
                if (service.status == SchedulerStatus.STARTED || service.status == SchedulerStatus.PAUSED ||
                    service.status == SchedulerStatus.STOPPED) {
                    tasksStarted = schedulingMethod.schedule() > 0;
                }
                if (!tasksStarted) {
                    Thread.sleep(SCHEDULER_TIME_OUT);
                }
            } catch (InterruptedException e) {
                break;
            } catch (Throwable t) {
                service.handleException(t);
            }
        }
    }

}
