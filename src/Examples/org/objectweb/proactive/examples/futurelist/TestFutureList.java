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
package org.objectweb.proactive.examples.futurelist;

import org.objectweb.proactive.core.config.ProActiveConfiguration;


public class TestFutureList {
    public TestFutureList() {
        super();
    }

    public static void main(String[] args) {
        BlockedObject blockedO = null;
        FutureReceiver tf = null;

        if (args.length < 2) {
            System.err.println(
                "Usage: TestFutureList hostName1/nodeName1 hostName2/nodeName2");
            System.exit(-1);
        }
        ProActiveConfiguration.load();

        System.out.println(
            "This is designed to test the use of the future list");

        try {
            blockedO = (BlockedObject) org.objectweb.proactive.api.PAActiveObject.newActive(BlockedObject.class.getName(),
                    null, args[0]);
            tf = (FutureReceiver) org.objectweb.proactive.api.PAActiveObject.newActive(FutureReceiver.class.getName(),
                    null, args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //we pass the reference to our blocked object
        tf.setBlockedObject(blockedO);

        //and ask the creation of a future list
        tf.createFutureList();

        //now we start the future request
        //thus creating futures un tf
        tf.getFutureAndAddToFutureList();
        tf.getFutureAndAddToFutureList();
        tf.getFutureAndAddToFutureList();
        tf.getFutureAndAddToFutureList();

        System.out.println("*** Sleeping for 5 seconds");

        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        tf.displayAwaited();
        tf.displayAllAwaited();
        tf.displayNoneAwaited();

        System.out.println("*** Sleeping for 1 seconds");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("*** Asking the reply to the futures");
        //tf.unblockOtherObjectAndWaitAll();	
        //tf.unblockOtherObjectAndWaitOne();	
        tf.unblockOtherObject();
        tf.waitAndDisplayAllFuture();
        // 	tf.waitAndDisplayOneFuture();	
        // 	tf.waitAndDisplayOneFuture();
        // 	tf.waitAndDisplayOneFuture();
        // 	tf.waitAndDisplayOneFuture();
        //tf.waitAllFuture();
        System.out.println("*** Sleeping for 5 seconds");
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        tf.displayAwaited();
        tf.displayAllAwaited();
        tf.displayNoneAwaited();
        tf.displayAllFutures();
        // 	//now we ask for the display of the futures
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("*** Test over");
    }
}
