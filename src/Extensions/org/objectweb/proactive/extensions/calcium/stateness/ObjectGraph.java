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
package org.objectweb.proactive.extensions.calcium.stateness;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;


public class ObjectGraph {
    static public <T> Object searchForClass(Object root, Handler<T> handler) throws Exception {
        IdentityHashMap<Object, Object> visited = new IdentityHashMap<Object, Object>();
        IdentityHashMap<Object, ArrayList<Reference>> found = new IdentityHashMap<Object, ArrayList<Reference>>();

        searchForClass(root, handler, visited, found);

        Iterator<Object> it = found.keySet().iterator();
        while (it.hasNext()) {
            Object referenced = it.next();
            ArrayList<Reference> list = found.get(referenced);

            Object transformed = handler.transform((T) referenced);

            for (Reference ref : list) {
                ref.updateReferenceWith(transformed);
            }
        }

        if (handler.matches(root)) {
            root = handler.transform((T) root);
        }

        return root;
    }

    static private <T> void searchForClass(Object parent, Handler<T> handler,
            IdentityHashMap<Object, Object> visited, IdentityHashMap<Object, ArrayList<Reference>> found)
            throws Exception {
        if (visited.containsKey(parent)) {
            return; //already visited (this is a loop)
        }
        visited.put(parent, parent); //mark as visited

        ArrayList<Field> field = getAllInheritedFields(parent.getClass());

        for (Field f : field) {
            Class<?> c = f.getType();

            // primitives & static vars are not considered
            if (c.isPrimitive() || Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            f.setAccessible(true);
            Object fieldObject = f.get(parent);

            if ((fieldObject == null) || visited.containsKey(fieldObject)) {
                continue; // already visited (this a loop)
            }

            //If its an array of T[], where T is not primitive
            //(ex: int[] is not assignable to Object[])
            if (new Object[0].getClass().isAssignableFrom(fieldObject.getClass())) {
                Object[] array = (Object[]) fieldObject;

                for (int i = 0; i < array.length; i++) {
                    if (array[i] == null) {
                        continue;
                    }

                    if (handler.matches(array[i])) {
                        array[i] = handler.transform((T) array[i]);
                        //visited.put(array[i], array[i]);
                    }

                    searchForClass(array[i], handler, visited, found);
                }

                f.set(parent, array);
            } else { // regular object, search recursively
                if (handler.matches(fieldObject)) {
                    if (!found.containsKey(fieldObject)) {
                        found.put(fieldObject, new ArrayList<Reference>());
                    }

                    ArrayList<Reference> list = found.get(fieldObject);
                    list.add(new Reference(parent, f));
                    //visited.put(fieldObject, fieldObject);
                }

                searchForClass(fieldObject, handler, visited, found);
            }
        }
    }

    /**
     * @param cls A Class.
     * @return All the Fields of this class, including the inherited ones.
     */
    static public ArrayList<Field> getAllInheritedFields(Class<?> cls) {
        ArrayList<Field> array = new ArrayList<Field>();

        // Reached the top of the recurtion. This class is stateless.
        if ((cls == null) || (cls == Object.class)) {
            return array;
        }

        Field[] declared = cls.getDeclaredFields();

        for (Field d : declared) {
            array.add(d);
        }

        array.addAll(getAllInheritedFields(cls.getSuperclass()));

        return array;
    }

    static class Reference {
        Object referencer;
        Field f;

        public Reference(Object parent, Field f) {
            this.referencer = parent;
            this.f = f;
        }

        public void updateReferenceWith(Object o) throws IllegalArgumentException, IllegalAccessException {
            f.set(referencer, o);
        }
    }
}
