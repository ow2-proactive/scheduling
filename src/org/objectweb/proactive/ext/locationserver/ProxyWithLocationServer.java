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
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveProperties;

public class ProxyWithLocationServer extends UniversalBodyProxy {

  protected LocationServer server = null;

  public ProxyWithLocationServer(ConstructorCall c, Object[] p) throws ProActiveException {
    super(c, p);
  }

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  
    protected void sendRequestInternal(MethodCall methodCall, Future future, Body sourceBody) throws java.io.IOException {
      try {
        super.sendRequestInternal(methodCall, future, sourceBody);
      } catch (java.io.IOException e) {
        tryBackup(methodCall, future, sourceBody);
      }
    }


  /**
   * Implements the backup solution for the remoteBody
   */
  protected void tryBackup(MethodCall methodCall, Future future, Body sourceBody) throws java.io.IOException {
    LocationServer server = getLocationServer(sourceBody);
    //	System.out.println("ProxyWithLocationServer: tryBackupRemote() for request " + r.getMethodName() + " contacting server " + server);
    long startTime = System.currentTimeMillis();
    //get the new location from the server
    UniversalBody mobile = (UniversalBody)server.searchObject(bodyID);
    //	System.out.println("ProxyWithLocationServer: tryBackupRemote() server has sent an answer");
    //we want to bypass the stub/proxy
    this.universalBody = (UniversalBody)((FutureProxy)((StubObject)mobile).getProxy()).getResult();
    sendRequestInternal(methodCall, future, sourceBody);
    long endTime = System.currentTimeMillis();
    System.out.println("ProxyWithLocationServer: the communication took " + (endTime - startTime));
  }



  /**
   * Try to get the location server from our migration manager. 
   * If this fails, try using the proactive.locationserver property
   */
  public LocationServer getLocationServer(Body callingBody) {
    if (server != null) return server;
    LocationServer tmp = null;
    //	System.out.println("ProxyWithLocationServer: getLocationServer started");
    if (callingBody instanceof BodyWithLocationServer) {
	//      tmp = ((BodyWithLocationServer)callingBody).getServer();
    } else {
      //	System.out.println("ProxyWithLocationServer: getLocationServer started - 2");	
      String serverClass = ProActiveProperties.getLocationServerClass();
      String serverRmiName = ProActiveProperties.getLocationServerRmi();
      // System.out.println("ProxyWithLocationServer: getLocationServer using properties");
      try {
        tmp = (LocationServer)ProActive.lookupActive(serverClass, serverRmiName);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    server = tmp;
    return server;
  }
}
