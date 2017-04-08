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

import java.sql.Types;


/**
 * Hibernate natively maps clob columns to java.sql.Clob.
 * However, it's sometimes useful to read the whole clob into memory and deal with it as a String array.
 * CharacterLargeOBject is made to fix this issue.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class CharacterLargeOBject extends BinaryLargeOBject {

    /**
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    @Override
    public int[] sqlTypes() {
        return new int[] { Types.BLOB };
    }

    /**
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    @Override
    public Class<?> returnedClass() {
        return String[].class;
    }

    /**
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean equals(Object x, Object y) {
        return (x == y) || (x != null && y != null && java.util.Arrays.equals((String[]) x, (String[]) y));
    }

    /**
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    @Override
    public Object deepCopy(Object value) {
        if (value == null)
            return null;

        String[] strs = (String[]) value;
        String[] result = new String[strs.length];
        System.arraycopy(strs, 0, result, 0, strs.length);

        return result;
    }

}
