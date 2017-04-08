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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.type.SerializableToBlobType;


/**
 * Map an empty Map to a null column to avoid fetching BLOBs that are empty Maps.
 * <br>
 * <b>Warning</b>: when reading back the value, a null value will be returned if an empty Map was stored.
 */
public class NonEmptyMapToBlobType extends SerializableToBlobType {

    @Override
    public boolean[] toColumnNullness(Object value, Mapping mapping) {
        if (value instanceof Map && ((Map) value).isEmpty()) {
            return ArrayHelper.FALSE;
        }
        return super.toColumnNullness(value, mapping);
    }

    @Override
    public void set(PreparedStatement st, Object value, int index, SessionImplementor session) throws SQLException {
        if (value instanceof Map && ((Map) value).isEmpty()) {
            st.setNull(index, sqlTypes(null)[0]);
        } else {
            super.set(st, value, index, session);
        }
    }
}
