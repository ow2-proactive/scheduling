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
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.migration.MigrationManagerImpl;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.node.Node;

public class MigrationManagerWithLocationServer extends MigrationManagerImpl {

transient  private LocationServer locationServer;



  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public MigrationManagerWithLocationServer() {
  }

 public MigrationManagerWithLocationServer(LocationServer locationServer) {
     this.locationServer=locationServer;
  }




  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
  
  /**
   * update our location on the Location Server
   */
  public void updateLocation(Body body) {
    if (locationServer != null) {
      //System.out.println("Updating location with thixs stub " + ProActive.getStubForBody(this.body)); 
      locationServer.updateLocation(body.getID(), body.getRemoteAdapter());
    }
  }
  

  //
  // -- Implements MigrationManager -----------------------------------------------
  //
  
  public UniversalBody migrateTo(Node node, Body body) throws MigrationException {
  	 locationServer = null;
    UniversalBody remoteBody = super.migrateTo(node,body);
   
    return remoteBody;
  }

  public void startingAfterMigration(Body body) {
    //we update our location
    super.startingAfterMigration(body);
    updateLocation(body);
  }
  
  public RequestReceiver createRequestReceiver(UniversalBody remoteBody, RequestReceiver currentRequestReceiver) {
    return new BouncingRequestReceiver();
  }
  
  public ReplyReceiver createReplyReceiver(UniversalBody remoteBody, ReplyReceiver currentReplyReceiver) {
    return currentReplyReceiver;
  }
}
