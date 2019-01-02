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
package org.ow2.proactive.db.connectionpooling;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.apache.log4j.Logger;

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

    private static final Logger logger = Logger.getLogger(DBConnectionPoolsHolder.class);

    private final LoadingCache<DBConnectionDetails, HikariDataSource> dbConnectionPoolMap = CacheBuilder.newBuilder()
                                                                                                        .maximumSize(CACHE_MAXIMUM_SIZE)
                                                                                                        .expireAfterAccess(CACHE_EXPIRATION_MINUTES,
                                                                                                                           TimeUnit.MINUTES)
                                                                                                        .build(new CacheLoader<DBConnectionDetails, HikariDataSource>() {
                                                                                                            public HikariDataSource
                                                                                                                    load(DBConnectionDetails key)
                                                                                                                            throws SQLException {
                                                                                                                logger.info("New connection to an external DB is created " +
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
}
