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
package org.ow2.proactive.resourcemanager.node.jmx;

import java.sql.*;

import org.hyperic.sigar.SigarException;


/**
 * Created by brian on 22/05/2017.
 */
public class SigarDatabaseEvents implements SigarDatabaseEventxMXBean {
    private static Connection connection;

    private String dbUrl;

    private String dbUser;

    private String dbPassword;

    private String dbTable;

    public SigarDatabaseEvents(String dbUrl, String dbUser, String dbPassword, String dbTable) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbTable = dbTable;
    }

    public Connection getConnection(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        }
        return connection;
    }

    @Override
    public int getRowsCount() throws SigarException {
        try {
            Statement stmt = getConnection(dbUrl, dbUser, dbPassword).createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT COUNT(*) FROM " + dbTable);
            resultSet.next();
            int count = resultSet.getInt(1);
            stmt.close();
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            // TODO            connection.close();
        }
        return -1;
    }
}
