/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.db.types;

import com.google.common.collect.ImmutableSet;
import org.hibernate.type.SerializableToBlobType;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is in charge of converting old blob types to the correct object
 * type used with the current version of the product. It is there for backward
 * compatibility only.
 */
public class PatternType extends SerializableToBlobType {

    @Override
    public Object get(ResultSet rs, String name) throws SQLException {
        Object result = super.get(rs, name);

        // in the past includes and excludes patterns
        // were stored as String[]
        if (result instanceof String[]) {
            return ImmutableSet.copyOf((String[]) result);
        }

        return result;
    }

}
