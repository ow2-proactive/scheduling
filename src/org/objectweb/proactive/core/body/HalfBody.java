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
package org.objectweb.proactive.core.body;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestReceiver;

public class HalfBody extends AbstractBody {

  //
  // -- PRIVATE MEMBERS -----------------------------------------------
  //

  private static final String NAME = "Other thread";

  public synchronized static HalfBody getHalfBody() {
    return new HalfBody();
  }


  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  private HalfBody() {
    super(new Object(),"LOCAL");
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  //
  // -- implements LocalBody -----------------------------------------------
  //

  // All the following methods have no implementation

  public void receiveRequest(Request c) throws java.io.IOException {
    throw new ProActiveRuntimeException("The method 'receiveRequest' is not implemented in class HalfBody.");
  }


  //
  // -- implements Body -----------------------------------------------
  //

  public String getName() {
    return NAME;
  }

  // All the following methods have no implementation

  public Object getReifiedObject() {
    throw new ProActiveRuntimeException("The method 'getReifiedObject' is not implemented in class HalfBody.");
  }

  public void fifoPolicy() {
    throw new ProActiveRuntimeException("The method 'fifoPolicy' is not implemented in class HalfBody.");
  }


  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected ReplyReceiver createReplyReceiver() {
    return new org.objectweb.proactive.core.body.reply.ReplyReceiverImpl();
  }


  protected RequestReceiver createRequestReceiver() {
    return null;
  }


  protected BlockingRequestQueue createRequestQueue() {
    return null;
  }

}
