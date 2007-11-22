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
package org.objectweb.proactive.core.body;

import java.util.Hashtable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class is a Map between UniqueID and either remote or local bodies.
 * It accepts event listeners interested in BodyEvent.
 * Body event are produced whenever a body is added or removed from
 * the collection.
 * </p><p>
 * In case of serialization of a object of this class, all reference to local bodies will
 * get serialized as reference of remote body. Local bodies are never serialized from
 * this container.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.1,  2001/12/23
 * @since   ProActive 0.9
 */
public class BodyMap /* extends AbstractEventProducer */ implements Cloneable,
    java.io.Externalizable {
    //
    // -- PRIVATE MEMBER -----------------------------------------------
    //
    private Hashtable<UniqueID, UniversalBody> idToBodyMap;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public BodyMap() {
        idToBodyMap = new Hashtable<UniqueID, UniversalBody>();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    // 

    /**
     * add the set (id, node) in the idToBodyMap
     * block if it already exists until it is removed
     */
    public synchronized void putBody(UniqueID id, UniversalBody b) {
        while (idToBodyMap.get(id) != null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        idToBodyMap.put(id, b);

        // ProActiveEvent
        //        if (hasListeners()) {
        //            notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_CREATED));
        //        }

        // END ProActiveEvent
    }

    /**
     * add the set (id, node) in the idToBodyMap
     * erase any previous entry
     */
    public synchronized void updateBody(UniqueID id, UniversalBody b) {
        //remove old reference
        if (idToBodyMap.get(id) != null) {
            idToBodyMap.remove(id);
        }

        //add new reference
        idToBodyMap.put(id, b);

        // ProActiveEvent
        //        if (hasListeners()) {
        //            notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_CREATED));
        //        }

        // END ProActiveEvent
    }

    public synchronized void removeBody(UniqueID id) {
        UniversalBody b = idToBodyMap.remove(id);
        notifyAll();

        // ProActiveEvent
        //        if ((b != null) && hasListeners()) {
        //            notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_DESTROYED));
        //        }

        // END ProActiveEvent
    }

    public synchronized int size() {
        int val = idToBodyMap.size();

        return val;
    }

    public synchronized UniversalBody getBody(UniqueID id) {
        Object o = null;
        if (id != null) {
            o = idToBodyMap.get(id);
        }

        return (UniversalBody) o;
    }

    public synchronized boolean containsBody(UniqueID id) {
        return idToBodyMap.containsKey(id);
    }

    public java.util.Iterator<UniversalBody> bodiesIterator() {
        return idToBodyMap.values().iterator();
    }

    @Override
    public synchronized String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(" -- BodyMap ------- \n");

        java.util.Set entrySet = idToBodyMap.entrySet();
        java.util.Iterator iterator = entrySet.iterator();

        while (iterator.hasNext()) {
            java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
            sb.append(entry.getKey()).append("  body = ")
              .append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    //
    // -- implements Cloneable -----------------------------------------------
    //
    @Override
    public Object clone() {
        BodyMap newLocationTable = new BodyMap();
        newLocationTable.idToBodyMap = (Hashtable<UniqueID, UniversalBody>) idToBodyMap.clone();

        return newLocationTable;
    }

    //
    // -- methods for BodyEventProducer -----------------------------------------------
    //    //
    //    public void addBodyEventListener(BodyEventListener listener) {
    //        addListener(listener);
    //    }
    //
    //    public void removeBodyEventListener(BodyEventListener listener) {
    //        removeListener(listener);
    //    }

    //
    // -- implements Externalizable -----------------------------------------------
    //

    /**
     * The object implements the readExternal method to restore its contents by calling the methods
     * of DataInput for primitive types and readObject for objects, strings and arrays.
     */
    public synchronized void readExternal(java.io.ObjectInput in)
        throws java.io.IOException, ClassNotFoundException {
        int size = in.readInt();

        for (int i = 0; i < size; i++) {
            UniqueID id = (UniqueID) in.readObject();
            UniversalBody remoteBody = (UniversalBody) in.readObject();
            idToBodyMap.put(id, remoteBody);
        }
    }

    /**
     * The object implements the writeExternal method to save its contents by calling the methods
     * of DataOutput for its primitive values or calling the writeObject method of ObjectOutput
     * for objects, strings, and arrays.
     */
    public synchronized void writeExternal(java.io.ObjectOutput out)
        throws java.io.IOException {
        int size = idToBodyMap.size();
        out.writeInt(size);

        java.util.Set entrySet = idToBodyMap.entrySet();
        java.util.Iterator iterator = entrySet.iterator();

        while (iterator.hasNext()) {
            java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
            out.writeObject(entry.getKey());

            Object value = entry.getValue();

            if (value instanceof Body) {
                out.writeObject(((Body) value).getRemoteAdapter());
            } else {
                out.writeObject(value);
            }
        }
    }
}
