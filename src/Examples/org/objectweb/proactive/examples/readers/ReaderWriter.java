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
package org.objectweb.proactive.examples.readers;


/*
   ajouter un futur a la place de tout ca.
   Dans l interface ajouter orange pour attente.
 */
import org.objectweb.proactive.ObjectForSynchronousCall;


public class ReaderWriter implements org.objectweb.proactive.RunActive {
    private int readCount;
    private int writeCount;
    private ReaderDisplay display;
    private boolean done;
    private int policy;
    public static final int DEFAULT_POLICY = 0;
    public static final int WRITER_POLICY = 1;
    public static final int READER_POLICY = 2;

    /**
     * The no arg constructor commanded by Papdc
     */
    public ReaderWriter() {
    }

    /**
     * The real constructor
     */
    public ReaderWriter(ReaderDisplay display, int policy) {
        this.display = display;
        this.policy = policy;
    }

    /**
     * setPolicy
     * Changes the synchronization policy
     * @param policy  the id of the new policy
     */
    public void setPolicy(int policy) {
        if ((policy == DEFAULT_POLICY) || (policy == WRITER_POLICY) ||
                (policy == READER_POLICY)) {
            this.policy = policy;
        }
    }

    public ObjectForSynchronousCall startRead() {
        readCount++;
        return new ObjectForSynchronousCall();
    }

    public int endRead() {
        return --readCount;
    }

    public ObjectForSynchronousCall startWrite() {
        writeCount++;
        return new ObjectForSynchronousCall();
    }

    public int endWrite() {
        return --writeCount;
    }

    /**
     * evenPolicy
     * <ul>This handles the default sync policy :
     * <li> When a new <code>startWrite</code> request arrives,
     * no <b>new</b> <code>startRead</code> request are served</li>
     * <li>If there is no reader or writer, just serve the oldest request.
     * </ul><br>Note that here, we <b>explicitly</b> allow specifics
     * requests to be carried out.
     */
    public void evenPolicy(org.objectweb.proactive.Service service) {
        // if there is no writer and no write requests are before the first Read rq.
        if (writeCount == 0) {
            service.serveOldest(new MyRequestFilter("startRead", "startWrite"));
        }

        // If ther is no activity and the first request is startWrite
        if ((readCount == 0) && (writeCount == 0)) {
            service.serveOldest(new MyRequestFilter("startWrite", "startRead"));
        }
    }

    public void readerPolicy(org.objectweb.proactive.Service service) {
        // If there is no activity, serve the first READER
        if (writeCount == 0) {
            // If there is a waiting reader, let him come im
            service.serveOldest("startRead");
        }
        if ((readCount == 0) && (writeCount == 0)) {
            // Serve the writers in priority
            service.serveOldest("startWrite");
        }
    }

    public void writerPolicy(org.objectweb.proactive.Service service) {
        // If there is no activity, serve the first READER
        if ((readCount == 0) && (writeCount == 0)) {
            // Serve the writers in priority
            service.serveOldest("startWrite");
        }
        if (writeCount == 0) {
            // If there is a waiting reader, let him come im
            service.serveOldest("startRead");
        }
    }

    /**
     * The main synchronization method
     */
    public void runActivity(org.objectweb.proactive.Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);

        // Loops over lifetime...
        while (!done) {
            // Allows policy changes
            service.serveOldest("setPolicy");
            switch (policy) {
            case WRITER_POLICY:
                writerPolicy(service);
                break;
            case READER_POLICY:
                readerPolicy(service);
                break;
            case DEFAULT_POLICY: // Default policy
            default: // We never know..
                evenPolicy(service);
                break;
            }

            // Allow endXX requests
            service.serveOldest("endRead");
            service.serveOldest("endWrite");
            service.waitForRequest(); // Passive wait.
        }
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //

    /**
     * Filter that will accept the first Request for methodA only if there is no Request for method B
     * before it.
     */
    private class MyRequestFilter implements org.objectweb.proactive.core.body.request.RequestFilter {
        private String methodNameA;
        private String methodNameB;
        private boolean foundMethodB;

        public MyRequestFilter(String methodNameA, String methodNameB) {
            this.methodNameA = methodNameA;
            this.methodNameB = methodNameB;
        }

        public boolean acceptRequest(
            org.objectweb.proactive.core.body.request.Request request) {
            if (foundMethodB) {
                return false;
            }
            String methodName = request.getMethodName();
            if (methodName.equals(methodNameA)) {
                return true;
            }
            foundMethodB = methodName.equals(methodNameB);
            return false;
        }
    }
}
