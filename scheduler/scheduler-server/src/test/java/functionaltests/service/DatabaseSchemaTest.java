package functionaltests.service;

import java.util.List;

import org.ow2.proactive.db.SessionWork;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.google.common.truth.Truth.assertWithMessage;

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
        runAndCheck(
                "SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC'",
                new Constraint<Object[]>() {
                    @Override
                    public void check(List<Object[]> databaseObjects) {
                        for (Object[] columns : databaseObjects) {
                            String tableName = (String) columns[0];
                            String columnName = (String) columns[1];

                            assertWithMessage("Table " + tableName + ", Column " + columnName)
                                    .that(tableName.length()).isLessThan(NAME_LENGTH_LIMIT + 1);
                        }
                    }
                });
    }

    @Test
    public void testIndexNamesShouldNotExceedLengthLimit() {
        runAndCheck(
                "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.SYSTEM_INDEXINFO " +
                        "WHERE TABLE_SCHEM = 'PUBLIC' AND INDEX_NAME NOT LIKE 'SYS%'",
                new Constraint<String>() {
                    @Override
                    public void check(List<String> databaseObjects) {
                        for (String indexName : databaseObjects) {
                            assertWithMessage("Index " + indexName)
                                    .that(indexName.length()).isLessThan(NAME_LENGTH_LIMIT + 1);
                        }
                    }
                });
    }

    @Test
    public void testTableNamesShouldNotExceedLengthLimit() {
        runAndCheck(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_TYPE = 'TABLE'",
                new Constraint<String>() {
                    @Override
                    public void check(List<String> databaseObjects) {
                        for (String tableName : databaseObjects) {
                            assertWithMessage("Table " + tableName)
                                    .that(tableName.length()).isLessThan(NAME_LENGTH_LIMIT + 1);
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
