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
package org.objectweb.proactive.core.gc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;


/* Not used for the moment */
public class ReferencesTracer {
    private static void indentedPrint(int indentation, String str) {
        for (int i = 0; i < indentation; i++) {
            System.out.print(" ");
        }

        System.out.println(str);
    }

    private static Field[] getFields(Class<?> c) {
        Vector<Field> returnedFields = new Vector<Field>();
        for (Class<?> current = c; current != null; current = current.getSuperclass()) {
            try {
                Field[] fields = current.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    f.setAccessible(true);
                    returnedFields.add(f);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        Field[] array = new Field[returnedFields.size()];
        returnedFields.copyInto(array);
        return array;
    }

    private static void printAllReferences(int indentation, Collection<Object> items, Object o) {
        try {
            if ((o == null) || !items.add(o)) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Class<?> c = o.getClass();
        if (c.isArray()) {
            if (!(o instanceof Object[])) {

                /* int[] for example */
                return;
            }
            indentedPrint(indentation, "<array name=\"" + c.getSimpleName() + "\">");
            Object[] array = (Object[]) o;
            for (int i = 0; i < array.length; i++) {
                printAllReferences(indentation + 1, items, array[i]);
            }
            indentedPrint(indentation, "</array>");
        } else {
            indentedPrint(indentation, "<class name=\"" + c + "\">");
            Field[] fields = getFields(c);
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                int modifier = f.getModifiers();
                if (Modifier.isStatic(modifier) || f.getType().isPrimitive()) {
                    continue;
                }
                indentedPrint(indentation + 1, "<field class=\"" + f.getType() + "\" name=\"" + f.getName() +
                    "\">");
                try {
                    printAllReferences(indentation + 2, items, f.get(o));
                } catch (IllegalAccessException iae) {
                    iae.printStackTrace();
                }
                indentedPrint(indentation + 1, "</field>");
            }
            indentedPrint(indentation, "</class>");
        }
    }

    public static void printAllReferences(Object o) {
        printAllReferences(0, new HashSet<Object>(), o);
    }

    private static Class<?>[] getAllClasses() {
        Field f;
        try {
            f = ClassLoader.class.getDeclaredField("classes");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        f.setAccessible(true);
        Vector classes;
        try {
            classes = (Vector) f.get(ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return (Class<?>[]) classes.toArray(new Class<?>[0]);
    }

    private static Collection<Object> getStaticObjects(Class<?> c) {
        Field[] fields = getFields(c);
        Vector<Object> staticObjects = new Vector<Object>();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            int modifier = f.getModifiers();
            if (Modifier.isStatic(modifier) && !f.getType().isPrimitive()) {
                try {
                    staticObjects.add(f.get(null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return staticObjects;
    }

    private static Object[] getAllStaticObjects() {
        Class<?>[] classes = getAllClasses();
        Vector<Object> staticObjects = new Vector<Object>();
        for (int i = 0; i < classes.length; i++) {
            System.out.println(i + " classes");
            staticObjects.addAll(getStaticObjects(classes[i]));
        }
        return staticObjects.toArray();
    }

    public static void printAllReferences() {
        Object[] allStaticObjects = getAllStaticObjects();
        for (int i = 0; i < allStaticObjects.length; i++) {
            printAllReferences(allStaticObjects[i]);
        }
    }

    public static Collection<Object> getAllTypedReferences(Object root, Class<?> type) {
        return getAllTypedReferences(root, type, new HashSet<Object>());
    }

    private static Collection<Object> EMPTY_COLLECTION = new Vector<Object>();

    private static Collection<Object> getAllTypedReferences(Object o, Class<?> type, Collection<Object> items) {
        try {
            if ((o == null) || !items.add(o)) {
                return EMPTY_COLLECTION;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Class<?> c = o.getClass();
        Collection<Object> list = new LinkedList<Object>();
        if (c.isAssignableFrom(type)) {
            list.add(o);
        } else if (c.isArray()) {
            if (!(o instanceof Object[])) {

                /* int[] for example */
                return EMPTY_COLLECTION;
            }
            Object[] array = (Object[]) o;
            for (int i = 0; i < array.length; i++) {
                list.addAll(getAllTypedReferences(array[i], type, items));
            }
        } else {
            Field[] fields = getFields(c);
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                int mod = f.getModifiers();
                if (!Modifier.isStatic(mod) && !f.getType().isPrimitive()) {
                    try {
                        Collection<Object> refs;
                        refs = getAllTypedReferences(f.get(o), type, items);
                        list.addAll(refs);
                    } catch (IllegalAccessException iae) {
                        iae.printStackTrace();
                    }
                }
            }
        }

        return list;
    }
}
