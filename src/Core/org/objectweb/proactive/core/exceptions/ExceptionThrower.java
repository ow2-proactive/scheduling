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
package org.objectweb.proactive.core.exceptions;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;


/*
 * This is the nasty hack to bypass java checked exceptions
 */

/*
 * This interface is the entry-point to the thrower, an object implementing this
 * interface will be built by reflection. By calling the throwException() method
 * on this object, it will be possible to call the ASM generated code.
 */
interface Thrower {
    public void throwException(Throwable t);
}

public class ExceptionThrower {
    private static final String THROWER_CLASS_NAME = "TheActualExceptionThrower";
    private static final String THROWER_CLASS_PACKAGE = ExceptionThrower.class.getPackage().getName();
    private static final String THROWER_CLASS_FULLNAME = THROWER_CLASS_PACKAGE + "." + THROWER_CLASS_NAME;
    private static Thrower thrower = null;

    private static Class<?> loadClassJavassist() {
        try {
            CtClass throwerClass = ClassPool.getDefault().makeClass(THROWER_CLASS_FULLNAME);
            throwerClass.addInterface(ClassPool.getDefault().get(Thrower.class.getName()));
            throwerClass.addConstructor(CtNewConstructor.defaultConstructor(throwerClass));
            CtMethod throwException = CtNewMethod.make("" + "public void throwException(Throwable t) {"
                + "    throw t;}", throwerClass);
            throwerClass.addMethod(throwException);
            return loadClass(THROWER_CLASS_FULLNAME, throwerClass.toBytecode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* We load a class given its name and its binary representation */
    private static Class<?> loadClass(String className, byte[] b) throws Exception {
        Class<?> clazz = null;
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Class<?> cls = Class.forName("java.lang.ClassLoader");
        java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass", new Class<?>[] { String.class,
                byte[].class, int.class, int.class });

        /* protected method invocaton */
        method.setAccessible(true);
        try {
            Object[] args = new Object[] { className, b, new Integer(0), new Integer(b.length) };
            clazz = (Class<?>) method.invoke(loader, args);
        } finally {
            method.setAccessible(false);
        }
        return clazz;
    }

    /* The first time the mechanism is used, it has to initialize the thrower */
    private static void activate() {
        try {
            Class<?> clazz = loadClassJavassist();
            thrower = (Thrower) clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void throwException(Throwable t) {
        if (thrower == null) {
            activate();
        }

        if (thrower != null) {
            thrower.throwException(t);
        }
    }
}
