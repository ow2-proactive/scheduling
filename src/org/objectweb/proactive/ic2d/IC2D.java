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
package org.objectweb.proactive.ic2d;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.ic2d.data.IC2DObject;
import org.objectweb.proactive.ic2d.data.WorldObject;
import org.objectweb.proactive.ic2d.gui.IC2DFrame;

/**
 * <p>
 * This class is the main entry to the application IC2D allowing to start it with 
 * a new JVM.
 * </p><p>
 * This class has a main method and can be used directly from the java command.<br>
 * &nbsp;&nbsp;&nbsp;java org.objectweb.proactive.ic2d.IC2D
 * </p> 
 *
 * @author  ProActive Team
 * @version 1.0,  2002/03/21
 * @since   ProActive 0.9
 *
 */
public class IC2D {

  public final static int NOTHING = 0;
  public final static int GLOBUS = 1;
  public final static int RSH = 2;
  
  
  /**
   * Main startup. 
   */
  public static void main(String args[]) {
    String[] hosts = null;
    if (args.length > 0) {
      hosts = readPropertiesFile(args[0]);
    }
    int options = NOTHING;
    /*
    try {
      Class.forName("org.globus.gram.Gram");
      System.out.println("Globus installed !");
      options = GLOBUS;
    } catch (ClassNotFoundException e) {
      System.out.println("Globus not installed !");
    }
    */
    try {
    	//Not sure following line is useful !
      Class.forName("org.objectweb.proactive.core.runtime.RuntimeFactory");
		RuntimeFactory.getDefaultRuntime();
	} catch (ProActiveException e1) {
		e1.printStackTrace();
	}
   catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
    IC2DObject ic2dObject = new IC2DObject();
    new IC2DFrame(ic2dObject, options);
    if (hosts != null) {
      WorldObject worldObject = ic2dObject.getWorldObject();
      for (int i = 0; i<hosts.length; i++) {
        try {
        	// to do : should decide wether we want rmi as default protocol
          worldObject.addHostObject(hosts[i], System.getProperty("proactive.rmi"));
        } catch (java.rmi.RemoteException e) {
          System.out.println("Can't create the host "+hosts[i]+", e="+e);
        }
      }
    }
  }


  //
  // -- CONSTRUCTORS -----------------------------------------------
  //


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //



  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  /**
   * go
   */
  private static String[] readPropertiesFile(String filename) {
    try {
      java.io.File f = new java.io.File(filename);
      if (! f.canRead()) return null;
      byte[] b = getBytesFromInputStream(new java.io.FileInputStream(f));
      java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(new String(b));
      int n = tokenizer.countTokens();
      if (n == 0) return null;
      String[] result = new String[n];
      int i=0;
      while (tokenizer.hasMoreTokens()) {
        result[i++] = tokenizer.nextToken();
      }
      return result;
    } catch (java.io.IOException e) {
      return null;
    }
  }


  /**
   * Returns an array of bytes containing the bytecodes for
   * the class represented by the InputStream
   * @param in the inputstream of the class file
   * @return the bytecodes for the class
   * @exception java.io.IOException if the class cannot be read
   */
  private static byte[] getBytesFromInputStream(java.io.InputStream in)  throws java.io.IOException {
    java.io.DataInputStream din = new java.io.DataInputStream(in);
    byte[] bytecodes = new byte[in.available()];
    try {
      din.readFully(bytecodes);
    } finally {
      if (din != null) din.close();
    }
    return bytecodes;
  }


  //
  // -- INNER CLASSES -----------------------------------------------
  //

}
