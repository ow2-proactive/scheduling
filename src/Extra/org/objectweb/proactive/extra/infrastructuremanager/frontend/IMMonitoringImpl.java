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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;


/**
 * @author Ellendir
 *
 */
public class IMMonitoringImpl implements IMMonitoring {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_MONITORING);

    // Attributes
    private IMCore imcore;

    // ----------------------------------------------------------------------//
    // CONSTRUTORS

    /** ProActive compulsory no-args constructor */
    public IMMonitoringImpl() {
    }

    public IMMonitoringImpl(IMCore imcore) {
        if (logger.isDebugEnabled()) {
            logger.debug("IMMonitoring constructor");
        }
        this.imcore = imcore;
    }

    // =======================================================//
    public StringWrapper echo() {
        return new StringWrapper("Je suis le IMMonitoring");
    }

    // =======================================================//
    public HashMap<String, ProActiveDescriptor> getListDescriptor() {
        if (logger.isDebugEnabled()) {
            logger.debug("getListDescriptor");
        }
        return imcore.getListPAD();
    }

    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad() {
        if (logger.isDebugEnabled()) {
            logger.debug("getDeployedVirtualNodeByPad");
        }
        return imcore.getDeployedVirtualNodeByPad();
    }

    public ArrayList<IMNode> getListAllIMNodes() {
        if (logger.isDebugEnabled()) {
            logger.debug("getListAllIMNodes");
        }
        return imcore.getListAllNodes();
    }

    public ArrayList<IMNode> getListFreeIMNode() {
        if (logger.isDebugEnabled()) {
            logger.debug("getListFreeIMNode");
        }
        return imcore.getListFreeIMNode();
    }

    public ArrayList<IMNode> getListBusyIMNode() {
        if (logger.isDebugEnabled()) {
            logger.debug("getListBusyIMNode");
        }
        return imcore.getListBusyIMNode();
    }

    public IntWrapper getNumberOfFreeResource() {
        if (logger.isDebugEnabled()) {
            logger.debug("getNumberOfFreeResource");
        }
        return imcore.getSizeListFreeIMNode();
    }

    public IntWrapper getNumberOfBusyResource() {
        if (logger.isDebugEnabled()) {
            logger.debug("getNumberOfBusyResource");
        }
        return imcore.getSizeListBusyIMNode();
    }

    public IntWrapper getNumberOfDownResource() {
        return this.imcore.getSizeListDownIMNode();
    }

    public IntWrapper getNumberOfAllResources() {
        return this.imcore.getNbAllIMNode();
    }
}
