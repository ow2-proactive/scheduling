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
package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;
import org.objectweb.proactive.extra.scheduler.common.scripting.VerifyingScript;


public class IMUserImpl implements IMUser {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_USER);

    // Attributes
    private IMCore imcore;

    //----------------------------------------------------------------------//
    // CONSTRUCTORS

    /** ProActive compulsory no-args constructor */
    public IMUserImpl() {
    }

    public IMUserImpl(IMCore imcore) {
        if (logger.isDebugEnabled()) {
            logger.debug("IMUser constructor");
        }
        this.imcore = imcore;
    }

    //=======================================================//
    public StringWrapper echo() {
        return new StringWrapper("Je suis le IMUser");
    }

    //=======================================================//

    //----------------------------------------------------------------------//
    // METHODS
    public NodeSet getAtMostNodes(IntWrapper nb, VerifyingScript verifyingScript) {
        //        if (logger.isInfoEnabled()) {
        //            logger.info("getAtMostNodes, nb nodes : " + nb + " (dispo : " +
        //                imcore.getSizeListFreeIMNode() + ")");
        //        }
        return imcore.getAtMostNodes(nb, verifyingScript);
    }

    public NodeSet getExactlyNodes(IntWrapper nb,
        VerifyingScript verifyingScript) {
        if (logger.isInfoEnabled()) {
            logger.info("getExactlyNodes, nb nodes : " + nb);
        }
        return imcore.getExactlyNodes(nb, verifyingScript);
    }

    public void freeNode(Node node) {
        if (logger.isInfoEnabled()) {
            logger.info("freeNode : " + node.getNodeInformation().getURL());
        }
        imcore.freeNode(node);
    }

    public void freeNodes(NodeSet nodes) {
        if (logger.isInfoEnabled()) {
            String freeNodes = "";
            for (Node node : nodes) {
                freeNodes += (node.getNodeInformation().getName() + " ");
            }
            logger.info("freeNode : " + freeNodes);
        }
        imcore.freeNodes(nodes);
    }
}
