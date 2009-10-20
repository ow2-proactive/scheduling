//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.event;

import org.ow2.proactive.scheduler.common.job.JobState;


/**
 * Event produced by the {@link InternalSchedulerEventListener} upon notifications received from the Scheduler
 * @author esalagea
 *
 */
public class InternalEvent {

    private EventType type;
    private JobState job;

    public InternalEvent(EventType type, JobState job) {
        super();
        this.type = type;
        this.job = job;
    }

    public EventType getType() {
        return type;
    }

    public JobState getJob() {
        return job;
    }

}
