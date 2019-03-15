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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * @author ActiveEon Team
 * @since 28/12/2018
 */

@PublicAPI
public class DBConnectionDetails implements Serializable {

    public static class Builder {

        private String jdbcUrl;

        private String username;

        private String password;

        private Map<String, String> properties;

        public Builder() {
            properties = new HashMap();
        }

        public Builder jdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder addDataSourceProperty(String key, String value) {
            this.properties.put(key, value);
            return this;
        }

        public DBConnectionDetails build() {
            return new DBConnectionDetails(jdbcUrl, username, password, properties);
        }
    }

    private final String jdbcUrl;

    private final String username;

    final String password;

    private final Map properties;

    private DBConnectionDetails(String jdbcUrl, String username, String password, Map properties) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.properties = properties;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public Map getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DBConnectionDetails that = (DBConnectionDetails) o;

        if (!jdbcUrl.equals(that.jdbcUrl))
            return false;
        if (!username.equals(that.username))
            return false;
        return properties != null ? properties.equals(that.properties) : that.properties == null;
    }

    @Override
    public int hashCode() {
        int result = jdbcUrl.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBConnectionDetails{");
        sb.append("jdbcUrl='").append(jdbcUrl).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }
}
