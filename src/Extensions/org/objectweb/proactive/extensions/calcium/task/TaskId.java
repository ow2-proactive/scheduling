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
    static public int DEFAULT_ROOT_PARENT_ID = -1;
    private int familyId; //Id of the root task
    private int parentId;
    private int id;

    public TaskId() {
        this.id = ProActiveRandom.nextPosInt();
        this.parentId = DEFAULT_ROOT_PARENT_ID;
        this.familyId = id; //Default is root task, head of the family
    }

    /**
     * @return the familyId
     */
    public int getFamilyId() {
        return familyId;
    }

    /**
     * @param familyId the familyId to set
     */
    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    /**
     * @return the id
     */
    public int getId() {
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
    public int getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    /**
     * @param familyId
     * @param parentId
     * @param id
     */
    public TaskId(int familyId, int parentId, int id) {
        super();
        this.familyId = familyId;
        this.parentId = parentId;
        this.id = id;
    }

    @Override
    public String toString() {
        return this.familyId + "|" + parentId + "." + this.id;
    }

    public TaskId getNewChildId() {
        TaskId newId = new TaskId();
        newId.familyId = familyId;
        newId.parentId = id;
        return newId;
    }
}
