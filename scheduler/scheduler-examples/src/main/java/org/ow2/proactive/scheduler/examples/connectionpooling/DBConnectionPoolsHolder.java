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
package org.ow2.proactive.scheduler.examples.connectionpooling;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.zaxxer.hikari.HikariDataSource;


/**
 * @author ActiveEon Team
 * @since 28/12/2018
 */
public class DBConnectionPoolsHolder {

    private static final int CACHE_MAXIMUM_SIZE = Integer.parseInt(System.getProperty("external.db.pool.cache.maximum.size",
                                                                                      "10"));

    private static final int CACHE_EXPIRATION_MINUTES = Integer.parseInt(System.getProperty("external.db.pool.cache.expiration.minutes",
                                                                                            "10"));

    private DBConnectionPoolsHolder() {
    }

    private final LoadingCache<DBConnectionDetails, HikariDataSource> dbConnectionPoolMap = CacheBuilder.newBuilder()
                                                                                                        .maximumSize(CACHE_MAXIMUM_SIZE)
                                                                                                        .expireAfterAccess(CACHE_EXPIRATION_MINUTES,
                                                                                                                           TimeUnit.MINUTES)
                                                                                                        .build(new CacheLoader<DBConnectionDetails, HikariDataSource>() {
                                                                                                            public HikariDataSource
                                                                                                                    load(DBConnectionDetails key)
                                                                                                                            throws SQLException {
                                                                                                                System.out.println("New connection to an external DB is created " +
                                                                                                                                   key.toString());
                                                                                                                return dbConnectionPoolFactory.generateConnectionPool(key);
                                                                                                            }
                                                                                                        });

    private final DBConnectionPoolFactory dbConnectionPoolFactory = new DBConnectionPoolFactory();

    private static final DBConnectionPoolsHolder INSTANCE = new DBConnectionPoolsHolder();

    public static DBConnectionPoolsHolder getInstance() {
        return INSTANCE;
    }

    /**
     *
     * @param dbConnectionDetails
     * @param queryStr
     * @return a cached result set for a given query to an external DB
     */

    public ResultSet executeQuery(DBConnectionDetails dbConnectionDetails, String queryStr) {
        ResultSet resultSet = null;
        try (Connection pooledConnection = dbConnectionPoolMap.get(dbConnectionDetails).getConnection();
                Statement stmt = pooledConnection.createStatement()) {
            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            resultSet = stmt.executeQuery(queryStr);
            crs.populate(resultSet);
            return crs;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to execute query: " + queryStr, e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     *
     * @param dbConnectionDetails
     * @param updateStr
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     *         or (2) 0 for SQL statements that return nothing
     */
    public int executeUpdate(DBConnectionDetails dbConnectionDetails, String updateStr) {
        try (Connection connection = dbConnectionPoolMap.get(dbConnectionDetails).getConnection();
                Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(updateStr);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to execute update: " + updateStr, e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a stored procedure query with the given name and parameters.
     *
     * This store procedure must return a set of row results
     *
     * @param dbConnectionDetails The connection details for the database.
     * @param storedProcName The name of the stored procedure.
     * @param params The parameters to be passed to the stored procedure.
     * @return A CachedRowSet containing the result set of the stored procedure execution.
     */
    public CachedRowSet executeStoredProcedure(DBConnectionDetails dbConnectionDetails, String storedProcName,
            Object... params) {
        try (Connection connection = dbConnectionPoolMap.get(dbConnectionDetails).getConnection()) {
            // Construct the stored procedure call string with placeholders using Java 8 features
            String paramPlaceholders = String.join(", ", Collections.nCopies(params.length, "?"));
            String callString = "{call " + storedProcName + "(" + paramPlaceholders + ")}";

            try (CallableStatement callableStmt = connection.prepareCall(callString)) {
                // Set parameters for the CallableStatement using IntStream
                IntStream.range(0, params.length).forEach(i -> {
                    try {
                        callableStmt.setObject(i + 1, params[i]); // Ensure index starts from 1
                    } catch (SQLException e) {
                        throw new RuntimeException("Error setting parameter at index " + (i + 1), e);
                    }
                });

                // Execute the stored procedure and handle the result set
                ResultSet resultSet = callableStmt.executeQuery();
                CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
                crs.populate(resultSet);
                return crs;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to execute stored procedure: " + storedProcName, e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a stored procedure update with the given name and parameters.
     *
     * This call returns an integer containing the number of rows updated or -1 if no table was updated.
     *
     * @param dbConnectionDetails The connection details for the database.
     * @param storedProcName The name of the stored procedure.
     * @param params The parameters to be passed to the stored procedure.
     * @return The number of rows modified by this update
     */
    public int executeStoredProcedureUpdate(DBConnectionDetails dbConnectionDetails, String storedProcName,
            Object... params) {
        try (Connection connection = dbConnectionPoolMap.get(dbConnectionDetails).getConnection()) {
            // Construct the stored procedure call string with placeholders using Java 8 features
            String paramPlaceholders = String.join(", ", Collections.nCopies(params.length, "?"));
            String callString = "{call " + storedProcName + "(" + paramPlaceholders + ")}";

            try (CallableStatement callableStmt = connection.prepareCall(callString)) {
                // Set parameters for the CallableStatement using IntStream
                IntStream.range(0, params.length).forEach(i -> {
                    try {
                        callableStmt.setObject(i + 1, params[i]); // Ensure index starts from 1
                    } catch (SQLException e) {
                        throw new RuntimeException("Error setting parameter at index " + (i + 1), e);
                    }
                });

                // Execute the stored procedure and return the result
                return callableStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to execute stored procedure: " + storedProcName, e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
