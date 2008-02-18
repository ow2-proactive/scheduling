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
package org.objectweb.proactive.extra.hpc.exchange;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;


public class ExchangeManager {
    public static int DEFAULT_QUEUE_SIZE = 10;

    /**
     * An hash map to register the local managers according to their Thread ID (long)
     */
    private static HashMap<Integer, ExchangeManager> mngrHashMap = new HashMap<Integer, ExchangeManager>();

    /**
     * The rank number of the local SPMD object
     */
    private int myRank = -1;

    /**
     * The exchange primitive require an hashMap
     */
    private boolean firstExchange = true;
    private HashMap<Integer, BlockingQueue<ExchangeableArrayPointer>> exchQueue;
    private HashMap<Integer, BlockingQueue<Boolean>> exchToken;
    private Object exchSync = new Object();

    //
    // -- CONSTRUCTOR ----------------------------------------------------------
    //

    private ExchangeManager() {
    }

    //
    // --- FACTORY -------------------------------------------------------------
    //  

    /**
     * Retrieve the instance of {@link ExchangeManager} attached to the current {@link Body}.
     * 
     * @return an instance of {@link ExchangeManager}. There is one instance per {@link Body}.
     */
    protected static ExchangeManager getExchangeManager() {
        Body body = PAActiveObject.getBodyOnThis();
        if (!body.isActive()) {
            throw new RuntimeException("Must be invoked from an active object.");
        }
        return getExchangeManager(body.getID().hashCode());
    }

    /**
     * Retrieve the instance of the {@link ExchangeManager} according to the hash code of the
     * {@link Body}'s ID.
     * 
     * @param bodyIdHashCode
     * @return the instance of {@link ExchangeManager} attached to the current body
     */
    protected static ExchangeManager getExchangeManager(int bodyIdHashCode) {
        synchronized (mngrHashMap) {
            if (!mngrHashMap.containsKey(bodyIdHashCode)) {
                registerManager(new ExchangeManager(), bodyIdHashCode);
            }
            return mngrHashMap.get(bodyIdHashCode);
        }
    }

    //
    // --- PROTECTED METHODS ---------------------------------------------------
    //  

    /**
     * Get the rank number
     * 
     * @return the local rank number
     */
    protected int getMyRank() {
        if (myRank == -1) {
            myRank = PASPMD.getMyRank();
        }

        return myRank;
    }

    protected ExchangeableArrayPointer getExchangeableArrayPointer(int tagID) {
        try {
            synchronized (exchSync) {
                while ((exchQueue == null) || (exchQueue.get(tagID) == null)) {
                    exchSync.wait(100);
                }
                return exchQueue.get(tagID).take();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        return null;
    }

    protected void setReady(int tagID) {
        try {
            synchronized (exchSync) {
                BlockingQueue<Boolean> queue = exchToken.get(tagID);
                if (queue == null) {
                    queue = new ArrayBlockingQueue<Boolean>(1);
                    exchToken.put(tagID, queue);
                }
                queue.put(true);
                exchSync.notifyAll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void waitForReady(int tagID) {
        try {
            BlockingQueue<Boolean> queue = exchToken.get(tagID);
            synchronized (exchSync) {
                while (queue == null) {
                    exchSync.wait(100);
                    queue = exchToken.get(tagID);
                }
            }
            exchToken.get(tagID).take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void initReady() {
        exchToken = new HashMap<Integer, BlockingQueue<Boolean>>(PASPMD.getMySPMDGroupSize());
    }

    protected static void registerManager(ExchangeManager mngr, int bodyIdHashCode) {
        synchronized (mngrHashMap) {
            mngrHashMap.put(bodyIdHashCode, mngr);
            mngrHashMap.notifyAll();
        }
    }

    //
    // --- PROTECTED EXCHANGE METHODS -----------------------------------------
    //  

    protected void exchange(int tagID, Object target, byte[] srcArray, int srcOffset, byte[] dstArray,
            int dstOffset, int len) {
        Proxy dstProxy = ((StubObject) target).getProxy();
        int dstUID = ((UniversalBodyProxy) dstProxy).getBodyID().hashCode();
        ExchangeableArrayPointer arrayPointer = new ExchangeableArrayPointer(dstArray, dstOffset, len);
        RequestExchange req = RequestExchange.getRequestExchange(tagID, srcArray, srcOffset, len, dstUID);

        internalExchange(tagID, req, target, arrayPointer, dstProxy);
    }

    protected void exchange(int tagID, Object target, double[] srcArray, int srcOffset, double[] dstArray,
            int dstOffset, int len) {
        Proxy dstProxy = ((StubObject) target).getProxy();
        int dstUID = ((UniversalBodyProxy) dstProxy).getBodyID().hashCode();
        ExchangeableArrayPointer arrayPointer = new ExchangeableArrayPointer(dstArray, dstOffset, len);
        RequestExchange req = RequestExchange.getRequestExchange(tagID, srcArray, srcOffset, len, dstUID);

        internalExchange(tagID, req, target, arrayPointer, dstProxy);
    }

    protected void exchange(int tagID, Object target, int[] srcArray, int srcOffset, int[] dstArray,
            int dstOffset, int len) {
        Proxy dstProxy = ((StubObject) target).getProxy();
        int dstUID = ((UniversalBodyProxy) dstProxy).getBodyID().hashCode();
        ExchangeableArrayPointer arrayPointer = new ExchangeableArrayPointer(dstArray, dstOffset, len);
        RequestExchange req = RequestExchange.getRequestExchange(tagID, srcArray, srcOffset, len, dstUID);

        internalExchange(tagID, req, target, arrayPointer, dstProxy);
    }

    protected void exchange(int tagID, Object target, ExchangeableDouble src, ExchangeableDouble dst) {
        Proxy dstProxy = ((StubObject) target).getProxy();
        int dstUID = ((UniversalBodyProxy) dstProxy).getBodyID().hashCode();
        ExchangeableArrayPointer arrayPointer = new ExchangeableArrayPointer(dst);
        RequestExchange req = RequestExchange.getRequestExchange(tagID, arrayPointer, dstUID);

        internalExchange(tagID, req, target, arrayPointer, dstProxy);
    }

    //
    // --- PRIVATE METHODS ------------------------------------------------------
    //  

    private void internalExchange(int tagID, RequestExchange req, Object target,
            ExchangeableArrayPointer arrayPointer, Proxy dstProxy) {
        if (firstExchange) {
            firstExchange = false;
            registerManager(this, PAActiveObject.getBodyOnThis().getID().hashCode());
            exchQueue = new HashMap<Integer, BlockingQueue<ExchangeableArrayPointer>>(DEFAULT_QUEUE_SIZE);
            exchToken = new HashMap<Integer, BlockingQueue<Boolean>>(DEFAULT_QUEUE_SIZE);
        }

        try {
            // Enqueue the target array for the ExchangeRequest reception
            BlockingQueue<ExchangeableArrayPointer> queue = exchQueue.get(tagID);
            synchronized (exchSync) {
                if (queue == null) {
                    queue = new ArrayBlockingQueue<ExchangeableArrayPointer>(1);
                    exchQueue.put(tagID, queue);
                    exchSync.notifyAll();
                }
            }
            queue.put(arrayPointer);

            // Sends the ExchangeRequest to the target
            if (((UniversalBodyProxy) dstProxy).isLocal()) {
                // System.out.println("ExchangeManager.internalExchange(): Local way");
                ExchangeManager targetManager = getExchangeManager(((UniversalBodyProxy) dstProxy)
                        .getBodyID().hashCode());

                ExchangeableArrayPointer targetArray = targetManager.getExchangeableArrayPointer(tagID);

                switch (req.dataType) {
                    case ExchangeableArrayPointer.BYTE_ARRAY:
                        System.arraycopy(req.byteArray, req.offsetArray, targetArray.getByteArray(),
                                targetArray.getOffset(), req.lenArray);

                        break;

                    case ExchangeableArrayPointer.DOUBLE_ARRAY:
                        System.arraycopy(req.doubleArray, req.offsetArray, targetArray.getDoubleArray(),
                                targetArray.getOffset(), req.lenArray);

                        break;

                    case ExchangeableArrayPointer.INT_ARRAY:
                        System.arraycopy(req.intArray, req.offsetArray, targetArray.getIntArray(),
                                targetArray.getOffset(), req.lenArray);

                        break;

                    case ExchangeableArrayPointer.EXCHANGEABLE_DOUBLE:
                        ExchangeableDouble src = req.exchangeableArrayPointer.getExchangeDouble();
                        ExchangeableDouble dst = targetArray.getExchangeDouble();
                        while (dst.hasNextPut() || src.hasNextGet()) {
                            // Why '||' ? Should raise an exception if gets and puts are not equals.
                            // Exchange operation must be symmetric.
                            dst.put(src.get());
                        }
                        break;
                }

                targetManager.setReady(tagID);
            } else {
                // System.out.println("ExchangeManager.internalExchange(): Distant way");
                req.send(((UniversalBodyProxy) dstProxy).getBody()); // ProActive call
            }

            // Waits for the reception of the ExchangeRequest from the target
            waitForReady(tagID);
        } catch (Throwable e) {
            System.err.println("Unable to perform the exchange operation.");
            e.printStackTrace();
        }
    }
}
