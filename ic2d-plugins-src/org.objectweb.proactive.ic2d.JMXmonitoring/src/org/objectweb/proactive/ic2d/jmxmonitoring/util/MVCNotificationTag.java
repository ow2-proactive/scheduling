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
package org.objectweb.proactive.ic2d.jmxmonitoring.util;


/**
 * This Enum describes the types of notification messages
 * sent in the ic2d mvc mechanism.
 * Each time an event occurs in an AbstractData object,
 * a notification of type <code> Notification </code> is sent.
 * A Notification includes a notification message (a constant defined in this enum)
 * and an object Data which depends on the notification message.
 *
 * @author ProActiveTeam
 *
 */
public enum MVCNotificationTag {
    //---------- General messages ----------
    /**
     * Notification message sent when a child has been added to an
     * <code> org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData </code> object.
     * Use the key corresponding to the child as data in the Notification object.
     */
    ADD_CHILD,
    /**
     * Notification message sent when a child has been removed from an
     * <code> org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData </code> object.
     * Use the key corresponding to the child as data in the Notification object.
     */
    REMOVE_CHILD,
    /**
     * Notification message sent when a child of a
     * <code> org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData </code> object
     * is no longer monitored.
     * Use the key corresponding to the child as data in the Notification object.
     */
    REMOVE_CHILD_FROM_MONITORED_CHILDREN,
    /**
     * Notification message sent when the state of a
     * <code> org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData </code> object
     * has been changed.
     * Use the a constant from <code>State</code> enum which describes the new state.
     */
    STATE_CHANGED,
    /**
     * Notification message sent when the first child is added to a
     * <code> org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject </code> object
     */
    WORLD_OBJECT_FIRST_CHILD_ADDED,
    /**
     * Notification message sent when the last child is removed from a
     * <code> org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject </code> object
     */
    WORLD_OBJECT_LAST_CHILD_REMOVED,
    /**
     * Notification message sent when a <code> VirtualNode </code> is added to a
     * <code> org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject </code> object
     * Use the key corresponding to the virtual node as data in the Notification object.
     */
    WORLD_OBJECT_ADD_VIRTUAL_NODE,
    /**
     * Notification message sent when a <code> VirtualNode </code> is removed from a
     * <code> org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject </code> object
     * Use the key corresponding to the virtual node as data in the Notification object.
     */
    WORLD_OBJECT_REMOVE_VIRTUAL_NODE,HOST_OBJECT_UPDATED_OSNAME_AND_VERSON,
    RUNTIME_OBJECT_RUNTIME_KILLED,
    RUNTIME_OBJECT_RUNTIME_NOT_RESPONDING,
    RUNTIME_OBJECT_RUNTIME_NOT_MONITORED,
    ACTIVE_OBJECT_ADD_COMMUNICATION,
    ACTIVE_OBJECT_RESET_COMMUNICATIONS,
    ACTIVE_OBJECT_REQUEST_QUEUE_LENGHT_CHANGED;
}
