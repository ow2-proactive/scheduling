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
package org.objectweb.proactive.core.filetransfer;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is meant to be a service active object. It provides a mechanism
 * to create FileTransferService objects on remote and local nodes.
 *
 * @author The ProActive Team
 */
public class FileTransferEngine implements ProActiveInternalObject, InitActive, RunActive {
    //Not Serializable on purpose: This is a service AO that cannot migrate!!
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);
    static public final int DEFAULT_MAX_FILE_TRANSFER_SERVICES = PAProperties.PA_FILETRANSFER_MAX_SERVICES
            .getValueAsInt();
    static FileTransferEngine singletonFTE = getFileTransferEngine();
    Vector<FileTransferService> ftsPool;
    int maxFTS;

    /**
     * This is an empty constructor for ProActive's MOP. Don't use directly.
     */
    @Deprecated
    public FileTransferEngine() {
    }

    public FileTransferEngine(int maxFTS) {
        this.maxFTS = maxFTS;
    }

    //@Override
    public void initActivity(Body body) {
        ftsPool = new Vector<FileTransferService>();
    }

    //Producer-Consumer
    //@Override
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (true) {
            String allowedMethodNames = "putFTS";

            if ((ftsPool.size() > 0) || (maxFTS > 0)) {
                allowedMethodNames += "getFTS|getFTS";
            }

            service.blockingServeOldest(new RequestFilterOnAllowedMethods(allowedMethodNames));
        }
    }

    public FileTransferService getFTS() throws ActiveObjectCreationException, NodeException {
        if (!ftsPool.isEmpty()) {
            return ftsPool.remove(0);
        }

        FileTransferService localFTS = (FileTransferService) PAActiveObject.newActive(
                FileTransferService.class.getName(), null);
        --maxFTS;

        return localFTS;
    }

    public void putFTS(FileTransferService fts) {
        ftsPool.add(fts);
    }

    static synchronized public FileTransferEngine getFileTransferEngine() {
        if (singletonFTE == null) {
            try {
                singletonFTE = (FileTransferEngine) PAActiveObject.newActive(FileTransferEngine.class
                        .getName(), new Object[] { DEFAULT_MAX_FILE_TRANSFER_SERVICES });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return singletonFTE;
    }

    public synchronized static FileTransferEngine getFileTransferEngine(Node node) {
        ProActiveRuntime runtime = node.getProActiveRuntime();

        FileTransferEngine fte = runtime.getFileTransferEngine();

        return fte;
    }

    public static boolean nodeEquals(Node a, Node b) {
        return a.getNodeInformation().getName().equals(b.getNodeInformation().getName());
    }

    protected class RequestFilterOnAllowedMethods implements RequestFilter, java.io.Serializable {
        private String allowedMethodNames;

        public RequestFilterOnAllowedMethods(String allowedMethodNames) {
            this.allowedMethodNames = allowedMethodNames;
        }

        public boolean acceptRequest(Request request) {
            return allowedMethodNames.indexOf(request.getMethodName()) >= 0;
        }
    }
}
