/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.util.DatabaseManager;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;


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

    public static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.DATABASE);

    /**
     * Create the database using the given configuration file.
     *
     * @param configFile the file that have to be used to configure the database.
     */
    public static void createDataBase(String configFile) {
        Connection conn = null;
        Statement stmt = null;

        try {
            logger.debug("Try to start the database and connect it");
            conn = DatabaseManager.getInstance(configFile).connect(true);
            logger.info("Database started and connection granted");
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            stmt.execute("create table " + AbstractSchedulerDB.JOB_TABLE_NAME +
                "(jobid_hashcode INTEGER,job BLOB," + "jobevent BLOB, CONSTRAINT " +
                AbstractSchedulerDB.JOB_TABLE_NAME + "_PK PRIMARY KEY(jobid_hashcode))");

            stmt.execute("create table " + AbstractSchedulerDB.TASK_TABLE_NAME + "(" +
                "jobid_hashcode INTEGER, taskid_hashcode INTEGER, " +
                "taskevent BLOB, taskresult BLOB, ExecContainer BLOB, " + "precious SMALLINT, " +
                "CONSTRAINT " + AbstractSchedulerDB.TASK_TABLE_NAME +
                "_PK PRIMARY KEY(jobid_hashcode, taskid_hashcode)," + "CONSTRAINT " +
                AbstractSchedulerDB.TASK_TABLE_NAME + "_FK FOREIGN KEY (jobid_hashcode) REFERENCES " +
                AbstractSchedulerDB.JOB_TABLE_NAME + ")");

            logger.debug("Tables " + AbstractSchedulerDB.JOB_TABLE_NAME + " and " +
                AbstractSchedulerDB.TASK_TABLE_NAME + " created");
            conn.commit();
            logger.info("Committed successfully");
            stmt.close();
            conn.close();
            logger.debug("Connection closed");
        } catch (SQLException e) {
            while (e != null) {
                logger.debug(e.toString());
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
                    logger.info("Transaction rolled back !");
                } catch (SQLException e) {
                    // Nothing to do
                }

                // do not collapse these two "try-catch" because if
                // conn.rollback() failed
                // i want to try to close the connection ;-)
                try {
                    conn.close();
                    logger.debug("Connection closed");
                } catch (SQLException e) {
                    // Nothing to do
                }
            }

            if (DatabaseManager.getInstance(configFile).disconnect()) {
                logger.debug("Database shut down normally");
            } else {
                logger.debug("Database shut down with problems");
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
            logger.info("Missing config file for database !");
        }
    }
}
