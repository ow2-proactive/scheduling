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
package functionaltests.service;

import static com.google.common.truth.Truth.assertWithMessage;

import java.util.List;

import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.db.SessionWork;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;


/**
 * The purpose of this class is to test the database schema that is generated
 * by Hibernate based on the annotations and values that are used.
 * <p>
 * For instance, Oracle database does not support table, column or index name
 * whose length is greater than 30 characters.
 */
public class DatabaseSchemaTest {

    private static RMDBManager rmDbManager;

    private static SchedulerDBManager schedulerDbManager;

    private static final int NAME_LENGTH_LIMIT = 30; // characters

    @BeforeClass
    public static void setUp() {
        rmDbManager = RMDBManager.createInMemoryRMDBManager();
        schedulerDbManager = SchedulerDBManager.createInMemorySchedulerDBManager();
    }

    @Test
    public void testColumnNamesShouldNotExceedLengthLimit() {
        runAndCheck("SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC'",
                    new Constraint<Object[]>() {
                        @Override
                        public void check(List<Object[]> databaseObjects) {
                            for (Object[] columns : databaseObjects) {
                                String tableName = (String) columns[0];
                                String columnName = (String) columns[1];

                                assertWithMessage("Table " + tableName + ", Column " + columnName)
                                                                                                  .that(tableName.length())
                                                                                                  .isLessThan(NAME_LENGTH_LIMIT +
                                                                                                              1);
                            }
                        }
                    });
    }

    @Test
    public void testIndexNamesShouldNotExceedLengthLimit() {
        runAndCheck("SELECT INDEX_NAME FROM INFORMATION_SCHEMA.SYSTEM_INDEXINFO " +
                    "WHERE TABLE_SCHEM = 'PUBLIC' AND INDEX_NAME NOT LIKE 'SYS%'", new Constraint<String>() {
                        @Override
                        public void check(List<String> databaseObjects) {
                            for (String indexName : databaseObjects) {
                                assertWithMessage("Index " + indexName).that(indexName.length())
                                                                       .isLessThan(NAME_LENGTH_LIMIT + 1);
                            }
                        }
                    });
    }

    @Test
    public void testTableNamesShouldNotExceedLengthLimit() {
        runAndCheck("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_TYPE = 'TABLE'",
                    new Constraint<String>() {
                        @Override
                        public void check(List<String> databaseObjects) {
                            for (String tableName : databaseObjects) {
                                assertWithMessage("Table " + tableName).that(tableName.length())
                                                                       .isLessThan(NAME_LENGTH_LIMIT + 1);
                            }
                        }
                    });
    }

    private void runAndCheck(final String sqlQuery, Constraint constraint) {
        SessionWork<List<String>> sessionWork = new SessionWork<List<String>>() {
            @Override
            public List<String> doInTransaction(Session session) {
                return session.createSQLQuery(sqlQuery).list();
            }
        };

        List<String> result = schedulerDbManager.executeReadOnlyTransaction(sessionWork);
        constraint.check(result);

        result = rmDbManager.executeReadTransaction(sessionWork);
        constraint.check(result);
    }

    private interface Constraint<T> {

        void check(List<T> databaseObjects);

    }

}
