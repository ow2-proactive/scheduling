package org.objectweb.proactive.extra.scheduler.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * This class "try" to create a new database with two new table
 * (JOB_AND_JOB_EVENTS and TASK_EVENTS_AND_TASK_RESULTS) Why "try" ? because it
 * can failed the creation if the database already exist, or if hasn't the right
 * permissions..
 *
 * @author FRADJ Johann
 */
public class CreateDataBase {
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try {
            System.out.println("Try to start the database and connect it");
            conn = DatabaseManager.getInstance().connect(true);
            System.out.println("Database started and connection granted");
            conn.setAutoCommit(false);

            stmt = conn.createStatement();

            stmt.execute(
                "create table JOB_AND_JOB_EVENTS(jobid_hashcode INTEGER,job BLOB," +
                "jobevent BLOB,CONSTRAINT JOB_AND_JOB_EVENTS_PK PRIMARY KEY(jobid_hashcode))");

            stmt.execute("create table TASK_EVENTS_AND_TASK_RESULTS(" +
                "jobid_hashcode INTEGER, taskid_hashcode INTEGER, " +
                "taskevent BLOB, taskresult BLOB," +
                "CONSTRAINT TASK_EVENTS_AND_TASK_RESULTS_PK PRIMARY KEY(jobid_hashcode, taskid_hashcode)," +
                "CONSTRAINT TASK_EVENTS_AND_TASK_RESULTS_FK FOREIGN KEY (jobid_hashcode) REFERENCES JOB_AND_JOB_EVENTS)");

            System.out.println(
                "Tables JOB_AND_JOB_EVENTS and TASK_EVENTS_AND_TASK_RESULTS created");
            conn.commit();
            System.out.println("Committed successfully");
            stmt.close();
            conn.close();
            System.out.println("Connection closed");
        } catch (SQLException e) {
            while (e != null) {
                System.out.println(e.toString());
                e = e.getNextException();
            }
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // Nothing to do
                }
            }
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back !");
                } catch (SQLException e) {
                    // Nothing to do
                }

                // do not collapse these two "try-catch" because if
                // conn.rollback() failed
                // i want to try to close the connection ;-)
                try {
                    conn.close();
                    System.out.println("Connection closed");
                } catch (SQLException e) {
                    // Nothing to do
                }
            }
            if (DatabaseManager.getInstance().disconnect()) {
                System.out.println("Database shut down normally");
            } else {
                System.out.println("Database shut down with problems");
            }
        }
    }
}
