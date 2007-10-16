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
package org.objectweb.proactive.p2p.v2.service.messages;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.p2p.v2.service.P2PService;


public class AcquaintanceRequest extends Message implements Serializable {
    public AcquaintanceRequest(int i) {
        super(i);
    }

    /**
     * Generates an acquaintance reply
     */
    public void execute(P2PService target) {
        if (!target.stubOnThis.equals(this.sender)) {
            Vector<String> result = target.acquaintanceManager_active.add(this.sender);
            result = (Vector<String>) ProFuture.getFutureValue(result);

            if (result == null) {
                //we have accepted the acquaintance request
                logger.info("Register request from " +
                    ProActiveObject.getActiveObjectNodeUrl(this.sender) +
                    " accepted");
                this.sender.message(new AcquaintanceReply(1,
                        target.generateUuid(), target.stubOnThis,
                        ProActiveObject.getActiveObjectNodeUrl(
                            target.stubOnThis)));
                //service.registerAnswer(ProActive.getActiveObjectNodeUrl(target.stubOnThis),target.stubOnThis);
            } else {
                logger.info("Register request from " +
                    ProActiveObject.getActiveObjectNodeUrl(this.sender) +
                    " rejected");
                //service.registerAnswer(ProActive.getActiveObjectNodeUrl(target.stubOnThis), result);
                this.sender.message(new AcquaintanceReply(1,
                        target.generateUuid(), target.stubOnThis,
                        ProActiveObject.getActiveObjectNodeUrl(
                            target.stubOnThis), result));
            }
        }
    }

    /**
     * This is message should not be forwarded
     */
    public void transmit(P2PService acq) {
    }
}
