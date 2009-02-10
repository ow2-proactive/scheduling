/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Proxy;


/**
 * A simple String based implementation of TaskLogs.
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@Entity
@Table(name = "SIMPLE_TASK_LOGS")
@AccessType("field")
@Proxy(lazy = false)
public class SimpleTaskLogs implements TaskLogs {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    // logs on standard output
    @Cascade(CascadeType.ALL)
    @Column(name = "STANDARD_LOGS", columnDefinition = "CLOB")
    private String standardLogs;

    // logs on error output
    @Cascade(CascadeType.ALL)
    @Column(name = "ERROR_LOGS", columnDefinition = "CLOB")
    private String errorlogs;

    /** Hibernate constructor */
    @SuppressWarnings("unused")
    private SimpleTaskLogs() {
    }

    /**
     * Create a new SimpleTaskLogs.
     * @param stdLogs the standard output.
     * @param errLogs the error output.
     */
    public SimpleTaskLogs(String stdLogs, String errLogs) {
        this.standardLogs = stdLogs;
        this.errorlogs = errLogs;
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getAllLogs(boolean)
     */
    public String getAllLogs(boolean timeStamp) {
        return this.standardLogs + this.errorlogs;
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStderrLogs(boolean)
     */
    public String getStderrLogs(boolean timeStamp) {
        return this.errorlogs;
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStdoutLogs(boolean)
     */
    public String getStdoutLogs(boolean timeStamp) {
        return this.standardLogs;
    }
}
