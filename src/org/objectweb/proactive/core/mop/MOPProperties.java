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
package org.objectweb.proactive.core.mop;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class MOPProperties {

  private static Properties defaultProperties;
  private static Properties currentProperties;
  private static final String DEFAULT_PROPERTIES_FILE_NAME = "defaultProActiveProperties";
  private static final String USER_PROPERTIES_FILE_NAME = "userProActiveProperties";
  private static final String COMPILER_INHERITS_CLASSPATH_PROPERTY_NAME = "proactive.inherits";
  private static final String COMPILER_COMMAND_LINE_PROPERTY_NAME = "proactive.compiler";
  private static final String PROACTIVE_CLASSES_PROPERTY_NAME = "proactive.classes";
  private static final String PROACTIVE_OUTPUT_PROPERTY_NAME = "proactive.output";
  private static final String PROACTIVE_KEEPSOURCE_PROPERTY_NAME = "proactive.keepsource";
  private static final String PROACTIVE_STUBSONDEMAND_PROPERTY_NAME = "proactive.stubsondemand";
  private static final String PROACTIVE_CHECKSTUBDATE_PROPERTY_NAME = "proactive.checkstubdate";
  private static final String PROACTIVE_REIFYNONPUBLICMETHODS_PROPERTY_NAME = "proactive.reifynonpublicmethods";
  private static final String PROACTIVE_USELOCKFILES_PROPERTY_NAME = "proactive.uselockfiles";
  private static final String PROACTIVE_GENERATEBYTECODE_PROPERTY_NAME = "proactive.generatebytecode";

  // private static final String PROACTIVE_DEFAULT_NODEFACTORY = "proactive.defaultnodefactory";

  private boolean propertiesAlreadyLoaded = false;

  /**
   *	This method is called by the static initializer of class MOP
   */

  static {
    MOPProperties.loadProperties();
  }


  private static void loadProperties() {
    // Sets the default properties set
    defaultProperties = loadDefaultProperties();

    // Loads the user properties
    currentProperties = loadUserProperties();

    //		saveUserProperties ();
    //MOPProperties.addPropertiesToSystem(currentProperties);
    return;
  }


  protected static Properties loadUserProperties() {
    // We assume the default properties have already been loaded
    Properties theUserProperties = new Properties(defaultProperties);
    String filename = System.getProperty("user.home") + System.getProperty("file.separator") + USER_PROPERTIES_FILE_NAME;

    try {
      // Creates an inputstream for reading the user properties file
      FileInputStream in = new FileInputStream(filename);
      // Reads from the stream
      if (in != null) {
        try {
          theUserProperties.load(in);
        } catch (IOException e) {
          throw e;
        } finally {
          in.close();
        }
      }
    } catch (IOException e) {
      System.out.println("User properties file not found. Creating it using default properties.");
      saveUserProperties();
    }

    return theUserProperties;
  }


  // protected static void addPropertiesToSystem(Properties p)
  //     {
  // 	//Properties systemP = System.getProperties();

  // 	for (Enumeration e=p.propertyNames();e.hasMoreElements();)
  // 	    {
  // 		String s = (String) e.nextElement();
  // 		System.out.println("XXXX Adding property " +  s + " to the system");
 
  // 		System.setProperty(s, p.getProperty(s));
  // 	    }

  //     }

  public static void saveUserProperties() {
    String filename = System.getProperty("user.home") + System.getProperty("file.separator") + USER_PROPERTIES_FILE_NAME;

    try {
      FileOutputStream out = new FileOutputStream(filename);
      defaultProperties.store(out, "ProActive User Properties");
      System.out.println("User properties file written as " + filename);
      out.close();
    } catch (IOException e) {
      System.err.println("Cannot write user properties file: " + e);
    }

    return;
  }


  protected static Properties loadDefaultProperties() {
    // Loads default properties
    Properties theDefaultProperties = new Properties();
    // The file with the default properties sits next to the .class
    // file for the current class. Therefore, we can access it as a
    // resource, even if we do not know where the .class file sits
    Class cl = MOPProperties.class;
    InputStream in = cl.getResourceAsStream("defaultProActiveProperties");
    if (in != null) {
      try {
        theDefaultProperties.load(in);
      } catch (IOException e) {
        System.err.println("Cannot read default properties file: " + e);
        theDefaultProperties = createDefaultProperties();
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          System.err.println("Warning: cannot close default properties file.");
        }
      }
    } else {
      System.err.println("Default properties file not found.");
      theDefaultProperties = createDefaultProperties();
    }
    return theDefaultProperties;
  }


  protected static Properties createDefaultProperties() {
    // Creates a Properties object from scratch in case we cannot read
    // the default one from the disk
    Properties theDefaultProperties = new Properties();

    // By default, the compiler inhetits classpath setting from the application
    theDefaultProperties.setProperty(COMPILER_INHERITS_CLASSPATH_PROPERTY_NAME, "true");

    // The command-line argument for calling the compiler
    theDefaultProperties.setProperty(COMPILER_COMMAND_LINE_PROPERTY_NAME, "javac");

    // Where to find ProActive's classes (we should maybe implement a
    // smart guess here)
    theDefaultProperties.setProperty(PROACTIVE_CLASSES_PROPERTY_NAME, "");

    // Where ProActive should put the stub classes it generates
    String proactiveOutput = System.getProperty("user.home") + System.getProperty("file.separator") + "proactive-tmp";
    theDefaultProperties.setProperty(PROACTIVE_OUTPUT_PROPERTY_NAME, proactiveOutput);

    // Various 'boolean' properties
    theDefaultProperties.setProperty(PROACTIVE_KEEPSOURCE_PROPERTY_NAME, "false");
    theDefaultProperties.setProperty(PROACTIVE_STUBSONDEMAND_PROPERTY_NAME, "false");
    theDefaultProperties.setProperty(PROACTIVE_CHECKSTUBDATE_PROPERTY_NAME, "true");
    theDefaultProperties.setProperty(PROACTIVE_REIFYNONPUBLICMETHODS_PROPERTY_NAME, "false");
    theDefaultProperties.setProperty(PROACTIVE_USELOCKFILES_PROPERTY_NAME, "false");
    theDefaultProperties.setProperty(PROACTIVE_GENERATEBYTECODE_PROPERTY_NAME, "true");

    return theDefaultProperties;
  }


  /**
   *	Sets the value of the property COMPILER_INHERITS_CLASSPATH_PROPERTY_NAME
   */

  public static boolean getCompilerInheritsClasspath() {
    String value;
    value = currentProperties.getProperty(COMPILER_INHERITS_CLASSPATH_PROPERTY_NAME);
    return (Boolean.valueOf(value).booleanValue());
  }


  public static void setCompilerInheritsClasspath(String value) {
    currentProperties.setProperty(COMPILER_INHERITS_CLASSPATH_PROPERTY_NAME, value);
    return;
  }



  /**
   *	Sets the value of the property PROACTIVE_GENERATEBYTECODE_PROPERTY_NAME
   */

  public static boolean getGenerateBytecode() {
    String value;
    value = currentProperties.getProperty(PROACTIVE_GENERATEBYTECODE_PROPERTY_NAME);
    return (Boolean.valueOf(value).booleanValue());
  }


  public static void setGenerateBytecode(String value) {
    currentProperties.setProperty(PROACTIVE_GENERATEBYTECODE_PROPERTY_NAME, value);
    return;
  }


  /**
   *	Sets the value of the command-line argument for calling the compiler
   */

  public static String getCompilerCommandLine() {
    return currentProperties.getProperty(COMPILER_COMMAND_LINE_PROPERTY_NAME);
  }


  public static void setCompilerCommandLine(String value) {
    currentProperties.setProperty(COMPILER_COMMAND_LINE_PROPERTY_NAME, value);
    return;
  }


  /**
   *	Returns the directory where the MOP writes .java and .class files for
   *	stub classes. If the value of the property doe not end with a file separator
   * character, it is appended.
   */
  public static String getStubsOutputDirectory() {
    String result = currentProperties.getProperty(PROACTIVE_OUTPUT_PROPERTY_NAME);

    // If thre is no value for this property, use the default location       
    if (result.equals("")) {
      result = System.getProperty("user.home") + System.getProperty("file.separator") + "proactive-tmp";
    }

    if (!(result.endsWith(System.getProperty("file.separator")))) {
      result = result + System.getProperty("file.separator");
    }

    return result;
  }


  public static void setStubsOutputDirectory(String value) {
    currentProperties.setProperty(PROACTIVE_OUTPUT_PROPERTY_NAME, value);
    return;
  }


  /**
   *	Sets the value of the property proactive.checkstubdate. When this property
   *	is set to "true", the MOP's runtime checks the date of the class file for the
   * reified class the first time a reified instance of that class is required.
   * If this date is found to be earlier than the class file for the stub, the
   * stub is generated again because it may not be coherent with the class it is
   * supposed to reify.
   */

  public static void setCheckStubDate(String value) {
    currentProperties.setProperty(PROACTIVE_CHECKSTUBDATE_PROPERTY_NAME, value);
    return;
  }


  public static boolean getCheckStubDate() {
    String value = currentProperties.getProperty(PROACTIVE_CHECKSTUBDATE_PROPERTY_NAME);
    return (Boolean.valueOf(value).booleanValue());
  }


  /**
   *	Sets the value of the property proactive.stubsondemand. When this property
   *	is set to "true", the MOP's runtime generates stub classes 'on the fly' if
   * it cannot find such a stub class in the CLASSPATH. As the generation of the
   *	stub requires writing to the local file system, this option needs to be
   *	turned off in environments where this is not possible (applets for example)
   */

  public static void setStubsOnDemand(String value) {
    currentProperties.setProperty(PROACTIVE_STUBSONDEMAND_PROPERTY_NAME, value);
    return;
  }


  public static boolean getStubsOnDemand() {
    String value = currentProperties.getProperty(PROACTIVE_STUBSONDEMAND_PROPERTY_NAME);
    return (Boolean.valueOf(value).booleanValue());
  }


  /**
   *	Sets the value of the property proactive.keepsource. When this property
   *	is set to "true", the source file that is created by the MOP for its
   * stub file is kept, otherwise it is deleted after the compilation, even if
   * it fails.
   */

  public static void setKeepSource(boolean value) {
    MOPProperties.setKeepSource(new Boolean(value).toString());
    return;
  }


  public static void setKeepSource(String value) {
    currentProperties.setProperty(PROACTIVE_KEEPSOURCE_PROPERTY_NAME, value);
    return;
  }


  public static boolean getKeepSource() {
    String value = currentProperties.getProperty(PROACTIVE_KEEPSOURCE_PROPERTY_NAME);
    return (Boolean.valueOf(value).booleanValue());
  }


  /**
   *	Sets the value of the property proactive.reifynonpublicmethods. When this
   *	property is set to "true", protected and 'default' methods are reifed in
   * addition to public methods.
   */

  public static void setReifyNonPublicMethods(String value) {
    currentProperties.setProperty(PROACTIVE_REIFYNONPUBLICMETHODS_PROPERTY_NAME, value);
    return;
  }


  public static boolean getReifyNonPublicMethods() {
    String value = currentProperties.getProperty(PROACTIVE_REIFYNONPUBLICMETHODS_PROPERTY_NAME);
    return (Boolean.valueOf(value).booleanValue());
  }


  /**
   *	Sets the value of the property proactive.reifynonpublicmethods. When this
   *	property is set to "true", the MOP uses lock files in order to prevent two
   * different VMs from compiling the same stub class at the same time, for
   *	example if they share the same file system
   */

  public static void setUseLockFiles(String value) {
    currentProperties.setProperty(PROACTIVE_USELOCKFILES_PROPERTY_NAME, value);
    return;
  }


  public static boolean getUseLockFiles() {
    String value = currentProperties.getProperty(PROACTIVE_USELOCKFILES_PROPERTY_NAME);
    return (Boolean.valueOf(value).booleanValue());
  }
}
