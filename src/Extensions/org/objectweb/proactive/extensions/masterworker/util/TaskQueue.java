package org.objectweb.proactive.extensions.masterworker.util;

import java.util.LinkedList;
import java.util.Queue;


/**
 * Queue of pending tasks, able to tell if some tasks have been submitted by a given worker
 *
 * @author The ProActive Team
 */
public class TaskQueue extends LinkedList<TaskID> implements Queue<TaskID> {

    public TaskQueue() {
        super();
    }

    public int countTasksByOriginator(String originator) {
        int count = 0;
        for (TaskID tid : this) {
            if (tid.getOriginator().equals(originator)) {
                count++;
            }
        }

        return count;
    }

    public boolean hasTasksByOriginator(String originator) {
        for (TaskID tid : this) {
            if (tid.getOriginator().equals(originator)) {
                return true;
            }
        }
        return false;
    }

}
