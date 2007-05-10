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
package org.objectweb.proactive.core.gc;

import org.apache.log4j.Level;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.body.SendReplyCommunicationException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;


/**
 * Sending a reply to an object that has been garbage collected will fail.
 * This is not an error.
 */
public class SendReplyCommunicationExceptionHandler implements NFEListener {
    public static final SendReplyCommunicationExceptionHandler instance = new SendReplyCommunicationExceptionHandler();

    public boolean handleNFE(NonFunctionalException e) {
        SendReplyCommunicationException srce = (SendReplyCommunicationException) e;
        GarbageCollector gc = ((AbstractBody) srce.getSender()).getGarbageCollector();
        gc.log(Level.INFO,
            "Failure to send a reply to " + srce.getReceiverID().shortString() +
            " caused by ");
        e.printStackTrace();
        return true;
    }
}
