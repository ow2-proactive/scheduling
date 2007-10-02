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
package org.objectweb.proactive.core.body.ft.message;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.Reply;


/**
 * This class is used for logging a reply.
 * It contains its original destination.
 * @author cdelbe
 * @since ProActive 2.2
 */
public class ReplyLog implements MessageLog {

    /**
         *
         */
    private static final long serialVersionUID = 5153002133749379035L;

    // Logged message and its destination
    private UniversalBody destination;
    private Reply reply;

    /**
     * Create a reply log.
     * @param r The reply to log
     * @param d The destination body
     */
    public ReplyLog(Reply r, UniversalBody d) {
        this.destination = d;
        this.reply = r;
    }

    /**
     * Return the logged reply.
     * @return the logged reply.
     */
    public Reply getReply() {
        return reply;
    }

    /**
     * Return the destination of this reply
     * @return the destination of this reply
     */
    public UniversalBody getDestination() {
        return destination;
    }
}
