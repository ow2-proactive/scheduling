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
package org.objectweb.proactive.ic2d.spy;

import org.objectweb.proactive.core.UniqueID;

public class BodyCreationSpyEvent extends BodySpyEvent implements java.io.Serializable {

  /** The name of the class */
  protected String className;

  /** nodename */
  protected String nodeURL;

  
  public BodyCreationSpyEvent(UniqueID bodyID, String nodeURL, String className) {
    this(bodyID, nodeURL, className, false);
  }
  
  public BodyCreationSpyEvent(UniqueID bodyID, String nodeURL, String className, boolean isActive) {
    super(BODY_CREATION_EVENT_TYPE, bodyID, isActive, true);
    this.className = className;
    this.nodeURL = nodeURL;
  }
  
  public String getClassName() { return className; }
  public String getNodeURL() { return nodeURL; }

  public String toString() {
    return super.toString() + "\n\tnodeName:" + nodeURL + " Type: NEW OBJECT class: " + className;
  }
}