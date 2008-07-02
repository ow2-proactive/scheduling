/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.ow2.proactive.scheduler.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB;


/**
 * This class "try" to create a new database with two new table
 * (JOB_AND_JOB_EVENTS and TASK_EVENTS_AND_TASK_RESULTS) Why "try" ? because the
 * creation can failed if the database already exist, or if hasn't the right
 * permissions..
 *
 *
 * @author The ProActive Team
 */
public class CreateDataBase {

    /**
     * Create the database using the given configuration file.
     *
     * @param configFile the file that have to be used to configure the database.
     */
    public static void createDataBase(String configFile) {
        Connection conn = null;
        Statement stmt = null;

        try {
            System.out.println("Try to start the database and connect it");
            conn = DatabaseManager.getInstance(configFile).connect(true);
            System.out.println("Database started and connection granted");
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            stmt.execute("create table " + AbstractSchedulerDB.JOB_TABLE_NAME +
                "(jobid_hashcode INTEGER,job BLOB," + "jobevent BLOB, CONSTRAINT " +
                AbstractSchedulerDB.JOB_TABLE_NAME + "_PK PRIMARY KEY(jobid_hashcode))");

            stmt.execute("create table " + AbstractSchedulerDB.TASK_TABLE_NAME + "(" +
                "jobid_hashcode INTEGER, taskid_hashcode INTEGER, " + "taskevent BLOB, taskresult BLOB, " +
                "precious SMALLINT, " + "CONSTRAINT " + AbstractSchedulerDB.TASK_TABLE_NAME +
                "_PK PRIMARY KEY(jobid_hashcode, taskid_hashcode)," + "CONSTRAINT " +
                AbstractSchedulerDB.TASK_TABLE_NAME + "_FK FOREIGN KEY (jobid_hashcode) REFERENCES " +
                AbstractSchedulerDB.JOB_TABLE_NAME + ")");

            System.out.println("Tables " + AbstractSchedulerDB.JOB_TABLE_NAME + " and " +
                AbstractSchedulerDB.TASK_TABLE_NAME + " created");
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

            if (DatabaseManager.getInstance(configFile).disconnect()) {
                System.out.println("Database shut down normally");
            } else {
                System.out.println("Database shut down with problems");
            }
        }
    }

    /**
     * Start the creation process.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            createDataBase(args[0]);
        } else {
            System.err.println("Missing config file for database !");
        }
    }
}
