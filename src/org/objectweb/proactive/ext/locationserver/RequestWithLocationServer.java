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
package org.objectweb.proactive.ext.locationserver;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;


public class RequestWithLocationServer extends RequestImpl implements java.io.Serializable {

  protected transient LocationServer server;

  public RequestWithLocationServer(MethodCall methodCall, UniversalBody sender, boolean isOneWay, long nextSequenceID, LocationServer server) {
    super(methodCall, sender, isOneWay, nextSequenceID);
    this.server = server;
  }


  protected void sendRequest(UniversalBody destinationBody) throws java.io.IOException {
    System.out.println("RequestSenderWithLocationServer: sending to universal");
    try {
      destinationBody.receiveRequest(this);
    } catch (java.io.IOException e) {
      this.backupSolution(destinationBody);
    }
  }



  /**
   * Implements the backup solution
   */
  protected void backupSolution(UniversalBody destinationBody) throws java.io.IOException {
    System.out.println("RequestSenderWithLocationServer: backupSolution() contacting server " + server); 
    //get the new location from the server
    UniversalBody mobile = (UniversalBody)server.searchObject(destinationBody.getID());
    System.out.println("RequestSenderWithLocationServer: backupSolution() server has sent an answer");
    //we want to bypass the stub/proxy
    UniversalBody newDestinationBody = (UniversalBody)((FutureProxy)((StubObject)mobile).getProxy()).getResult();
    // !!!!
    // !!!! should put a counter here to stop calling if continuously failing
    // !!!!
    sendRequest(newDestinationBody);
    //everything went fine, we have to update the current location of the object
    //so that next requests don't go through the server
    System.out.println("RequestSenderWithLocationServer: backupSolution() updating location");
    if (sender != null) {
      sender.updateLocation(newDestinationBody.getID(), newDestinationBody.getRemoteAdapter());
    }
  }

}
