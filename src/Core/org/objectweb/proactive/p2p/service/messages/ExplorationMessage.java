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

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.util.UniversalUniqueID;


public class ExplorationMessage extends BreadthFirstMessage implements Serializable {
    public ExplorationMessage(int ttl, UniversalUniqueID id, P2PService sender) {
        super(ttl, id, sender);
    }

    @Override
    public void execute(P2PService target) {
        // This should be register
        if (target.shouldBeAcquaintance(this.sender)) {
            //  target.register(this.sender);
            //we cannot resister the sender as one of our peer yet
            //because he might have received replies
            //and have reached its NOA
            try {
                String[] result = null; //this.sender.registerRequest(target.stubOnThis).toArray(new String[] {});

                if (result == null) {
                    logger.info("ExplorationMessage me = " +
                        P2PService.getHostNameAndPortFromUrl(PAActiveObject
                                .getActiveObjectNodeUrl(target.stubOnThis)) +
                        " adding " +
                        P2PService.getHostNameAndPortFromUrl(PAActiveObject
                                .getActiveObjectNodeUrl(this.sender)));
                    //indeed, the peer really wants us
                    //     target.registerRequest(this.sender);
                    target.getAcquaintanceManager().startAcquaintanceHandShake(
                            P2PService.getHostNameAndPortFromUrl(PAActiveObject
                                    .getActiveObjectNodeUrl(this.sender)), this.sender);
                }
            } catch (Exception e) {
                logger.info("Trouble with registering remote peer", e);
                target.acquaintanceManager_active.remove(this.sender, null);
            }
        }
    }
}
