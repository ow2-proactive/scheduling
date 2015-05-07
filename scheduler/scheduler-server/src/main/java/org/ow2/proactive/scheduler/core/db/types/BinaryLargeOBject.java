/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;


/**
 * Hibernate natively maps blob columns to java.sql.Blob.<br />
 * However, it's sometimes useful to read the whole blob into memory and deal with it as a byte array.<br />
 * BinaryLargeOBject is made to fix this issue.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class BinaryLargeOBject implements UserType {

    public static final Logger logger = Logger.getLogger(BinaryLargeOBject.class);

    /**
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return new int[] { Types.BLOB };
    }

    /**
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    public Class<?> returnedClass() {
        return byte[].class;
    }

    /**
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    public boolean equals(Object x, Object y) {
        return (x == y) || (x != null && y != null && java.util.Arrays.equals((byte[]) x, (byte[]) y));
    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
     */
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor s, Object owner)
            throws HibernateException, SQLException {
        /*Blob blob = rs.getBlob(names[0]);
        return deserialize(blob);*/
        byte[] bytes = rs.getBytes(names[0]);
        try {
            return ByteToObjectConverter.ObjectStream.convert(bytes);
        } catch (Exception e) {
            throw new HibernateException("Cannot get byteArray", e);
        }

    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor s)
            throws HibernateException, SQLException {
        try {
            st.setBytes(index, ObjectToByteConverter.ObjectStream.convert(value));
        } catch (Exception e) {
            throw new HibernateException("Cannot set byteArray", e);
        }
        //st.setBlob(index, serialize(value));
    }

    /**
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    public Object deepCopy(Object value) {
        if (value == null)
            return null;

        byte[] bytes = (byte[]) value;
        byte[] result = new byte[bytes.length];
        System.arraycopy(bytes, 0, result, 0, bytes.length);

        return result;
    }

    /**
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    public boolean isMutable() {
        return true;
    }

    /**
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    public Object assemble(Serializable arg0, Object obj) throws HibernateException {
        //not implemented : not used
        return null;
    }

    /**
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    public Serializable disassemble(Object obj) throws HibernateException {
        //not implemented : not used
        return null;
    }

    /**
     * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
     */
    public int hashCode(Object obj) throws HibernateException {
        //not implemented : not used
        return obj.hashCode();
    }

    /**
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        //not implemented : not used
        return null;
    }

    /*private Blob serialize(Object o) {
        try {
            return new SerialBlob(ObjectToByteConverter.ObjectStream.convert(o));
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    private Object deserialize(Blob blob) {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(blob.getBinaryStream());
            Object o = ois.readObject();
            return o;
        } catch (Exception e) {
            logger.error("", e);
            return null;
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    // Nothing to do
                }
            }
        }
    }*/

}
