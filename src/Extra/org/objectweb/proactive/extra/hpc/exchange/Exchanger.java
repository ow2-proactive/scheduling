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

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;


/**
 * Exchange operation offers a way to exchange data between two Active Objects in an efficient
 * manner.<br/> To use it, you have to specify different variables :
 * <ul>
 * <li> a <i>tag</i> which is an unique identifier for the exchange operation in case of multiple
 * exchanges are performed in parallel,
 * <li> a <i>reference</i> to the other Active Object you want to exchange data with,
 * <li> a <i>reference to the data source</i> you want to send to the other,
 * <li> a <i>reference to a location</i> where received data will be put,
 * <li> the <i>amount of data</i> you want to exchange (must be symmetric).
 * </ul>
 * Here is an example of usage :<br/> <code><pre>
 * public class AO1 {
 *   private int[] myArray;
 *   (...)
 *   public void foo() {
 *     Exchanger exchanger = Exchanger.getExchanger();
 *     myArray = new int[] {1,2,0,0};
 *     // Before : myArray = [1, 2, 0, 0]
 *     exchanger.exchange(&quot;myExch&quot;, ao2, myArray, 0, myArray, 2, 2); // Synchronize and exchange
 *     // After : myArray = [1, 2, 3, 4]
 *   }
 * }
 * 
 * public class AO2 {
 *   private int[] myArray;
 *   (...)
 *   public void bar() {
 *     Exchanger exchanger = Exchanger.getExchanger();
 *     myArray = new int[] {0,0,3,4};
 *     // Before : myArray = [1, 2, 0, 0]
 *     exchanger.exchange(&quot;myExch&quot;, ao1, myArray, 2, myArray, 0, 2); // Synchronize and exchange
 *     // After : myArray = [1, 2, 3, 4]
 *   }
 * }
 * </pre></code>
 * 
 */
public class Exchanger {

    private ExchangeManager manager;

    private Exchanger() {
        manager = ExchangeManager.getExchangeManager();
    }

    /**
     * Retrieve an instance of the Exchanger of local body. Must be invoked from an active object.
     * 
     * @return an instance of the Exchanger
     */
    public static Exchanger getExchanger() {
        return new Exchanger();
    }

    /**
     * Performs an exchange on a byte array between to Active Objects.
     * 
     * @param tag
     * @param destAO
     * @param srcArray
     * @param srcOffset
     * @param dstArray
     * @param dstOffset
     * @param len
     */
    public void exchange(String tag, Object destAO, byte[] srcArray, int srcOffset, byte[] dstArray,
            int dstOffset, int len) {
        manager.exchange(tag.hashCode(), destAO, srcArray, srcOffset, dstArray, dstOffset, len);
    }

    /**
     * Performs an exchange on a byte array between to Active Objects.
     * 
     * @param tag
     * @param destRank
     * @param srcArray
     * @param srcOffset
     * @param dstArray
     * @param dstOffset
     * @param len
     */
    public void exchange(String tag, int destRank, byte[] srcArray, int srcOffset, byte[] dstArray,
            int dstOffset, int len) {
        Object destAO = PAGroup.get(PASPMD.getSPMDGroup(), destRank);
        manager.exchange(tag.hashCode(), destAO, srcArray, srcOffset, dstArray, dstOffset, len);
    }

    /**
     * Performs an exchange on a double array between to Active Objects.
     * 
     * @param tag
     * @param destAO
     * @param srcArray
     * @param srcOffset
     * @param dstArray
     * @param dstOffset
     * @param len
     */
    public void exchange(String tag, Object destAO, double[] srcArray, int srcOffset, double[] dstArray,
            int dstOffset, int len) {
        manager.exchange(tag.hashCode(), destAO, srcArray, srcOffset, dstArray, dstOffset, len);
    }

    /**
     * Performs an exchange on a double array between to Active Objects.
     * 
     * @param tag
     * @param destRank
     * @param srcArray
     * @param srcOffset
     * @param dstArray
     * @param dstOffset
     * @param len
     */
    public void exchange(String tag, int destRank, double[] srcArray, int srcOffset, double[] dstArray,
            int dstOffset, int len) {
        Object destAO = PAGroup.get(PASPMD.getSPMDGroup(), destRank);
        manager.exchange(tag.hashCode(), destAO, srcArray, srcOffset, dstArray, dstOffset, len);
    }

    /**
     * Performs an exchange on an integer array between to Active Objects.
     * 
     * @param tag
     * @param destAO
     * @param srcArray
     * @param srcOffset
     * @param dstArray
     * @param dstOffset
     * @param len
     */
    public void exchange(String tag, Object destAO, int[] srcArray, int srcOffset, int[] dstArray,
            int dstOffset, int len) {
        manager.exchange(tag.hashCode(), destAO, srcArray, srcOffset, dstArray, dstOffset, len);
    }

    /**
     * Performs an exchange on an integer array between to Active Objects.
     * 
     * @param tag
     * @param destRank
     * @param srcArray
     * @param srcOffset
     * @param dstArray
     * @param dstOffset
     * @param len
     */
    public void exchange(String tag, int destRank, int[] srcArray, int srcOffset, int[] dstArray,
            int dstOffset, int len) {
        Object destAO = PAGroup.get(PASPMD.getSPMDGroup(), destRank);
        manager.exchange(tag.hashCode(), destAO, srcArray, srcOffset, dstArray, dstOffset, len);
    }

    /**
     * Performs an exchange on a complex structure of doubles between to Active Objects.
     * 
     * @param tag
     * @param destAO
     * @param src
     * @param dst
     */
    public void exchange(String tag, Object destAO, ExchangeableDouble src, ExchangeableDouble dst) {
        manager.exchange(tag.hashCode(), destAO, src, dst);
    }

    /**
     * Performs an exchange on a complex structure of doubles between to Active Objects.
     * 
     * @param tag
     * @param destRank
     * @param src
     * @param dst
     */
    public void exchange(String tag, int destRank, ExchangeableDouble src, ExchangeableDouble dst) {
        Object destAO = PAGroup.get(PASPMD.getSPMDGroup(), destRank);
        manager.exchange(tag.hashCode(), destAO, src, dst);
    }
}
