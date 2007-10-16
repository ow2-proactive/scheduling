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
package org.objectweb.proactive.mpi.control.util;


/**
 * Provides functions to easily convert short, int float
 *  into/from byte[]
 * <UL>.
 * <LI> short -- 2 bytes
 * <LI> int -- 4 bytes
 * <LI> float -- 4 bytes
 * <LI> long -- 8 bytes
 * <LI> double -- 8 bytes
 * </UL>
 */
public class ProActiveMPIUtil {
    public static final int BYTE_LEN = 1;
    public static final int SHORT_LEN = 2;
    public static final int INT_LEN = 4;
    public static final int FLOAT_LEN = 4;
    public static final int LONG_LEN = 8;
    public static final int DOUBLE_LEN = 8;

    /** translate int into bytes, stored in byte array
     *starting from startIndex
     *@param num the integer to be translated
     *@param bytes[] the byte array
     *@param startIndex starting to store in this index
     *@ret the index of the cell after storing the number.
     */
    public static int intToBytes(int num, byte[] bytes, int startIndex) {
        bytes[startIndex] = (byte) (num & 0xff);
        bytes[startIndex + 1] = (byte) ((num >> 8) & 0xff);
        bytes[startIndex + 2] = (byte) ((num >> 16) & 0xff);
        bytes[startIndex + 3] = (byte) ((num >> 24) & 0xff);
        return startIndex + 4;
    }

    /** Given a byte array, restore it as an int
     * @param bytes the byte array
     * @param startIndex the starting index of the place the int is stored
     */
    public static int bytesToInt(byte[] bytes, int startIndex) {
        return (((int) bytes[startIndex] & 0xff) |
        (((int) bytes[startIndex + 1] & 0xff) << 8) |
        (((int) bytes[startIndex + 2] & 0xff) << 16) |
        (((int) bytes[startIndex + 3] & 0xff) << 24));
    }

    /** translate float into bytes, stored in byte array
     *starting from startIndex
     *@param num the float to be translated
     *@param bytes[] the byte array
     *@param startIndex starting to store in this index
     *@ret the index of the cell after storing the number.
     */
    public static int floatToBytes(float fnum, byte[] bytes, int startIndex) {
        return intToBytes(Float.floatToIntBits(fnum), bytes, startIndex);
    }

    /** Given a byte array, restore it as an float
     * @param bytes the byte array
     * @param startIndex the starting index of the place the int is stored
     */
    public static float bytesToFloat(byte[] bytes, int startIndex) {
        return (Float.intBitsToFloat(bytesToInt(bytes, startIndex)));
    }

    /** translate short into bytes, stored in byte array
     *starting from startIndex
     *@param num the short to be translated
     *@param bytes[] the byte array
     *@param startIndex starting to store in this index
     *@ret the index of the cell after storing the number.
     */
    public static int shortToBytes(short num, byte[] bytes, int startIndex) {
        bytes[startIndex] = (byte) (num & 0xff);
        bytes[startIndex + 1] = (byte) ((num >> 8) & 0xff);
        return startIndex + 2;
    }

    /** Given a byte array, restore it as a short
     * @param bytes the byte array
     * @param startIndex the starting index of the place the int is stored
     */
    public static short bytesToShort(byte[] bytes, int startIndex) {
        return (short) (((int) bytes[startIndex] & 0xff) |
        (((int) bytes[startIndex + 1] & 0xff) << 8));
    }

    /**
     * Give a String less than 255 bytes, store it as byte array, starting with
     * the length of the string.
     * If the length of the String is longer than 255, a warning is generated,
     * and the string will be truncated.
     *
     * @param str the string that is less than 255 bytes
     * @param bytes the byte array
     * @param startIndex the starting index where the string will be stored.
     * @ret the index of the array after storing this string
     */
    public static int stringToBytes(String str, byte[] bytes, int startIndex) {
        byte[] temp;
        int len = str.length();

        temp = str.getBytes();
        if (len > 255) {
            System.err.println(
                "String has more than 255 bytes in \"stringToBytes\", it will be truncated.");

            bytes[startIndex++] = (byte) 255;
            System.arraycopy(temp, 0, bytes, startIndex, 255);
            return startIndex + 255;
        } else {
            bytes[startIndex++] = (byte) len;
            System.arraycopy(temp, 0, bytes, startIndex, len);
            return startIndex + len;
        }
    }

    /**
     * Given a byte array, restore a String out of it.
     * the first cell stores the length of the String
     * @param bytes the byte array
     * @param startIndex the starting index where the string is stored, the first cell stores the length
     * @ret the string out of the byte array.
     */
    public static String bytesToString(byte[] bytes, int startIndex) {
        int len = (int) (bytes[startIndex++]) & 0xff;
        return new String(bytes, startIndex, len);
    }

    /**
     * Given a long, convert it into a byte array
     * @param lnum the long given to convert
     * @param bytes the bytes where to store the result
     * @param startIndex the starting index of the array where the result is stored.
     * @ret the index of the array after storing this long
     */
    public static int longToBytes(long lnum, byte[] bytes, int startIndex) {
        for (int i = 0; i < 8; i++)
            bytes[startIndex + i] = (byte) ((lnum >> (i * 8)) & 0xff);
        return startIndex + 8;
    }

    /**
     * Given an array of bytes, convert it to a long, least significant byte
     * is stored in the beginning.
     * @param bytes the byte array
     * @param startIndex the starting index of the array where the long is stored.
     * @ret the long result.
     */
    public static long bytesToLong(byte[] bytes, int startIndex) {
        // the lower 4 bytes
        //	long temp = (long)bytesToInt(bytes, startIndex) & (long)0xffffffff;
        //return temp | ((long)bytesToInt(bytes, startIndex+4) << 32);
        return (((long) bytes[startIndex] & 0xff) |
        (((long) bytes[startIndex + 1] & 0xff) << 8) |
        (((long) bytes[startIndex + 2] & 0xff) << 16) |
        (((long) bytes[startIndex + 3] & 0xff) << 24) |
        (((long) bytes[startIndex + 4] & 0xff) << 32) |
        (((long) bytes[startIndex + 5] & 0xff) << 40) |
        (((long) bytes[startIndex + 6] & 0xff) << 48) |
        (((long) bytes[startIndex + 7] & 0xff) << 56));
    }

    /**
     * Given a double, convert it into a byte array
     * @param dnum the double given to convert
     * @param bytes the bytes where to store the result
     * @param startIndex the starting index of the array where the result is stored.
     * @ret the index of the array after storing this double
     */
    public static int doubleToBytes(double dnum, byte[] bytes, int startIndex) {
        return longToBytes(Double.doubleToLongBits(dnum), bytes, startIndex);
    }

    /**
     * Given an array of bytes, convert it to a double, least significant byte
     * is stored in the beginning.
     * @param bytes the byte array
     * @param startIndex the starting index of the array where the long is stored.
     * @ret the double result.
     */
    public static double bytesToDouble(byte[] bytes, int startIndex) {
        return Double.longBitsToDouble(bytesToLong(bytes, startIndex));
    }
}
