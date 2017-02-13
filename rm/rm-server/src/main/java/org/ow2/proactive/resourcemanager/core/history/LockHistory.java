/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.core.history;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Stores data about nodes locked per Node Source.
 *
 * @see org.ow2.proactive.resourcemanager.db.RMDBManager
 * @see org.ow2.proactive.resourcemanager.core.NodesLockRestorationManager
 */
@Entity
@Table(name = "LockHistory")
public class LockHistory {

    @Id
    @Column(name = "nodeSource")
    private String nodeSource;

    @Column(name = "lockCount")
    private int lockCount;

    /**
     * Default constructor
     */
    public LockHistory() {
        // required by Hibernate
    }

    public LockHistory(String nodeSource, int lockCount) {
        this.nodeSource = nodeSource;
        this.lockCount = lockCount;
    }

    public String getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(String nodeSource) {
        this.nodeSource = nodeSource;
    }

    public int decrementLockCount() {
        return --lockCount;
    }

    public int getLockCount() {
        return lockCount;
    }

    public int incrementLockCount() {
        return ++lockCount;
    }

    @Override
    public String toString() {
        return "LockHistory{" + "nodeSource='" + nodeSource + '\'' + ", lockCount=" + lockCount + '}';
    }

}
