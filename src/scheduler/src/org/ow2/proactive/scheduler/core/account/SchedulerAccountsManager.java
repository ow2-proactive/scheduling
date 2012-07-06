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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.core.account;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
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
        super("Scheduler Accounts Manager Refresher", ProActiveLogger
                .getLogger(SchedulerAccountsManager.class));

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