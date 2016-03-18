package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.UpdatableProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class OnErrorPolicyInterpreter {

    public boolean requiresPauseTaskOnError(InternalJob job, InternalTask task) {

        return propertySetOnTaskOrJobLevel(job, task, OnTaskError.PAUSE_TASK);

    }

    public boolean requiresPauseJobOnError(InternalJob job, InternalTask task) {

        return propertySetOnTaskOrJobLevel(job, task, OnTaskError.PAUSE_JOB);

    }

    public boolean requiresCancelJobOnError(InternalJob job, InternalTask task) {

        return propertySetOnTaskOrJobLevel(job, task, OnTaskError.CANCEL_JOB);

    }

    private boolean propertySetOnTaskOrJobLevel(InternalJob job, InternalTask task, OnTaskError onTaskError) {
        if (propertyIs(task.getOnTaskErrorProperty(), onTaskError)) {
            return true;
        } else if (notSetOrNone(task.getOnTaskErrorProperty()) &&
            propertyIs(job.getOnTaskErrorProperty(), onTaskError)) {
            return true;
        }
        return false;
    }

    public boolean notSetOrNone(UpdatableProperties<OnTaskError> onTaskErrorProperty) {
        if (!onTaskErrorProperty.isSet()) {
            return true;
        } else if (propertyIs(onTaskErrorProperty, OnTaskError.NONE)) {
            return true;
        }

        return false;

    }

    private boolean propertyIs(UpdatableProperties<OnTaskError> onTaskErrorProperty,
            OnTaskError onTaskError) {

        return onTaskErrorProperty.isSet() &&
            onTaskErrorProperty.getValue().toString().equals(onTaskError.toString());
    }

}
