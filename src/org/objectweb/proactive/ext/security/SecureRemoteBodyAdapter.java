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
package org.objectweb.proactive.ext.security;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter;


import java.rmi.RemoteException;

/**
 *   An adapter for a RemoteBody to be able to receive remote calls. This helps isolate RMI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to another remote objects library.
 */
public class SecureRemoteBodyAdapter extends RemoteBodyAdapter {


  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public SecureRemoteBodyAdapter() {
  }


  public SecureRemoteBodyAdapter(SecureRemoteBody remoteBody) throws ProActiveException {
    super(remoteBody);
  }
  
  
  public SecureRemoteBodyAdapter(SecureBody body) throws ProActiveException {
    try {
      this.proxiedRemoteBody = new SecureRemoteBodyImpl(body);
    } catch (java.rmi.RemoteException e) {
      throw new ProActiveException(e);
    }
    this.bodyID = body.getID();
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  

  //
  // -- PRIVATE METHODS -----------------------------------------------
  //
  
}
