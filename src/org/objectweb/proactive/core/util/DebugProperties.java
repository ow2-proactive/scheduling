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
package org.objectweb.proactive.core.util;

import java.io.IOException;
import java.io.InputStream;

public class DebugProperties extends java.util.Properties {

  private static java.util.Properties defaultProperties;
  private static java.util.Properties currentProperties;



  //     private static final String DEBUG_BODY_TRACING = "debug.body.tracing";
  //     private static final String DEBUG_BODY_EXECUTE = "debug.body.execute";
  //     private static final String DEBUG_BODY_DEBUG = "debug.body.debug";
  //     private static final String DEBUG_BODY_THREAD = "debug.body.thread";
  //     private static final String DEBUG_BODY_SERIALIZE = "debug.body.serialize";
  //     private static final String DEBUG_BODY_CONSTRUCTOR = "debug.body.constructor";
  //     private static final String DEBUG_BODY_UPDATE_LOCATION = "debug.body.location";





  //    static
  //     {
  // 	DebugProperties.loadProperties ();
  //     }

  public DebugProperties() {
    // Sets the default properties set
    //DebugProperties.loadDefaultProperties ();
    Class cl;
    // Loads the user properties
    //currentProperties = loadUserProperties ();

    //		saveUserProperties ();
    try {
      // The file with the default properties sits next to the .class
      // file for the current class. Therefore, we can access it as a
      // resource, even if we do not know where the .class file sits
      cl = Class.forName("org.objectweb.proactive.utils.DebugProperties");

      InputStream in = cl.getResourceAsStream("defaultDebugProperties");
      if (in != null) {
        try {
          this.load(in);
        } catch (IOException e) {
          System.err.println("Cannot read default properties file: " + e);
          //	theDefaultProperties = createDefaultProperties ();
        } finally {
          try {
            in.close();
          } catch (IOException e) {
            System.err.println("Warning: cannot close default properties file.");
          }
        }
      } else {
        //System.err.println("Default properties file not found.");
        //	theDefaultProperties = createDefaultProperties ();
      }
    } catch (ClassNotFoundException e) {
      System.err.println("!?!? Cannot even load myself...");
      //	theDefaultProperties = createDefaultProperties ();
    }

    // 	return;
  }


  protected static java.util.Properties loadDefaultProperties() {
    // Loads default properties
    java.util.Properties theDefaultProperties = new java.util.Properties();
    Class cl;

    try {
      // The file with the default properties sits next to the .class
      // file for the current class. Therefore, we can access it as a
      // resource, even if we do not know where the .class file sits
      cl = Class.forName("org.objectweb.proactive.utils.DebugProperties");

      InputStream in = cl.getResourceAsStream("defaultDebugProperties");
      if (in != null) {
        try {
          theDefaultProperties.load(in);
        } catch (IOException e) {
          System.err.println("Cannot read default properties file: " + e);
          //	theDefaultProperties = createDefaultProperties ();
        } finally {
          try {
            in.close();
          } catch (IOException e) {
            System.err.println("Warning: cannot close default properties file.");
          }
        }
      } else {
        //System.err.println("Default properties file not found.");
        //	theDefaultProperties = createDefaultProperties ();
      }
    } catch (ClassNotFoundException e) {
      System.err.println("!?!? Cannot even load myself...");
      //	theDefaultProperties = createDefaultProperties ();
    }

    return theDefaultProperties;
  }
}
