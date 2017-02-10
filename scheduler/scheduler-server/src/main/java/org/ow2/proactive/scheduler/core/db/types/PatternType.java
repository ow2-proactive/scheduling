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
package org.ow2.proactive.scheduler.core.db.types;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.SerializableToBlobType;

import com.google.common.collect.ImmutableSet;


/**
 * This class is in charge of converting old blob types to the correct object
 * type used with the current version of the product. It is there for backward
 * compatibility only.
 */
public class PatternType extends SerializableToBlobType {

    @Override
    public Object get(ResultSet rs, String name, SessionImplementor implementor) throws SQLException {
        Object result = super.get(rs, name, implementor);

        // in the past includes and excludes patterns
        // were stored as String[]
        if (result instanceof String[]) {
            return ImmutableSet.copyOf((String[]) result);
        }

        return result;
    }

}
