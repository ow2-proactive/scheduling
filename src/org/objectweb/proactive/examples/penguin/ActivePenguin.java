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
package org.objectweb.proactive.examples.penguin;

/**
 *   The root of all interfaces that declare an active object behavior. If a reifiable
 *   class implements this interface, any of its reified instances will by default use
 *   a BodyProxy proxy and an Body body. It is the responsibily of interfaces
 *   extending Active to override PROXY_CLASS_NAME and BODY_CLASS_NAME
 */
public interface ActivePenguin extends org.objectweb.proactive.Active {

  /**
   *   The name of the default body class used for active instances of classes
   *   implementing ActiveCompagnon.
   */
  public static String BODY_CLASS_NAME = "org.objectweb.proactive.ext.locationserver.BodyWithLocationServer";
}