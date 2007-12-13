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
package org.objectweb.proactive.core.body.ft.servers.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.rmi.ClassServerHelper;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author cdelbe
 * @since 2.2
 */
public abstract class CheckpointServerImpl implements CheckpointServer {
    // logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE);

    // used memory
    private final static Runtime runtime = Runtime.getRuntime();

    // global server
    protected FTServer server;

    // ClassFileServer and the assiociated codebase
    protected static ClassServerHelper classServerHelper = new ClassServerHelper();
    protected String codebase;

    // The stable storage (idCheckpointer --> [list of] [checkpoints])
    protected Hashtable<UniqueID, List<Checkpoint>> checkpointStorage;

    /**
     *
     */
    public CheckpointServerImpl(FTServer server) {
        this.server = server;

        this.checkpointStorage = new Hashtable<UniqueID, List<Checkpoint>>();

        // classloader
        try {
            CheckpointServerImpl.classServerHelper.setShouldCreateClassServer(true);
            this.codebase = CheckpointServerImpl.classServerHelper.initializeClassServer();
            System.setProperty("java.rmi.server.codebase", this.codebase);
            logger.info("ClassServer is bound on " + this.codebase);
        } catch (IOException e) {
            this.codebase = "NO CODEBASE";
            System.err.println("** ERROR ** Unable to launch FT server : ");
            e.printStackTrace();
        }

        try {
            NodeFactory.getDefaultNode();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getServerCodebase()
     */
    public String getServerCodebase() throws RemoteException {
        return this.codebase;
    }

    // UTIL METHODS
    protected long getSize(Serializable c) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // serialize the body
            oos.writeObject(c);
            // store the serialized form
            return baos.toByteArray().length;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /*
     * Return the memory actually used For debugging stuff.
     */
    protected long getUsedMem() {
        return (CheckpointServerImpl.runtime.totalMemory() - CheckpointServerImpl.runtime.freeMemory()) / 1024;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#initialize()
     */
    public void initialize() throws RemoteException {
        this.checkpointStorage = new Hashtable<UniqueID, List<Checkpoint>>();
    }
}
