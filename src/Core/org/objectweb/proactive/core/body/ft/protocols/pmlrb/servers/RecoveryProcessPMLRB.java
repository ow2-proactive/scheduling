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
package org.objectweb.proactive.core.body.ft.protocols.pmlrb.servers;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryJob;
import org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcessImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class defines a recovery process for PMLRB protocol.
 * @author The ProActive Team
 * @since 3.0
 */
public class RecoveryProcessPMLRB extends RecoveryProcessImpl {
    //logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE_PML);

    /**
     * Constructor.
     * @param server the corresponding global server.
     */
    public RecoveryProcessPMLRB(FTServer server) {
        super(server);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcessImpl#recover(org.objectweb.proactive.core.UniqueID)
     */
    @Override
    protected void recover(UniqueID failed) {
        try {
            Checkpoint toSend = this.server.getLastCheckpoint(failed);

            //look for a new Runtime for this oa
            Node node = this.server.getFreeNode();

            //if (node==null)return;
            RecoveryJob job = new RecoveryJob(toSend, FTManager.DEFAULT_TTC_VALUE, node);
            this.submitJob(job);
        } catch (RemoteException e) {
            logger.error("[RECOVERY] **ERROR** Cannot contact other servers : ");
            e.printStackTrace();
        }
    }
}
