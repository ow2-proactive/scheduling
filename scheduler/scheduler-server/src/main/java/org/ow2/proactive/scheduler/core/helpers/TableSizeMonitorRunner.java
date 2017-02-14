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
package org.ow2.proactive.scheduler.core.helpers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.ow2.proactive.db.SessionWork;
import org.ow2.proactive.db.TransactionHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

/**
 * TableSizeMonitorRunner will poll the DB for table counts for debugging purpose and display them in the logs.
 *
 * @author ActiveEon Team
 * @since 14/02/17
 */
public class TableSizeMonitorRunner implements Runnable {

    private static final String LOG_FLAG = "TABLE-SIZE-MONITOR#";
    private static String TS_PATTERN = "yyyy.MM.dd.HH.mm.ss";
    private static TransactionHelper transactionHelper;
    private static Logger logger;
    private HashMap<String, Long> counts = new HashMap<String, Long>();
    private final ArrayList<String> tableNames = new ArrayList<String>(
            Arrays.asList("JobData (All)", "JobData (Finished)", "JobContent",
                    "TaskData", "TaskDataNotFinished", "SelectorData",
                    "EnvironmentModifierData", "ScriptData", "SelectionScriptData",
                    "TaskDataVariable", "TaskResultData",
                    "ThirdPartyCredentialData"));

    public TableSizeMonitorRunner(TransactionHelper transactionHelper, Logger logger) {
        this.transactionHelper = transactionHelper;
        this.logger = logger;
        if (PASchedulerProperties.SCHEDULER_DB_SIZE_MONITORING_TS_PATTERN.isSet()) {
            TS_PATTERN = PASchedulerProperties.SCHEDULER_DB_SIZE_MONITORING_TS_PATTERN.getValueAsString();
        }
    }

    private Long getCount(final String queryName) {
        return transactionHelper.executeReadOnlyTransaction(new SessionWork<Long>() {
            @Override
            public Long doInTransaction(Session session) {
                Query query = session.getNamedQuery(queryName);
                return (Long) query.uniqueResult();
            }
        });
    }

    private void logCounts(String date) {
        StringBuilder sb = new StringBuilder();
        sb.append(LOG_FLAG + date);
        for (String key : tableNames) {
            sb.append(";" + counts.get(key));
        }
        logger.info(sb.toString());
    }

    @Override
    public void run() {
//        final StringBuilder names = new StringBuilder();
//        names.append(LOG_FLAG + "timestamp");
//        for (String name : tableNames) {
//            names.append(";" + name);
//        }
//        logger.info(names.toString());
        counts.put("JobData (All)", getCount("countJobData"));
        counts.put("JobData (Finished)", getCount("countJobDataFinished"));
        counts.put("JobContent", getCount("countJobContent"));
        counts.put("TaskData", getCount("countTaskData"));
        counts.put("TaskDataNotFinished", getCount("countTaskDataNotFinished"));
        counts.put("SelectorData", getCount("countSelectorData"));
        counts.put("EnvironmentModifierData", getCount("countEnvironmentModifierData"));
        counts.put("ScriptData", getCount("countScriptData"));
        counts.put("SelectionScriptData", getCount("countSelectionScriptData"));
        counts.put("TaskDataVariable", getCount("countTaskDataVariable"));
        counts.put("TaskResultData", getCount("countTaskResultData"));
        counts.put("ThirdPartyCredentialData", getCount("countThirdPartyCredentialData"));
        logCounts(new SimpleDateFormat(TS_PATTERN).format(new Date()));
    }
}
