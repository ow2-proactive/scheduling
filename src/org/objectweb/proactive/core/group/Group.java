/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.group;

import java.util.ListIterator;


/**
 * This interface presents the group abilities extending java.util.Collection.
 *
 * @see java.util.Collection
 *
 * @author Laurent Baduel
 *
 */
public interface Group extends java.util.Collection {

    /**
     * Returns the (upper) class of member.
     */
    public Class getType() throws java.lang.ClassNotFoundException;

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
    public Object get(int index);

    /**
     * Merges a group into the group.
     */
    public void addMerge(Object ogroup);

    /**
     * Removes the object at the specified index.
     */
    public void remove(int index);

    /**
     * Returns the index in the group of the first occurence of the specified element, -1 if the list does not contain this element.
     */
    public int indexOf(Object obj);

    /**
     * Returns a list iterator of the members in this Group (in proper sequence).
     */
    public ListIterator listIterator();

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
     * @param <code>index</code> the rank of the awaited member.
     */
    public void waitTheNth(int n);

    /**
     * Waits that at least <code>n</code> members are arrived.
     * @param <code>n</code> the number of awaited members.
     */
    public void waitN(int n);

    /**
     * Waits that at least one member is arrived and returns it.
     * @return a non-awaited member of the Group.
     */
    public Object waitAndGetOne();

    /**
     * Waits that the member at the specified rank is arrived and returns it.
     * @param <code>n</code> the rank of the wanted member.
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
     * Returns an ExceptionList containing all the throwables (exceptions and errors) occured
     * when this group was built
     * @return an ExceptionList
     */
    public ExceptionList getExceptionList();

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
    public void setRatioNemberToThread(int i);

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
    
//	/**
//	 * Strongly synchronizes all the members of the group
//	 */
//    public void barrier ();
}
