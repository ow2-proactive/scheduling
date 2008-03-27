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
package org.objectweb.proactive.p2p.service.messages;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.p2p.service.P2PService;


public class AcquaintanceRequest extends Message implements Serializable {
    public AcquaintanceRequest(int i) {
        super(i);
    }

    /**
     * Generates an acquaintance reply
     */
    @Override
    public void execute(P2PService target) {
        if (!target.stubOnThis.equals(this.sender)) {
            Vector<String> result = target.acquaintanceManager_active.add(this.sender);
            result = (Vector<String>) PAFuture.getFutureValue(result);

            if (result == null) {
                //we have accepted the acquaintance request
                logger.info("Register request from " + PAActiveObject.getActiveObjectNodeUrl(this.sender) +
                    " accepted");
                this.sender.message(new AcquaintanceReply(1, target.generateUuid(), target.stubOnThis,
                    PAActiveObject.getActiveObjectNodeUrl(target.stubOnThis)));
                //service.registerAnswer(ProActive.getActiveObjectNodeUrl(target.stubOnThis),target.stubOnThis);
            } else {
                logger.info("Register request from " + PAActiveObject.getActiveObjectNodeUrl(this.sender) +
                    " rejected");
                //service.registerAnswer(ProActive.getActiveObjectNodeUrl(target.stubOnThis), result);
                this.sender.message(new AcquaintanceReply(1, target.generateUuid(), target.stubOnThis,
                    PAActiveObject.getActiveObjectNodeUrl(target.stubOnThis), result));
            }
        }
    }

    /**
     * This is message should not be forwarded
     */
    @Override
    public void transmit(P2PService acq) {
    }
}
