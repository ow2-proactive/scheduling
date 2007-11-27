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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMInitialState;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreInterface;


public class IMAdminImpl implements IMAdmin, Serializable {
    private static final long serialVersionUID = 320085562179242055L;
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_ADMIN);

    // Attributes
    private IMCoreInterface imcore;

    //----------------------------------------------------------------------//
    // CONSTRUTORS

    /** ProActive compulsory no-args constructor */
    public IMAdminImpl() {
    }

    /**
     * @param imcore
     */
    public IMAdminImpl(IMCoreInterface imcore) {
        if (logger.isInfoEnabled()) {
            logger.info("IMAdmin constructor");
        }

        this.imcore = imcore;
    }

    // =======================================================//
    // TEST
    public StringWrapper echo() {
        return new StringWrapper("Je suis le IMAdmin");
    }

    public void createStaticNodesource(String SourceName,
        ProActiveDescriptor pad) {
        this.imcore.createStaticNodesource(pad, SourceName);
    }

    public void createP2PNodeSource(String id, int nbMaxNodes, int nice,
        int ttr, Vector<String> peerUrls) {
        this.imcore.createP2PNodeSource(id, nbMaxNodes, nice, ttr, peerUrls);
    }

    public void createDummyNodeSource(String id, int nbMaxNodes, int nice,
        int ttr) {
        this.imcore.createDummyNodeSource(id, nbMaxNodes, nice, ttr);
    }

    public void addNodes(ProActiveDescriptor pad, String sourceName) {
        this.imcore.addNodes(pad, sourceName);
    }

    public void addNodes(ProActiveDescriptor pad) {
        this.imcore.addNodes(pad);
    }

    public void removeNode(String nodeUrl, boolean killNode) {
        this.imcore.removeNode(nodeUrl, killNode);
    }

    public void removeSource(String sourceName, boolean killNodes) {
        this.imcore.removeSource(sourceName, killNodes);
    }

    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad() {
        return null;
    }

    //----------------------------------------------------------------------//
    // SHUTDOWN

    /**
     * @throws ProActiveException
     * @see the IMAdmin interface
     */
    public void shutdown() throws ProActiveException {
        this.imcore.shutdown();
    }

    public IMInitialState addIMEventListener(IMEventListener listener,
        IMEvent... events) {
        return this.imcore.getMonitoring().addIMEventListener(listener, events);
    }
}
