/***
 * Julia: France Telecom's implementation of the Fractal API
 * Copyright (C) 2001-2002 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */
package functionalTests.component.conform.components;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.api.control.BindingController;


public class C implements CAttributes, BindingController, I, J {
    private boolean x1;
    private byte x2;
    private char x3;
    private short x4;
    private int x5;
    private long x6;
    private float x7;
    private double x8;
    private String x9;
    private boolean x11;
    private I i;
    private Map<String, Object> j = new HashMap<String, Object>();

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

    public void setWriteOnlyX11(boolean x11) {
        this.x11 = x11;
    }

    // BINDING CONTROLLER
    public String[] listFc() {
        String[] result = new String[j.size() + 1];
        j.keySet().toArray(result);
        result[j.size()] = "client";
        return result;
    }

    public Object lookupFc(String s) {
        if (s.equals("client")) {
            return i;
        } else if (s.startsWith("clients")) {
            return j.get(s);
        }
        return null;
    }

    public void bindFc(String s, Object o) {
        if (s.equals("client")) {
            i = (I) o;
        } else if (s.startsWith("clients")) {
            j.put(s, o);
        }
    }

    public void unbindFc(String s) {
        if (s.equals("client")) {
            i = null;
        } else if (s.startsWith("clients")) {
            j.remove(s);
        }
    }

    // FUNCTIONAL INTERFACE
    public void m(boolean v) {
    }

    public void m(byte v) {
    }

    public void m(char v) {
    }

    public void m(short v) {
    }

    public void m(int v) {
    }

    public void m(long v) {
    }

    public void m(float v) {
    }

    public void m(double v) {
    }

    public void m(String v) {
    }

    public void m(String[] v) {
    }

    public boolean n(boolean v, String[] w) {
        return v | x11; // for write only attribute tests
    }

    public byte n(byte v, String w) {
        return v;
    }

    public char n(char v, double w) {
        return v;
    }

    public short n(short v, float w) {
        return v;
    }

    public int n(int v, long w) {
        if (i != null) {
            // for interceptors tests
            return (w == 0) ? v : i.n(v + 1, w - 1);
        } else if (j.size() > 0) {
            // for interceptors tests
            return (w == 0) ? v : ((I) j.values().iterator().next()).n(v + 1, w - 1);
        } else {
            return v;
        }
    }

    public long n(long v, int w) {
        return v;
    }

    public float n(float v, short w) {
        return v;
    }

    public double n(double v, char w) {
        return v;
    }

    public String n(String v, byte w) {
        return v;
    }

    public String[] n(String[] v, boolean w) {
        return v;
    }
}
