/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.body.UniversalBody;


public class BodyEventProducerImpl extends AbstractEventProducer {
    public BodyEventProducerImpl() {
    }

    public void fireBodyCreated(UniversalBody b) {
        if (hasListeners()) {
            notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_CREATED));
        }
    }

    public void fireBodyRemoved(UniversalBody b) {
        if (hasListeners()) {
            notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_DESTROYED));
        }
    }

    public void fireBodyChanged(UniversalBody b) {
        //System.out.println("fireBodyChanged from AbstractBody active="+b.isActive());
        if (hasListeners()) {
            notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_CHANGED));
        }
    }

    //
    // -- implements BodyEventProducer -----------------------------------------------
    //
    public void addBodyEventListener(BodyEventListener listener) {
        addListener(listener);
    }

    public void removeBodyEventListener(BodyEventListener listener) {
        removeListener(listener);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void notifyOneListener(ProActiveListener listener,
        ProActiveEvent event) {
        BodyEvent bodyEvent = (BodyEvent) event;
        switch (bodyEvent.getType()) {
        case BodyEvent.BODY_CREATED:
            ((BodyEventListener) listener).bodyCreated(bodyEvent);
            break;
        case BodyEvent.BODY_DESTROYED:
            ((BodyEventListener) listener).bodyDestroyed(bodyEvent);
            break;
        case BodyEvent.BODY_CHANGED:
            ((BodyEventListener) listener).bodyChanged(bodyEvent);
            break;
        }
    }
} // end class BodyEventProducerImpl
