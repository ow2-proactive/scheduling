/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.task;

import java.io.Serializable;

import org.objectweb.proactive.core.util.ProActiveRandom;


public class TaskId implements Serializable {
    static final public TaskId DEFAULT_ROOT_PARENT_ID = null;
    private TaskId familyId; //Id of the root task
    private TaskId parentId;
    private int id;

    public TaskId() {
        this.id = ProActiveRandom.nextPosInt();
        this.parentId = DEFAULT_ROOT_PARENT_ID;
        this.familyId = this; //Default is root task, head of the family
    }

    /**
     * @param familyId
     * @param parentId
     * @param id
     */
    private TaskId(TaskId familyId, TaskId parentId, int id) {
        super();
        this.familyId = familyId;
        this.parentId = parentId;
        this.id = id;
    }

    /**
    * @return the familyId
    */
    public TaskId getFamilyId() {
        return familyId;
    }

    /**
     * @param familyId the familyId to set
     */
    public void setFamilyId(int familyId) {
        this.familyId.id = familyId;
    }

    /**
     * @return the id
     */
    public int value() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the parentId
     */
    public TaskId getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(TaskId parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        if (isRootTaskId()) {
            return this.familyId.value() + "|" + this.id;
        }
        return this.familyId.value() + "|" + parentId.value() + "." + this.id;
    }

    public TaskId getNewChildId() {
        TaskId newId = new TaskId();
        newId.familyId = familyId;
        newId.parentId = this;
        return newId;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = (PRIME * result) + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        /*
        if (getClass() != obj.getClass()){
                return false;
        }
        */
        final TaskId other = (TaskId) obj;

        if (isRootTaskId()) {
            return (this.id == other.id) &&
            (familyId.value() == other.familyId.value());
        }
        return (this.id == other.id) &&
        (familyId.value() == other.familyId.value()) &&
        (parentId.value() == other.parentId.value());
        //return true;
    }

    public boolean isRootTaskId() {
        if (this.parentId == null) {
            return true;
        }

        return false;
    }

    public boolean isBrotherTask(TaskId taskId) {
        if ((this.parentId == null) && (taskId.parentId == null)) {
            return true;
        }

        if (this.parentId.value() == taskId.parentId.value()) {
            return true;
        }

        return false;
    }
}
