/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.stateness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.examples.nqueens.Board;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.ConquerBoard;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.DivideCondition;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.Result;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt1.DivideBT1;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt1.SolveBT1;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt2.DivideBT2;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt2.SolveBT2;
import org.objectweb.proactive.extensions.calcium.muscle.Condition;
import org.objectweb.proactive.extensions.calcium.muscle.Muscle;
import org.objectweb.proactive.extensions.calcium.skeletons.DaC;
import org.objectweb.proactive.extensions.calcium.skeletons.Fork;
import org.objectweb.proactive.extensions.calcium.skeletons.Seq;
import org.objectweb.proactive.extensions.calcium.skeletons.Skeleton;


public class Stateness {

    /**
     * @see isStateFull(java.lang.Class)
     */
    static public boolean isStateFul(Object o) {
        Class<?> cls = o.getClass();

        return isStateFul(cls);
    }

    /**
     * This method groups objects. A group of Objects corresponds to: all the
     * objects that are linked by other object references.
     *
     * That is to say, that for any two objects (a,b) in a group, there exists a
     * path in the (undirected) reference graph from a to b.
     *
     * Ex: {A.x, B.x, B.y, C.y, D.o, E.p, o.q, p.q}
     *
     * yields => {A, B, C} and {D, E}
     *
     * A and C are linked through B: A->x->B->y->C D and E are linked through q:
     * D->o->q<-p<-E
     *
     * @param list
     *            The list of objects that are going to be grouped (graph entry
     *            points).
     * @return A list of groups, holding the entry points objects.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    static public <T> Collection<Collection<T>> getReferenceGroups(
        Collection<T> list)
        throws IllegalArgumentException, IllegalAccessException {
        Collection<Collection<T>> groups = new ArrayList<Collection<T>>();

        for (T muscle : list) {
            boolean newGroupRequired = true;
            for (Collection<T> g : groups) {
                if (shareStateWithGroup(g, muscle)) {
                    g.add(muscle);
                    newGroupRequired = false;
                    break;
                }
            }
            if (newGroupRequired) {
                List<T> newGroup = new ArrayList<T>();
                newGroup.add(muscle);
                groups.add(newGroup);
            }
        }

        return groups;
    }

    /**
     * A class A is stateful when any of the following three conditions are met:
     *
     * 1. If A is annotated as StateFul.value==true. 2. If A has declared
     * non-static fields AND the corresponding class is not annotated as
     * StateFul.value==false. 3. Any of A's super classes meets conditions 1 or
     * 2.
     *
     * @param cls
     *            The class to verify.
     * @return true if the muscle code is stateless, false otherwise.
     */
    static private boolean isStateFul(Class<?> cls) {
        // Reached the top of the recurtion. This class is stateless.
        if ((cls == null) || (cls == Object.class)) {
            return false;
        }

        // The default value for the annotation, when not present, is false.
        boolean isStateFulAnnotation = cls.isAnnotationPresent(StateFul.class)
            ? cls.getAnnotation(StateFul.class).value() : false;

        // Case 1.
        if (isStateFulAnnotation) {
            return true;
        }

        // Case 2.
        if ((getNonStaticFields(cls.getDeclaredFields()).length > 0) &&
                !cls.isAnnotationPresent(StateFul.class)) {
            return true;
        }

        // Case 3.
        return isStateFul(cls.getSuperclass());
    }

    static private Field[] getNonStaticFields(Field[] field) {
        ArrayList<Field> nonStatic = new ArrayList<Field>();
        for (Field f : field) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue; // static vars are not considered
            }

            nonStatic.add(f);
        }

        return nonStatic.toArray(new Field[nonStatic.size()]);
    }

    /**
     * Determines if two objects share a common state, by having an object in
     * their reference graph.
     *
     * Note that it is possible for an object not to share a state with it self,
     * if the object does not have fields.
     *
     * @param o1
     *            The first object to compare
     * @param o2
     *            The second object to compare
     * @return True if the objects have a reference on a same object inside
     *         their graphs. False if the two object do not have any other
     *         object in common inside their reference graphs.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    static public boolean shareState(Object o1, Object o2)
        throws IllegalArgumentException, IllegalAccessException {
        // If either class is stateless then there is no shared state
        if (!isStateFul(o1.getClass()) || !isStateFul(o2.getClass())) {
            return false;
        }

        IdentityHashMap fieldsO1 = getAllFieldObjects(o1);
        IdentityHashMap fieldsO2 = getAllFieldObjects(o2);

        Collection list = fieldsO1.values();

        for (Object f : list) {
            if (fieldsO2.containsKey(f)) {
                // System.out.println("State shared variable:"+
                // System.identityHashCode(f) + ":" + f.getClass());
                return true;
            }
        }

        return false;
    }

    static public <T> IdentityHashMap<T, T> getAllFieldObjects(Object o)
        throws IllegalArgumentException, IllegalAccessException {
        return getAllFieldObjects(o, new IdentityHashMap<T, T>(), null);
    }

    static public <T> IdentityHashMap<T, T> getAllFieldObjects(Object o,
        Class<T> filter)
        throws IllegalArgumentException, IllegalAccessException {
        return getAllFieldObjects(o, new IdentityHashMap<T, T>(), filter);
    }

    /**
     * This method returns all the sub object instances for an object that match
     * a given pattern. It does not consider static variables though.
     *
     * @param o
     *            The root object to search from
     * @param list
     *            A list of already found objects, or null in which case a new
     *            IdentityHashMap will be instantiated.
     * @param filter
     *            Only considers objects asignable to this class. If this
     *            parameter is null then all fields are consdiered.
     * @return The list with all the objects found, including those passed as
     *         parameteres.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    static public <T> IdentityHashMap<T, T> getAllFieldObjects(Object o,
        IdentityHashMap list, Class<?> filter)
        throws IllegalArgumentException, IllegalAccessException {
        if (list == null) {
            list = new IdentityHashMap();
        }

        if (!list.containsKey(o)) {
            if (filter == null) {
                list.put(o, o);
                // System.out.println("added:" + o.getClass().getSimpleName());
            } else if (filter.isAssignableFrom(o.getClass())) {
                list.put(o, o);
                // System.out.println("added:" + o.getClass().getSimpleName() +
                // " asignable from:" + filter.getSimpleName());
            }
        }

        Class<?> cls = o.getClass();
        ArrayList<Field> field = getAllInheritedFields(cls);

        for (Field f : field) {
            Class<?> c = f.getType();

            // primitives can not be shared & static vars are not considered
            if (c.isPrimitive() || Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            f.setAccessible(true);
            Object fieldObject = f.get(o);

            if ((fieldObject == null) || list.containsKey(fieldObject)) {
                continue; // already visited (this a loop)
            }

            // If its an array of T[], where T is not primitive
            // (ex: int[] is not assignable to Object[])
            if (new Object[0].getClass().isAssignableFrom(fieldObject.getClass())) {
                Object[] array = (Object[]) fieldObject;
                for (Object a : array) {
                    if (a != null) {
                        getAllFieldObjects(a, list, filter);
                    }
                }
            } else { // regular object (not an array
                getAllFieldObjects(fieldObject, list, filter);
            }
        }

        return list;
    }

    /**
     * @param cls
     *            A Class.
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

    /**
     * Determines if an object has a shared state with any of the objects inside
     * the set.
     *
     * @param set
     * @param muscle
     * @return true if the object shares the state. False otherwise.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    static private <T> boolean shareStateWithGroup(Collection<T> set, T muscle)
        throws IllegalArgumentException, IllegalAccessException {
        for (T m : set) {
            if (shareState(m, muscle)) {
                return true;
            }
        }

        return false;
    }

    public static Collection<Muscle> getStatefulMuscles(Skeleton root)
        throws IllegalArgumentException, IllegalAccessException {
        IdentityHashMap fields = getAllFieldObjects(root, null, Muscle.class);

        Collection<Muscle> list = fields.values();

        return list;
    }

    public static void main(String[] args)
        throws IllegalArgumentException, IllegalAccessException {
        Condition<Board> div = new DivideCondition();

        Skeleton<Board, Result> BT1 = new DaC<Board, Result>(new DivideBT1(),
                div, new Seq<Board, Result>(new SolveBT1()), new ConquerBoard());

        Skeleton<Board, Result> BT2 = new DaC<Board, Result>(new DivideBT2(),
                div, new Seq<Board, Result>(new SolveBT2()), new ConquerBoard());

        Skeleton<Board, Result> root = new Fork<Board, Result>(new ConquerBoard(),
                BT1, BT2);

        Collection<Muscle> muscles = getStatefulMuscles(root);
        System.out.println(muscles.size());
        Collection<Collection<Muscle>> groups = getReferenceGroups(muscles);
        for (Collection<Muscle> col : groups) {
            System.out.println(col.size() + col.toString());
        }

        System.out.println(groups);
    }

    @SuppressWarnings("unchecked")
    public static <T> Vector<T> deepCopy(T o, int n)
        throws IOException, ClassNotFoundException {
        // serialize Object into byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        byte[] buf = baos.toByteArray();
        oos.close();

        // deserialize byte array
        Vector<T> vector = new Vector<T>(n);
        while (n-- > 0) {
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(bais);
            vector.add((T) ois.readObject());
            ois.close();
        }

        return vector;
    }

    @SuppressWarnings("unchecked")
    static public <T> T deepCopy(T o)
        throws IOException, ClassNotFoundException {
        // serialize Object into byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        byte[] buf = baos.toByteArray();
        oos.close();

        // deserialize byte array
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        ObjectInputStream ois = new ObjectInputStream(bais);
        T copy = (T) ois.readObject();
        ois.close();

        return copy;
    }
}
