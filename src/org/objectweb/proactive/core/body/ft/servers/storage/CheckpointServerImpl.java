/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.body.ft.servers.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.rmi.ClassServerHelper;


/**
 * @author cdelbe
 * @since 2.2
 */
public abstract class CheckpointServerImpl implements CheckpointServer {
    //logger
    protected static Logger logger = Logger.getLogger(CheckpointServer.class.getName());

    // global server
    protected FTServer server;

    // ClassFileServer and the assiociated codebase
    protected static ClassServerHelper classServerHelper = new ClassServerHelper();
    protected String codebase;

    // The stable storage (idCheckpointer --> [list of] [checkpoints])
    protected Hashtable checkpointStorage;

    /**
     *
     */
    public CheckpointServerImpl(FTServer server) {
        this.server = server;

        this.checkpointStorage = new Hashtable();

        //classloader
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
}
