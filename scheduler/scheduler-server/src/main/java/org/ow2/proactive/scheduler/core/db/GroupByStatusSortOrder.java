/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.db;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.ow2.proactive.db.SortOrder;
import org.ow2.proactive.scheduler.common.job.JobStatus;


/**
 * A custom sort for JobStatus.
 *
 * In ascending order, jobs are grouped in this order:
 * - pending jobs
 * - running, stalled, paused jobs
 * - other states (killed, finished,...)
 *
 */
public class GroupByStatusSortOrder extends Order {

    private static final long serialVersionUID = 62L;

    private String propertyName;
    private boolean ascending;

    public GroupByStatusSortOrder(SortOrder sortOrder, String property) {
        super(property, sortOrder.isAscending());
        propertyName = property;
        ascending = sortOrder.isAscending();
    }

    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        String column = criteriaQuery.getColumnsUsingProjection(criteria, propertyName)[0];
        return " case " +
            // pending first
            " when " + column + " = " + JobStatus.PENDING.ordinal() +
            " then 0 " +
            // running, stalled, paused then
            " when " + column + " = " + JobStatus.RUNNING.ordinal() + " then 1 " + " when " + column + " = " +
            JobStatus.STALLED.ordinal() + " then 1 " + " when " + column + " = " +
            JobStatus.PAUSED.ordinal() + " then 1 " +
            // and the rest (killed, finished, etc)
            " else 2 end " + (ascending ? " asc" : " desc");
    }

    @Override
    public Order ignoreCase() {
        throw new UnsupportedOperationException("not implemented");
    }
}
