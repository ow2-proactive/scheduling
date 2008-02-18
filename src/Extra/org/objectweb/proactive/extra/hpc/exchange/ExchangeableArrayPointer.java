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
package org.objectweb.proactive.extra.hpc.exchange;

public class ExchangeableArrayPointer {
    // Array data type definitions
    public static final int BYTE_ARRAY = 0;
    public static final int DOUBLE_ARRAY = 1;
    public static final int INT_ARRAY = 2;
    public static final int EXCHANGEABLE_DOUBLE = 3;

    // Private data
    private byte[] byteArray;
    private double[] doubleArray;
    private int[] intArray;
    private int offset;
    private int len;
    private int dataType;
    private ExchangeableDouble doubleStructure;

    public ExchangeableArrayPointer(byte[] array, int offset, int len) {
        this(offset, len, BYTE_ARRAY);
        this.byteArray = array;
    }

    public ExchangeableArrayPointer(double[] array, int offset, int len) {
        this(offset, len, DOUBLE_ARRAY);
        this.doubleArray = array;
    }

    public ExchangeableArrayPointer(int[] array, int offset, int len) {
        this(offset, len, INT_ARRAY);
    }

    public ExchangeableArrayPointer(ExchangeableDouble structure) {
        this.doubleStructure = structure;
        this.dataType = EXCHANGEABLE_DOUBLE;
    }

    private ExchangeableArrayPointer(int offset, int len, int dataType) {
        this.offset = offset;
        this.len = len;
        this.dataType = dataType;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public double[] getDoubleArray() {
        return doubleArray;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public int getOffset() {
        return offset;
    }

    public int getLenArray() {
        return len;
    }

    public int getDataType() {
        return dataType;
    }

    public ExchangeableDouble getExchangeDouble() {
        return this.doubleStructure;
    }
}
