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
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.runtime.ProActiveRuntime;


public class RuntimeRegistrationEventProducerImpl extends AbstractEventProducer implements
        RuntimeRegistrationEventProducer {
    //
    //-------------------implements RuntimeRegistrationEventProducer------------------
    //

    /**
     * @see org.objectweb.proactive.core.event.RuntimeRegistrationEventProducer#addRuntimeRegistrationEventListener(RuntimeRegistrationEventListener)
     */
    public void addRuntimeRegistrationEventListener(RuntimeRegistrationEventListener listener) {
        addListener(listener);
    }

    /**
     * @see org.objectweb.proactive.core.event.RuntimeRegistrationEventProducer#removeRuntimeRegistrationEventListener(RuntimeRegistrationEventListener)
     */
    public void removeRuntimeRegistrationEventListener(RuntimeRegistrationEventListener listener) {
        removeListener(listener);
    }

    //
    //-------------------inherited methods from AbstractEventProducer------------------
    //

    /**
     * @see org.objectweb.proactive.core.event.AbstractEventProducer#notifyOneListener(ProActiveListener, ProActiveEvent)
     */
    @Override
    protected void notifyOneListener(ProActiveListener proActiveListener, ProActiveEvent event) {
        RuntimeRegistrationEvent runtimeRegistrationEvent = (RuntimeRegistrationEvent) event;
        RuntimeRegistrationEventListener runtimeRegistrationEventListener = (RuntimeRegistrationEventListener) proActiveListener;

        //notify the listener that a registration occurs
        runtimeRegistrationEventListener.runtimeRegistered(runtimeRegistrationEvent);
    }

    //
    //-------------------PROTECTED METHODS------------------
    //
    protected void notifyListeners(ProActiveRuntime proActiveRuntime, int type,
            ProActiveRuntime registeredRuntime, String creatorID, String protocol, String vmName) {
        if (hasListeners()) {
            notifyAllListeners(new RuntimeRegistrationEvent(proActiveRuntime, type, registeredRuntime,
                creatorID, protocol, vmName));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("no listener");
            }
        }
    }
}
