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
package org.objectweb.proactive.examples.minidescriptor;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Sends a bunch of parallel requests to a given virtual node
 *
 * @author Jerome+Sylvain
 */
public class MiniDescrClient {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private static final int NB_THREADS = 10;
    private static final int NB_CALLS_PER_THREAD = 10;

    public MiniDescrClient(String location) {
        VirtualNode virtualnode = null;

        ProActiveDescriptor pad = null;
        logger.info("-+-+-+-+-+-+-+- MiniDescrClient launched -+-+-+-+-+-+-+-");

        try {
            pad = ProDeployment.getProactiveDescriptor(location);
            virtualnode = pad.getVirtualNode("MiniVNServer");
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        virtualnode.activate();

        Node[] nodes = null;
        Thread[] bombs = new Thread[NB_THREADS];

        try {
            nodes = virtualnode.getNodes();

            for (int i = 0; i < nodes.length; i++) {
                for (int j = 0; j < NB_THREADS; j++) {
                    bombs[j] = new Bomber(j, nodes[i]);
                    bombs[j].start();

                    // Use this line instead to make sequential calls
                    // bombs[j].run();
                }
            }

            // Wait for all threads to finish before to return
            for (int j = 0; j < NB_THREADS; j++)
                try {
                    bombs[j].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MiniDescrClient("descriptors/examples/minidescriptor_client.xml");

        System.out.println("Done");
        System.exit(0);
    }

    class Bomber extends Thread {
        int no;
        Node node;

        Bomber(int no, Node node) {
            this.no = no;
            this.node = node;
        }

        void appendZeros(StringBuffer buf, int n, int maxDigits) {
            int nbZeros = maxDigits -
                (int) Math.ceil((Math.log(n + 1) / Math.log(10)));

            for (int i = 0; i < nbZeros; i++)
                buf.append('0');
        }

        @Override
        public void run() {
            try {
                // Create remote object on the node
                MiniDescrActive desc = (MiniDescrActive) ProActiveObject.newActive(MiniDescrActive.class.getName(),
                        null, node);

                // Thread number trace
                int threadNbDigits = (int) Math.ceil((Math.log(NB_THREADS + 1) / Math.log(
                            10)));
                String threadTrace;
                {
                    StringBuffer buf = new StringBuffer();
                    buf.append("Thread #");
                    appendZeros(buf, no + 1, threadNbDigits);
                    buf.append(no + 1);
                    buf.append(", Call #");
                    threadTrace = buf.toString();
                }

                int callsNbDigits = (int) Math.ceil((Math.log(NB_CALLS_PER_THREAD +
                            1) / Math.log(10)));
                for (int k = 0; k < NB_CALLS_PER_THREAD; k++) {
                    // Call remote object
                    Message msg = desc.getComputerInfo();

                    // Call number trace
                    StringBuffer buf = new StringBuffer(threadTrace);
                    appendZeros(buf, k + 1, callsNbDigits);
                    buf.append(k + 1);
                    buf.append(" -+-+-+-+-+-+-+- ");
                    buf.append(msg);
                    buf.append(" -+-+-+-+-+-+-+-");
                    logger.info(buf.toString());
                }
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            } catch (NodeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Error during remote call: ");
                e.printStackTrace();
            }
        }
    }
}
