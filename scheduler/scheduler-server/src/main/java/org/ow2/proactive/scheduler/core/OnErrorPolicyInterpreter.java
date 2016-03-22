package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.UpdatableProperties;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class OnErrorPolicyInterpreter {

    public boolean requiresPauseTaskOnError(InternalTask task) {
        return propertyIs(task.getOnTaskErrorProperty(), OnTaskError.PAUSE_TASK);
    }

    public boolean requiresPauseJobOnError(InternalTask task) {
        return propertyIs(task.getOnTaskErrorProperty(), OnTaskError.PAUSE_JOB);
    }

    public boolean requiresCancelJobOnError(InternalTask task) {
        return propertyIs(task.getOnTaskErrorProperty(), OnTaskError.CANCEL_JOB);
    }

    private boolean propertyIs(UpdatableProperties<OnTaskError> onTaskErrorProperty,
            OnTaskError onTaskError) {
        return onTaskErrorProperty.isSet() &&
            onTaskErrorProperty.getValue().toString().equals(onTaskError.toString());
    }

}
