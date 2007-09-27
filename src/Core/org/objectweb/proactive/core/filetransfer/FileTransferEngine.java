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
package org.objectweb.proactive.core.filetransfer;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is ment to be a service active object. It provides a mechanism
 * to create FileTransferService objects on remote and local nodes.
 *
 * @author The ProActive Team 06/06 (mleyton)
 *
 */
public class FileTransferEngine implements ProActiveInternalObject {
    //Not serializable on purpose: This is a service AO that cannot migrate!!
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);
    static FileTransferEngine singletonFTE = getFileTransferEngine();
    public Vector<FileTransferService> ftsPool;

    public FileTransferEngine() {
    }

    public void init() {
        ftsPool = new Vector<FileTransferService>();
    }

    public FileTransferService getFTS()
        throws ActiveObjectCreationException, NodeException {
        if (!ftsPool.isEmpty()) {
            return ftsPool.remove(0);
        }

        FileTransferService localFTS = (FileTransferService) ProActiveObject.newActive(FileTransferService.class.getName(),
                null);
        setImmediateServices(localFTS);

        return localFTS;
    }

    //TODO improove this method to getFTS on remote nodes from a pool
    public FileTransferService getFTS(Node node)
        throws ActiveObjectCreationException, NodeException {
        FileTransferService remoteFTS = (FileTransferService) ProActiveObject.newActive(FileTransferService.class.getName(),
                null, node);

        setImmediateServices(remoteFTS);
        return remoteFTS;
    }

    public FileTransferService getFTS(String srcNodeURL)
        throws ActiveObjectCreationException, NodeException {
        FileTransferService remoteFTS = (FileTransferService) ProActiveObject.newActive(FileTransferService.class.getName(),
                null, srcNodeURL);

        setImmediateServices(remoteFTS);
        return remoteFTS;
    }

    private void setImmediateServices(FileTransferService fts) {
        fts.setImmediateSevices();
    }

    public void putFTS(FileTransferService fts) {
        ftsPool.add(fts);
    }

    //TODO improove this method using ProActive
    static synchronized public FileTransferEngine getFileTransferEngine() {
        if (singletonFTE == null) {
            try {
                singletonFTE = (FileTransferEngine) ProActiveObject.newActive(FileTransferEngine.class.getName(),
                        null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            singletonFTE.init();
        }
        return singletonFTE;
    }

    public synchronized static FileTransferEngine getFileTransferEngine(
        Node node) {
        try {
            return (FileTransferEngine) ProActiveObject.lookupActive(FileTransferEngine.class.getName(),
                node.getNodeInformation().getURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
