package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.UpdatableProperties;


public class OnErrorPolicyInterpreter {

    public boolean requiresPauseTaskOnError(Task task) {
        return propertyIs(task.getOnTaskErrorProperty(), OnTaskError.PAUSE_TASK);
    }

    public boolean requiresPauseJobOnError(Task task) {
        return propertyIs(task.getOnTaskErrorProperty(), OnTaskError.PAUSE_JOB);
    }

    public boolean requiresCancelJobOnError(Task task) {
        return propertyIs(task.getOnTaskErrorProperty(), OnTaskError.CANCEL_JOB);
    }

    public boolean notSetOrNone(Task task) {
        if (!task.getOnTaskErrorProperty().isSet()) {
            return true;
        } else if (propertyIs(task.getOnTaskErrorProperty(), OnTaskError.NONE)) {
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
