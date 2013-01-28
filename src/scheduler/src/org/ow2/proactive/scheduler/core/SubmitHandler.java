package org.ow2.proactive.scheduler.core;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.job.InternalJob;


class SubmitHandler implements Runnable {

    static final Logger logger = Logger.getLogger(SchedulingService.class);

    private final InternalJob job;

    private final SchedulingService service;

    SubmitHandler(SchedulingService service, InternalJob job) {
        this.service = service;
        this.job = job;
    }

    @Override
    public void run() {
        logger.info("Submitting a new job '" + job.getName() + "'");

        service.jobs.jobSubmitted(job);

        // TODO cdelbe : create classserver only when job is running ?
        // TODO: here to create classserver can get classpath content directly from InternalJob
        job.getEnvironment().clearJobClasspathContent();
        service.infrastructure.getTaskClassServer().createTaskClassServer(job);
    }

}
