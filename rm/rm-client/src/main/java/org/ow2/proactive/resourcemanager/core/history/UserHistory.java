/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core.history;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;
import org.ow2.proactive.resourcemanager.authentication.Client;


/**
 * 
 * This class represents the users connection history.
 *
 */
@Entity
@Table(name = "UserHistory")
public class UserHistory {

    public static final Logger logger = Logger.getLogger(UserHistory.class);

    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    protected long id;

    @Column(name = "userName")
    @Index(name = "userNameIndex")
    private String userName;

    @Column(name = "startTime")
    protected long startTime;

    @Column(name = "endTime")
    protected long endTime;

    /**
     * Default constructor for Hibernate
     */
    public UserHistory() {
    }

    /**
     * Constructs new history record.
     */
    public UserHistory(Client client) {
        this.userName = client.getName();
        this.startTime = System.currentTimeMillis();
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
