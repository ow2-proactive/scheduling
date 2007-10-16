/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.examples.webservices.c3dWS.ws;

public class ArrayOfInt implements java.io.Serializable {
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(ArrayOfInt.class,
            true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
                "http://tempuri.org/", "ArrayOfInt"));

        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_int");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://tempuri.org/", "int"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
    }

    private int[] _int;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public ArrayOfInt() {
    }

    /**
     * Gets the _int value for this ArrayOfInt.
     *
     * @return _int
     */
    public int[] get_int() {
        return _int;
    }

    /**
     * Sets the _int value for this ArrayOfInt.
     *
     * @param _int
     */
    public void set_int(int[] _int) {
        this._int = _int;
    }

    public int get_int(int i) {
        return this._int[i];
    }

    public void set_int(int i, int value) {
        this._int[i] = value;
    }

    @Override
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ArrayOfInt)) {
            return false;
        }

        ArrayOfInt other = (ArrayOfInt) obj;

        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }

        __equalsCalc = obj;

        boolean _equals;
        _equals = true &&
            (((this._int == null) && (other.get_int() == null)) ||
            ((this._int != null) &&
            java.util.Arrays.equals(this._int, other.get_int())));
        __equalsCalc = null;

        return _equals;
    }

    @Override
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }

        __hashCodeCalc = true;

        int _hashCode = 1;

        if (get_int() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(get_int());
                    i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(get_int(), i);

                if ((obj != null) && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }

        __hashCodeCalc = false;

        return _hashCode;
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
        java.lang.String mechType, java.lang.Class<?> _javaType,
        javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanSerializer(_javaType,
            _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
        java.lang.String mechType, java.lang.Class<?> _javaType,
        javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType,
            _xmlType, typeDesc);
    }
}
