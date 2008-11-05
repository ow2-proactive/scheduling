package org.ow2.proactive.scheduler.gui.wizards.flatJobWizard;

import java.util.Comparator;
import org.ow2.proactive.scheduler.common.task.Task;


public class TaskComparator implements Comparator<Task> {

    @Override
    public int compare(Task o1, Task o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
