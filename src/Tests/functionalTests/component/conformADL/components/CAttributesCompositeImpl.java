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
package functionalTests.component.conformADL.components;

public class CAttributesCompositeImpl implements CAttributes {
    private boolean x1;
    private byte x2;
    private char x3;
    private short x4;
    private int x5;
    private long x6;
    private float x7;
    private double x8;
    private String x9;

    // ATTRIBUTE CONTROLLER
    public boolean getX1() {
        return x1;
    }

    public void setX1(boolean x1) {
        this.x1 = x1;
    }

    public byte getX2() {
        return x2;
    }

    public void setX2(byte x2) {
        this.x2 = x2;
    }

    public char getX3() {
        return x3;
    }

    public void setX3(char x3) {
        this.x3 = x3;
    }

    public short getX4() {
        return x4;
    }

    public void setX4(short x4) {
        this.x4 = x4;
    }

    public int getX5() {
        return x5;
    }

    public void setX5(int x5) {
        this.x5 = x5;
    }

    public long getX6() {
        return x6;
    }

    public void setX6(long x6) {
        this.x6 = x6;
    }

    public float getX7() {
        return x7;
    }

    public void setX7(float x7) {
        this.x7 = x7;
    }

    public double getX8() {
        return x8;
    }

    public void setX8(double x8) {
        this.x8 = x8;
    }

    public String getX9() {
        return x9;
    }

    public void setX9(String x9) {
        this.x9 = x9;
    }

    public boolean getReadOnlyX10() {
        return true;
    }
}
