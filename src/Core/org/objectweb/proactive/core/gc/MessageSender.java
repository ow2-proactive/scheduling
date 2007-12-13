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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.LocalBodyStore;


/**
 * When many objects in a VM reference a same remote object, instead
 * of having each referencer spawn a thread to send a GC Message, the
 * GC messages are aggregated and sent once. This reduces the load.
 *
 * When the target object is in the same VM, we don't go through RMI
 * or anything else, we directly deliver the message.
 */
public class MessageSender {
    static void sendMessages(Collection<GCSimpleMessage> messages) {
        Map<Referenced, GCMessage> queue = new TreeMap<Referenced, GCMessage>();
        for (GCSimpleMessage m : messages) {
            Referenced destination = m.getReferenced();
            UniqueID destId = destination.getBodyID();
            Body body = LocalBodyStore.getInstance().getLocalBody(destId);
            if (body != null) {

                /* Local body, directly deliver */
                GCSimpleResponse resp = ((AbstractBody) body).getGarbageCollector().receiveSimpleGCMessage(m);
                destination.setLastResponse(resp);
                continue;
            }

            GCMessage compound = queue.get(destination);
            if (compound == null) {
                compound = new GCMessage();
                queue.put(destination, compound);
            }

            compound.add(m);
        }

        for (Map.Entry<Referenced, GCMessage> entry : queue.entrySet()) {
            entry.getKey().sendMessage(entry.getValue());
        }
    }
}
