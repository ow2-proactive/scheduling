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
package org.objectweb.proactive.ext.mixedlocation;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.ext.locationserver.LocationServer;

public class RequestWithMixedLocation extends RequestImpl implements java.io.Serializable {

  private static final int MAX_TRIES = 30;
  private static int counter = 0;

  private int tries;
  transient protected LocationServer server;

  public RequestWithMixedLocation(MethodCall methodCall, UniversalBody sender, boolean isOneWay, long nextSequenceID, LocationServer server) {
    super(methodCall, sender, isOneWay, nextSequenceID);
    System.out.println("RequestWithMixedLocation.RequestWithMixedLocation " + ++counter);
    this.server = server;
  }

  protected void sendRequest(UniversalBody destinationBody) throws java.io.IOException {
    System.out.println("RequestWithMixedLocation: sending to universal " + counter);
    try {
      destinationBody.receiveRequest(this);
    } catch (Exception e) {
      //  e.printStackTrace();
      this.backupSolution(destinationBody);
    }
  }

  /**
   * Implements the backup solution
   */
  protected void backupSolution(UniversalBody destinationBody) throws java.io.IOException {
    boolean ok = false;
    tries = 0;

    System.out.println("RequestWithMixedLocationr: backupSolution() contacting server " + server);
    System.out.println("RequestWithMixedLocation.backupSolution() : looking for " + destinationBody);
    //get the new location from the server
    while (!ok && (tries < MAX_TRIES)) {
      UniversalBody mobile = (UniversalBody) server.searchObject(destinationBody.getID());
      System.out.println("RequestWithMixedLocation: backupSolution() server has sent an answer");
      //we want to bypass the stub/proxy
      UniversalBody newDestinationBody = (UniversalBody) ((FutureProxy) ((StubObject) mobile).getProxy()).getResult();
      // !!!!
      // !!!! should put a counter here to stop calling if continuously failing
      // !!!!
      try {
        // sendRequest(newDestinationBody);
        newDestinationBody.receiveRequest(this);
        //everything went fine, we have to update the current location of the object
        //so that next requests don't go through the server
        System.out.println("RequestWithMixedLocation: backupSolution() updating location");
        if (sender != null) {
          sender.updateLocation(newDestinationBody.getID(), newDestinationBody.getRemoteAdapter());
        }
        ok = true;
      } catch (Exception e) {
        System.out.println("RequestWithMixedLocation: backupSolution() failed");
        tries++;
        try {
          Thread.sleep(500);
        } catch (Exception e2) {
          e2.printStackTrace();
        }
      }
    }
  }
}