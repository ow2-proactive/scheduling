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
package org.objectweb.proactive.core.descriptor.xml;

import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.io.Attributes;

/**
 * This class receives deployment events
 *
 * @author       Lionel Mestre
 * @version      1.0
 */
class PathHandler extends BasicUnmarshaller {

  //
  //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
  //
  private static final String TYPE_ATTRIBUTE = "type";
  private static final String ABSOLUTE_TYPE = "absolute";
  private static final String RELATIVE_TYPE = "relative";
  private static final String DEFAULT_TYPE = ABSOLUTE_TYPE;

  private static final String ORIGIN_ATTRIBUTE = "origin";
  private static final String USER_HOME_ORIGIN = "userHome";
  private static final String WORKING_DIRECTORY_ORIGIN = "workingDirectory";
  private static final String FROM_CLASSPATH_ORIGIN = "fromClasspath";
  private static final String DEFAULT_ORIGIN = USER_HOME_ORIGIN;

  private static final String VALUE_ATTRIBUTE = "value";

  private static final String userDir = System.getProperty("user.dir");
  private static final String userHome = System.getProperty("user.home");
  private static final String javaHome = System.getProperty("java.home");
  private static final String pathSeparator = System.getProperty("path.separator");
  private static final String fileSeparator = System.getProperty("file.separator");


  //
  //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
  //

  public PathHandler() {
  }


  //
  //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
  //

  public Object getResultObject() throws org.xml.sax.SAXException {
    return super.getResultObject();
  }

  public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
    // read from XML
    String type = attributes.getValue(TYPE_ATTRIBUTE);
    if (! checkNonEmpty(type)) type = DEFAULT_TYPE;
    String origin = attributes.getValue(ORIGIN_ATTRIBUTE);
    if (! checkNonEmpty(origin)) origin = DEFAULT_ORIGIN;
    String value = attributes.getValue(VALUE_ATTRIBUTE);
    System.out.println("Found Path Element type="+type+" origin="+origin+" value="+value);
    if (! checkNonEmpty(value)) throw new org.xml.sax.SAXException("Path element defined without a value");
    // build the associated string
    if (type.equals(ABSOLUTE_TYPE)) {
      setResultObject(value);
    } else if (type.equals(RELATIVE_TYPE)) {
      if (origin.equals(USER_HOME_ORIGIN)) {
        setResultObject(resolvePath(userHome, value));
      } else if (origin.equals(WORKING_DIRECTORY_ORIGIN)) {
        setResultObject(resolvePath(userDir, value));
      } else if (origin.equals(FROM_CLASSPATH_ORIGIN)) {
        setResultObject(resolvePathFromClasspath(value));
      } else {
        throw new org.xml.sax.SAXException("Relative Path element defined with an unknown origin="+origin);
      }
    } else {
      throw new org.xml.sax.SAXException("Path element defined with an unknown type="+type);
    }
  }


  //
  //  ----- PRIVATE METHODS -----------------------------------------------------------------------------------
  //

  private String resolvePath(String origin, String value) {
    java.io.File originDirectory = new java.io.File(origin);
    java.io.File file = new java.io.File(originDirectory, value);
    return file.getAbsolutePath();
  }

  private String resolvePathFromClasspath(String value) {
    ClassLoader cl = this.getClass().getClassLoader();
    java.net.URL url = cl.getResource(value);
    return url.getPath();
  }


  //
  //  ----- INNER CLASSES -----------------------------------------------------------------------------------
  //


}