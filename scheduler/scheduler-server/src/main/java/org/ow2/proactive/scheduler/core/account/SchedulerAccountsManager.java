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
package org.ow2.proactive.scheduler.core.account;

import org.apache.log4j.Logger;
import org.ow2.proactive.account.AbstractAccountsManager;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * This class is responsible to read periodically accounts from the scheduler database.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public final class SchedulerAccountsManager extends AbstractAccountsManager<SchedulerAccount> {

    /** Scheduler db manager used to submit SQL requests */
    private final SchedulerDBManager dbManager;

    /**
     * Create a new instance of this class.
     */
    public SchedulerAccountsManager(SchedulerDBManager dbManager) {
        super("Scheduler Accounts Manager Refresher", Logger.getLogger(SchedulerAccountsManager.class));

        this.dbManager = dbManager;
    }

    /**
     * Reads database and fills accounts for specified user.
     */
    protected SchedulerAccount readAccount(String username) {
        return dbManager.readAccount(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDefaultCacheValidityTimeInSeconds() {
        return PASchedulerProperties.SCHEDULER_ACCOUNT_REFRESH_RATE.getValueAsInt();
    }

}
