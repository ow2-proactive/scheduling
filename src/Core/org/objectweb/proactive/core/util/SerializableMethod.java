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
package org.objectweb.proactive.core.util;

import java.io.Serializable;
import java.lang.reflect.Method;


public class SerializableMethod implements Serializable {
    private transient Method m;

    public SerializableMethod(Method m) {
        this.m = m;
    }

    public Method getMethod() {
        return m;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        System.out.println("writing WrappedMethod");
        out.writeObject(m.getDeclaringClass());
        out.writeObject(m.getName());
        out.writeObject(m.getParameterTypes());
    }

    //    private Class[] fixBugRead(FixWrapper[] para) {
    //        Class[] tmp = new Class[para.length];
    //        for (int i = 0; i < para.length; i++) {
    //            //	System.out.println("fixBugRead for " + i + " value is " +para[i]);
    //            tmp[i] = para[i].getWrapped();
    //        }
    //        return tmp;
    //    }
    //
    //    private FixWrapper[] fixBugWrite(Class[] para) {
    //        FixWrapper[] tmp = new FixWrapper[para.length];
    //        for (int i = 0; i < para.length; i++) {
    //            //	System.out.println("fixBugWrite for " + i + " out of " + para.length + " value is " +para[i] );	
    //            tmp[i] = new MethodCall.FixWrapper(para[i]);
    //        }
    //        return tmp;
    //    }
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        System.out.println("reading WrappedMethod");
        Class<?> declaringClass = (Class<?>) in.readObject();
        String name = (String) in.readObject();
        Class<?>[] paramTypes = (Class<?>[]) in.readObject();

        try {
            m = declaringClass.getMethod(name, paramTypes);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int hashCode() {
        return m.hashCode();
    }

    @Override
    public String toString() {
        return m.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SerializableMethod)) {
            return false;
        }
        return m.equals(((SerializableMethod) obj).getMethod());
    }
}
