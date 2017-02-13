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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.authentication.Client;


/**
 * This class represents the users connection history.
 */
@Entity
@Table(name = "UserHistory", indexes = { @Index(name = "USER_HISTORY", columnList = "userName") })
public class UserHistory {

    public static final Logger logger = Logger.getLogger(UserHistory.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_HISTORY_ID_SEQUENCE")
    @SequenceGenerator(name = "USER_HISTORY_ID_SEQUENCE", sequenceName = "USER_HISTORY_ID_SEQUENCE")
    @SuppressWarnings("unused")
    protected long id;

    @Column(name = "userName")
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
