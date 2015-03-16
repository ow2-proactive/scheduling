package org.ow2.proactive.scheduler.core;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.util.PerfLogger;


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

        PerfLogger.log(job.getId() + ": handler working", job.getSubmissionWatch());
        service.jobs.jobSubmitted(job, service.infrastructure.getTaskClassServer(), service.infrastructure
                .getSpacesSupport());
    }

}
