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
package org.objectweb.proactive.core.group;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;


/**
 * This interface presents the group abilities extending java.util.Collection.
 * A Group also permit some 'Map' operations like put, get(key), ...
 *
 * @see java.util.Collection
 *
 * @author Laurent Baduel
 *
 */
public interface Group<E> extends List<E> {

    /**
     * Returns the (upper) class of member.
     */
    public Class<?> getType() throws java.lang.ClassNotFoundException;

    /**
     * Returns the name of the (upper) class of member.
     */
    public String getTypeName();

    /**
     * Returns an object representing the group, and assignable from the (upper) class of member.
     */
    public Object getGroupByType();

    /**
     * Returns the object at the specified index.
     */
    public E get(int index);

    /**
     * Merges a group into the group.
     */
    public void addMerge(Object ogroup);

    /**
     * Removes the object at the specified index.
     * @return the object that has been removed
     */
    public E remove(int index);

    /**
     * Returns the index in the group of the first occurence of the specified element, -1 if the list does not contain this element.
     */
    public int indexOf(Object obj);

    /**
     * Returns a list iterator of the members in this Group (in proper sequence).
     */
    public ListIterator<E> listIterator();

    /**
     * Waits that all the members are arrived.
     */
    public void waitAll();

    /**
     * Waits that at least one member is arrived.
     */
    public void waitOne();

    /**
     * Waits that the member at the specified rank is arrived.
     * @param n the rank of the awaited member.
     */
    public void waitTheNth(int n);

    /**
     * Waits that at least <code>n</code> members are arrived.
     * @param n the number of awaited members.
     */
    public void waitN(int n);

    /**
     * Waits that at least one member is arrived and returns it.
     * @return a non-awaited member of the Group.
     */
    public Object waitAndGetOne();

    /**
     * Waits one future is arrived and returns it (removes it from the group).
     * @return a member of <code>o</code>. (<code>o</code> is removed from the group)
     */
    public Object waitAndGetOneThenRemoveIt();

    /**
     * Waits that the member at the specified rank is arrived and returns it.
     * @param n - the rank of the wanted member.
     * @return the member (non-awaited) at the rank <code>n</code> in the Group.
     */
    public Object waitAndGetTheNth(int n);

    /**
     * Waits that at least one member is arrived and returns its index.
     * @return the index of a non-awaited member of the Group.
     */
    public int waitOneAndGetIndex();

    /**
     * Checks if all the members of the Group are awaited.
     * @return <code>true</code> if all the members of the Group are awaited.
     */
    public boolean allAwaited();

    /**
     * Checks if all the members of the Group are arrived.
     * @return <code>true</code> if all the members of the Group are arrived.
     */
    public boolean allArrived();

    /**
     * Returns an ExceptionListException containing all the throwables (exceptions and errors) occured
     * when this group was built
     * @return an ExceptionListException
     */
    public ExceptionListException getExceptionList();

    /**
     * Removes all exceptions and null references contained in the Group.
     * Exceptions (and null references) appears with communication/program-level/runtime errors
     * and are stored in the Group.
     * (After this operation the size of the Group decreases)
     */
    public void purgeExceptionAndNull();

    /**
     * Modifies the number of members served by one thread
     * @param i - the new ratio
     */
    public void setRatioMemberToThread(int i);

    /**
     * Creates a new group with all members of the group and all the members of the group <code>g</code>
     * @param g - a group
     * @return a group that contain all the members of the group and <code>g</code>. <code>null<code> if the class of the group is incompatible.
     */
    public Group union(Group g);

    /**
     * Creates a new group with all members that belong to the group and to the group <code>g</code>.
     * @param g - a group
     * @return a group that contain the common members of the group and <code>g</code>. <code>null<code> if the class of the group is incompatible.
     */
    public Group intersection(Group g);

    /**
     * Creates a new group with all members that belong to the group or to the group <code>g</code>, but not to both.
     * @param g - a group
     * @return a group that contain the non-common members of the group and <code>g</code>. <code>null<code> if the class of the group is incompatible.
     */
    public Group difference(Group g);

    /**
     * Creates a new group with the members that belong to the group, but not to the group <code>g</code>.
     * @param g - a group
     * @return a group that contain the members of the group without the member <code>g</code>. <code>null<code> if the class of the group is incompatible.
     */
    public Group exclude(Group g);

    /**
     * Creates a new group with the members of the group begining at the index <code>begin</code> and ending at the index <code>end</code>.
     * @param begin - the begining index
     * @param end - the ending index
     * @return a group that contain the members of the group from <code>begin</code> to <code>end</code>. <code>null</code> if <code>begin > end</code>.
     */
    public Group range(int begin, int end);

    /**
     * Set whether to automatically remove failing elements from the group
     * instead of throwing an exception
     */
    public void setAutomaticPurge(boolean autoPurge);

    // Map class style methods

    /**
     * Returns <code>true</code> if this Group contains a mapping for the specified key.
     * More formally, returns <code>true</code> if and only if this Group contains at
     * a mapping for a key <code>k</code> such that <code>(key==null ? k==null : key.equals(k))</code>.
     * (There can be at most one such mapping.)
     * @param key - key whose presence in this Group is to be tested.
     * @return <code>true</code> if this Group contains a mapping for the specified key.
     * @throws ClassCastException - if the key is of an inappropriate type for this Group (optional).
     * @throws NullPointerException - if the key is null and this Group does not not permit null keys (optional).
     */
    public boolean containsKey(String key);

    /**
     * Returns <code>true</code> if this Group maps one or more keys to the specified value.
     * More formally, returns <code>true</code> if and only if this Group contains at least
     * one mapping to a value <code>v</code> such that <code>(value==null ? v==null : value.equals(v))</code>.
     * @param value - value whose presence in this map is to be tested.
     * @return <code>true</code> if this Group maps one or more keys to the specified value.
     * @throws ClassCastException - if the value is of an inappropriate type for this Collection (optional).
     * @throws NullPointerException - if the value is null and this Group does not not permit null values (optional).
     */
    public boolean containsValue(Object value);

    /**
     * Returns the Object to which this Group maps the specified key.
     * Returns <code>null</code> if the Collection contains no mapping for this key.
     * A return value of <code>null</code> does not necessarily indicate that the Collection
     * contains no mapping for the key; it's also possible that the Group explicitly maps the key to null.
     * The containsKey operation may be used to distinguish these two cases.
     * More formally, if this Group contains a mapping from a key <code>k</code> to a value
     * <code>v</code> such that <code>(key==null ? k==null : key.equals(k))</code>,
     * then this method returns <code>v</code>; otherwise it returns <code>null</code>.
     * (There can be at most one such mapping.)
     * @param key - key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or <code>null</code> if the map contains no mapping for this key.
     * @throws ClassCastException - if the key is of an inappropriate type for this Group (optional).
     * @throws NullPointerException - key is <code>null</code> and this Group does not not permit null keys (optional).
     */
    public Object getNamedElement(String key);

    /**
     * Removes the mapping for this key from the group if it is present.
     * More formally, if this group contains a mapping from key k to value v such that (key==null ? k==null : key.equals(k)),
     * that mapping is removed. (The map can contain at most one such mapping.)
     * Returns the value to which the group previously associated the key, or null if the group contained no mapping for this key.
     * The group will not contain a mapping for the specified key once the call returns.
     * @param key the name of the element
     * @return the named element
     */
    public Object removeNamedElement(String key);

    /**
     * Associates the specified value with the specified key in this Group (optional operation).
     * If the Group previously contained a mapping for this key, the old value is replaced by
     * the specified value. (A map <code>m</code> is said to contain a mapping for a key
     * <code>k</code> if and only if <code>m.containsKey(k)</code> would return <code>true</code>.))
     * In that case, the old value is also removed from the group.
     * @param key - key with which the specified value is to be associated.
     * @param value - value to be associated with the specified key.
     * @throws UnsupportedOperationException - if the put operation is not supported by this Group.
     * @throws ClassCastException - if the class of the specified key or value prevents it from being stored in this Group.
     * @throws IllegalArgumentException - if some aspect of this key or value prevents it from being stored in this Group.
     * @throws NullPointerException - this map does not permit null keys or values, and the specified key or value is <code>null</code>.
     */
    public void addNamedElement(String key, Object value);

    /**
     *        Returns a set view of the keys contained in this Group.
     * The set is backed by the Group, so changes to the Group are reflected in the set,
     * and vice-versa. If the Group is modified while an iteration over the set is in progress,
     * the results of the iteration are undefined. The set supports element removal,
     * which removes the corresponding mapping from the Group, via the Iterator.remove,
     * Set.remove, removeAll retainAll, and clear operations.
     * It does not support the add or addAll operations.
     * @return a set view of the keys contained in this Group.
     */
    public Set keySet();
}
