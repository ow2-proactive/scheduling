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

public class Debug {

  private static DebugProperties properties = new DebugProperties();

  //The static variables for the body
  static boolean bodyTracing;// = false;
  static boolean bodyExecute;// = false;
  static boolean bodyDebug;// = false;
  static boolean bodyThread;// = false;
  static boolean bodySerialize;// = false;
  static boolean bodyConstructor;// = true;// false;
  static boolean bodyUpdateLocation;// = false;
  static boolean bodyForward;

 
  //The static variables for the BodyProxy
  static boolean proxyBodyConstructor;// =false;
  static boolean proxyBodyDebug;// = false;
  static boolean proxyBodyTracing;// = false;
  static boolean proxyBodySerialize;// = false;
  static boolean proxyBodyFinalize;// = false;


  //The static variables for the ProxyForFutur
  static boolean proxyFutureConstructor;// = false;
  static boolean proxyFutureDebug;// = false;
  static boolean proxyFutureSerialize;// = false;

  //The static variables for ProActive
  static boolean proActiveDebug = false;

  //The static variables for MOP
  static boolean MOPDebug = false;
    
//The static variables for the RequestQueue
  static boolean requestQueueDebug = false;

  //The static variables for the MigrationStrategyImpl
  static boolean itineraryDebug = false;



  //The static variables for the Node
  static boolean nodeDebug = false;
  static boolean nodeRegister = false;
  static boolean nodeForwarderRegister;


  //The static variables for the NodeJini
  static boolean nodeJiniDebug = false;


  //The static variables for the MethodCall
  static boolean methodSerialize = false;
  static boolean methodExecute = false;

  //The static variables for the BodyMap
  static boolean locationTableDebug = false;


  static {
    //The static variables for the body
    Debug.bodyTracing = Boolean.valueOf(properties.getProperty("debug.body.tracing")).booleanValue();
    Debug.bodyExecute = Boolean.valueOf(properties.getProperty("debug.body.execute")).booleanValue();
    Debug.bodyDebug = Boolean.valueOf(properties.getProperty("debug.body.debug")).booleanValue();
    Debug.bodyThread = Boolean.valueOf(properties.getProperty("debug.body.thread")).booleanValue();
    Debug.bodySerialize = Boolean.valueOf(properties.getProperty("debug.body.serialize")).booleanValue();
    Debug.bodyConstructor = Boolean.valueOf(properties.getProperty("debug.body.constructor")).booleanValue();
    Debug.bodyUpdateLocation = Boolean.valueOf(properties.getProperty("debug.body.location")).booleanValue();
    Debug.bodyForward = Boolean.valueOf(properties.getProperty("debug.body.forward")).booleanValue();
    // System.out.println("BodyForward = " + bodyForward);
 

    Debug.proxyBodyConstructor = Boolean.valueOf(properties.getProperty("debug.proxy.constructor")).booleanValue();
    Debug.proxyBodyDebug = Boolean.valueOf(properties.getProperty("debug.proxy.debug")).booleanValue();
    Debug.proxyBodyTracing = Boolean.valueOf(properties.getProperty("debug.proxy.tracing")).booleanValue();
    Debug.proxyBodySerialize = Boolean.valueOf(properties.getProperty("debug.proxy.serialize")).booleanValue();
    Debug.proxyBodyFinalize = Boolean.valueOf(properties.getProperty("debug.proxy.finalize")).booleanValue();

    Debug.proxyFutureConstructor = Boolean.valueOf(properties.getProperty("debug.future.constructor")).booleanValue();
    Debug.proxyFutureDebug = Boolean.valueOf(properties.getProperty("debug.future.debug")).booleanValue();
    Debug.proxyFutureSerialize = Boolean.valueOf(properties.getProperty("debug.future.serialize")).booleanValue();

    Debug.proActiveDebug = Boolean.valueOf(properties.getProperty("debug.proactive.debug")).booleanValue();

    Debug.MOPDebug = Boolean.valueOf(properties.getProperty("debug.mop.debug")).booleanValue();

    Debug.requestQueueDebug = Boolean.valueOf(properties.getProperty("debug.requestline.debug")).booleanValue();

    Debug.itineraryDebug = Boolean.valueOf(properties.getProperty("debug.migrationStrategy.debug")).booleanValue();

    Debug.nodeDebug = Boolean.valueOf(properties.getProperty("debug.node.debug")).booleanValue();
    Debug.nodeRegister = Boolean.valueOf(properties.getProperty("debug.node.register")).booleanValue();
    Debug.nodeForwarderRegister = Boolean.valueOf(properties.getProperty("debug.node.registerForwarder")).booleanValue();

    Debug.nodeJiniDebug = Boolean.valueOf(properties.getProperty("debug.nodejini.debug")).booleanValue();

    Debug.methodSerialize = Boolean.valueOf(properties.getProperty("debug.method.serialize")).booleanValue();
    Debug.methodExecute = Boolean.valueOf(properties.getProperty("debug.method.execute")).booleanValue();

    Debug.locationTableDebug = Boolean.valueOf(properties.getProperty("debug.locationtable.debug")).booleanValue();
  }


  /**
   * Will display the string s if bodyTracing is set to false
   */
  public static void bodyTrace(String s) {
    if (bodyTracing)
      System.out.println(s);
    // 	if (properties.getProperty(debug.body.tracing))
    // 	    System.out.println(s);
  }


  public static void bodyExecute(String s) {
    if (bodyExecute)
      System.out.println(s);
  }


  public static void bodyDebug(String s) {
    if (bodyDebug)
      System.out.println(s);
  }


  public static void bodyThread(String s) {
    if (bodyThread)
      System.out.println(s);
  }


  public static void bodySerialize(String s) {
    if (bodySerialize)
      System.out.println(s);
  }


  public static void bodyConstructor(String s) {
    if (bodyConstructor)
      System.out.println(s);
  }


  public static void bodyUpdate(String s) {
    if (bodyUpdateLocation)
      System.out.println(s);
  }


  public static void bodyForward(String s) {
    if (bodyForward)
      System.out.println(s);
  }
 
 
  //   //The static variables for the BodyProxy
  //     static boolean proxyBodyConstructor =false;
  //     static boolean proxyBodyDebug = false;
  //     static boolean proxyBodyTracing = false;
  //     static boolean proxyBodySerialize = false;
  //     static boolean proxyBodyFinalize = false;
   
  public static void proxyBodyConstructor(String s) {
    if (proxyBodyConstructor)
      System.out.println(s);
  }


  public static void proxyBodyDebug(String s) {
    if (proxyBodyDebug)
      System.out.println(s);
  }


  public static void proxyBodyTracing(String s) {
    if (proxyBodyTracing)
      System.out.println(s);
  }


  public static void proxyBodySerialize(String s) {
    if (proxyBodySerialize)
      System.out.println(s);
  }


  public static void proxyBodyFinalize(String s) {
    if (proxyBodyFinalize)
      System.out.println(s);
  } //  //The static variables for MOP
  //     static boolean MOPDebug =false;
    

  //    public static void MOPDebug(String s)
  //     {
  //         if (MOPDebug) 
  //             System.out.println(s);
  //     }

   
  public static void proxyFutureConstructor(String s) {
    if (proxyFutureConstructor)
      System.out.println(s);
  }


  public static void proxyFutureDebug(String s) {
    if (proxyFutureDebug)
      System.out.println(s);
  }


  public static void proxyFutureSerialize(String s) {
    if (proxyFutureSerialize)
      System.out.println(s);
  }


  public static void proActiveDebug(String s) {
    if (proActiveDebug)
      System.out.println(s);
  }


  public static void MOPDebug(String s) {
    if (MOPDebug)
      System.out.println(s);
  }


  public static void requestQueueDebug(String s) {
    if (requestQueueDebug)
      System.out.println(s);
  }


  public static void itineraryDebug(String s) {
    if (itineraryDebug)
      System.out.println(s);
  }


  public static void nodeDebug(String s) {
    if (nodeDebug)
      System.out.println(s);
  }


  public static void nodeRegister(String s) {
    if (nodeRegister)
      System.out.println(s);
  }


  public static void nodeForwarderRegister(String s) {
    if (nodeForwarderRegister)
      System.out.println(s);
  }


  public static void nodeJiniDebug(String s) {
    if (nodeJiniDebug)
      System.out.println(s);
  }


  public static void methodSerialize(String s) {
    if (methodSerialize)
      System.out.println(s);
  }


  public static void methodExecute(String s) {
    if (methodExecute)
      System.out.println(s);
  }


  public static void locationTableDebug(String s) {
    if (locationTableDebug)
      System.out.println(s);
  }
}
