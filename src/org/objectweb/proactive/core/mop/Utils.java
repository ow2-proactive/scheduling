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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * This class contains static convenience and utility methods
 */
abstract class Utils extends Object {

  /**
   * Static variables
   */

  public static Class JAVA_LANG_NUMBER;
  public static Class JAVA_LANG_CHARACTER;
  public static Class JAVA_LANG_BOOLEAN;
  public static Class JAVA_LANG_VOID;
  public static Class JAVA_LANG_RUNTIMEEXCEPTION;
  public static Class JAVA_LANG_EXCEPTION;
  public static Class JAVA_LANG_THROWABLE;
  public static String STUB_DEFAULT_PREFIX = "Stub_";


  static {
    try {
      JAVA_LANG_NUMBER = MOP.forName("java.lang.Number");
    } catch (ClassNotFoundException e) {
      JAVA_LANG_NUMBER = null;
      System.err.println("Static initializer in class org.objectweb.proactive.core.mop.Utils: Cannot load classes in java.lang.*");
    }

    try {
      JAVA_LANG_CHARACTER = MOP.forName("java.lang.Character");
    } catch (ClassNotFoundException e) {
      JAVA_LANG_CHARACTER = null;
      System.err.println("Static initializer in class org.objectweb.proactive.core.mop.Utils: Cannot load classes in java.lang.*");
    }

    try {
      JAVA_LANG_BOOLEAN = MOP.forName("java.lang.Boolean");
    } catch (ClassNotFoundException e) {
      JAVA_LANG_BOOLEAN = null;
      System.err.println("Static initializer in class org.objectweb.proactive.core.mop.Utils: Cannot load classes in java.lang.*");
    }

    try {
      JAVA_LANG_VOID = MOP.forName("java.lang.Void");
    } catch (ClassNotFoundException e) {
      JAVA_LANG_VOID = null;
      System.err.println("Static initializer in class org.objectweb.proactive.core.mop.Utils: Cannot load classes in java.lang.*");
    }

    try {
      JAVA_LANG_RUNTIMEEXCEPTION = MOP.forName("java.lang.RuntimeException");
    } catch (ClassNotFoundException e) {
      JAVA_LANG_RUNTIMEEXCEPTION = null;
      System.err.println("Static initializer in class org.objectweb.proactive.core.mop.Utils: Cannot load classes in java.lang.*");
    }

    try {
      JAVA_LANG_EXCEPTION = MOP.forName("java.lang.Exception");
    } catch (ClassNotFoundException e) {
      JAVA_LANG_EXCEPTION = null;
      System.err.println("Static initializer in class org.objectweb.proactive.core.mop.Utils: Cannot load classes in java.lang.*");
    }

    try {
      JAVA_LANG_THROWABLE = MOP.forName("java.lang.Throwable");
    } catch (ClassNotFoundException e) {
      JAVA_LANG_THROWABLE = null;
      System.err.println("Static initializer in class org.objectweb.proactive.core.mop.Utils: Cannot load classes in java.lang.*");
    }
  }

  /**
   * Static methods
   */

  /**
   * Removes the keyword 'native' from the String given as argument.
   *
   * We assume there is only one occurence of 'native' in the string.
   *
   * @return the input String minus the first occurence of 'native'.
   * @param  in The String the keyword 'native' is to be removed from.
   */
  static public String getRidOfNative(String in) {
    String result;
    int leftindex, rightindex;

    leftindex = in.indexOf("native");
    if (leftindex == -1)
      return in;
    rightindex = leftindex + 6;

    result = in.substring(0, leftindex) + in.substring(rightindex, in.length());
    return result;
  }


  static public String getRidOfAbstract(String in) {
    String result;
    int leftindex, rightindex;

    leftindex = in.indexOf("abstract");
    if (leftindex == -1)
      return in;
    rightindex = leftindex + 8;

    result = in.substring(0, leftindex) + in.substring(rightindex, in.length());
    return result;
  }


  static public String getRidOfNativeAndAbstract(String in) {
    String s = in;
    s = getRidOfAbstract(s);
    return getRidOfNative(s);
  }


  /**
   * Checks if the given method can be reified.
   *
   * Criteria for NOT being reifiable are :
   * <UL>
   * <LI> method is final
   * <LI> method is static
   * <LI> method is finalize ()
   * </UL>
   *
   * @return True if the method is reifiable
   * @param  met The method to be checked
   */

  static public boolean checkMethod(Method met) {
    int modifiers = met.getModifiers();

    // Final methods cannot be reified since we cannot redefine them
    // in a subclass
    if (Modifier.isFinal(modifiers))
      return false;


    // Static methods cannot be reified since they are not 'virtual'
    if (Modifier.isStatic(modifiers))
      return false;

    // We choose not to reify private methods, because it doen not match
    // the semantics of the 'private' keyword: a private method is never
    // called from the outside of an object
    if (MOPProperties.getReifyNonPublicMethods()) {
      if (Modifier.isPrivate(modifiers))
        return false;
      else
        return true;
    } else {
      if (!(Modifier.isPublic(modifiers)))
        return false;
    }



    // If method is finalize (), don't reify it
    if ((met.getName().equals("finalize")) && (met.getParameterTypes().length == 0))
      return false;

    return true;
  }


  /**
   * Returns a String representing the'source code style' declaration
   * of the Class object representing an array type given as argument.
   *
   * The problem is that the <code>toString()</code> method of class Class
   * does not
   * return what we are expecting, i-e the type definition that appears in
   * the source code (like <code>char[][]</code>).
   *
   * @param tab A class object representing an array type.
   * @return A String with the'source code representation' of that array type
   */

  static public String sourceLikeForm(Class cl) {
    if (!(cl.isArray())) {
      //to fix an issue with jdk1.3 and inner class
      // A$B should be A.B in source code
      //System.out.println("Remplacing in " + cl.getName());
 
	  
      return cl.getName().replace('$', '.');
    } else {
      int nb = 0;
      Class current = cl;
      String result = "";

      do {
        current = current.getComponentType();
        result = "[]" + result;
        nb++;
      } while ((current.getComponentType()) != null);

      result = current.getName() + result;
      return result;
    }
  }

  /*
   * Returns the name of the wrapper class for class <code>cl</code>.
   * If <code>cl</code> is not a primitive type, returns <code>null</code>
   */

  static public String nameOfWrapper(Class cl) {
    String str = cl.getName();

    if (cl.isPrimitive()) {
      if (str.equals("int"))
        return "java.lang.Integer";
      else if (str.equals("boolean"))
        return "java.lang.Boolean";
      else if (str.equals("byte"))
        return "java.lang.Byte";
      else if (str.equals("short"))
        return "java.lang.Short";
      else if (str.equals("long"))
        return "java.lang.Long";
      else if (str.equals("float"))
        return "java.lang.Float";
      else if (str.equals("double"))
        return "java.lang.Double";
      else if (str.equals("void"))
        return "void";
      else if (str.equals("char"))
        return "java.lang.Character";
      else {
        throw new InternalException("Unknown primitive type: " + cl.getName());
      }
    } else {
      return null;
    }
  }

  /*
   * Extract the package name from the fully qualified class name given as
   * an argument
   */

  public static String getPackageName(String fqnameofclass) {
    int indexoflastdot;

    indexoflastdot = fqnameofclass.lastIndexOf('.');

    if (indexoflastdot == -1) {
      return "";
    } else {
      return fqnameofclass.substring(0, indexoflastdot);
    }

  }


  /**
   * Extracts the simple name of the class from its fully qualified name
   */

  public static String getSimpleName(String fullyQualifiedNameOfClass) {
    int indexOfLastDot;
    String result;

    indexOfLastDot = fullyQualifiedNameOfClass.lastIndexOf('.');

    if (indexOfLastDot == -1) // There are no dots
    {
      return fullyQualifiedNameOfClass;
    } else {
      // If last character is a dot, returns an empty string
      if (indexOfLastDot == (fullyQualifiedNameOfClass.length() - 1))
        return "";
      else
        return fullyQualifiedNameOfClass.substring(indexOfLastDot + 1);
    }
  }


  /**
   * Returns the Class object that is a wrapper for the given <code>cl</code>
   * class.
   */
  public static Class getWrapperClass(Class cl) {
    if (!(cl.isPrimitive())) return null;
    String s = Utils.nameOfWrapper(cl);
    try {
      return MOP.forName(s);
    } catch (ClassNotFoundException e) {
      throw new InternalException("Cannot load wrapper class " + s);
    }
  }


  /**
   * Performs the opposite operation as getWrapperClass
   */
  public static Class getPrimitiveType(Class cl) {
    Field cst;
    if (Utils.isWrapperClass(cl)) {
      // These types are not classes , yet class static variables
      // We want to locale the TYPE field in the class
      try {
        cst = cl.getField("TYPE");
        return (Class)cst.get(null);
      } catch (NoSuchFieldException e) {
        throw new InternalException("Cannot locate constant TYPE in class " + cl.getName());
      } catch (SecurityException e) {
        throw new InternalException("Access to field TYPE in class " + cl.getName() + " denied");
      } catch (IllegalAccessException e) {
        throw new InternalException("Access to field TYPE in class " + cl.getName() + " denied");
      }
    } else {
      throw new InternalException("Not a wrapper class: " + cl.getName());
    }
  }


  /**
   * Tests if the class given as an argument is a wrapper class
   * How can we be sure that all subclasses of java.lang.Number are wrappers ??
   */

  public static boolean isWrapperClass(Class cl) {
    if (Utils.JAVA_LANG_NUMBER.isAssignableFrom(cl))
      return true;
    else if (Utils.JAVA_LANG_BOOLEAN.isAssignableFrom(cl))
      return true;
    else if (Utils.JAVA_LANG_CHARACTER.isAssignableFrom(cl))
      return true;
    else if (Utils.JAVA_LANG_VOID.isAssignableFrom(cl))
      return true;
    else
      return false;
  }


  public static String getRelativePath(String className) {
    String packageName;
    String fileSeparator;
    String result;
    int indexOfDot, indexOfLastDot;

    fileSeparator = System.getProperty("file.separator");
    packageName = Utils.getPackageName(className);

    indexOfDot = packageName.indexOf((int)'.', 0);
    result = "";
    indexOfLastDot = 0;

    while (indexOfDot != -1) {

      result = result + fileSeparator + packageName.substring(indexOfLastDot, indexOfDot);
      indexOfLastDot = indexOfDot + 1;
      indexOfDot = packageName.indexOf((int)'.', indexOfDot + 1);
      if (indexOfDot == -1)
        result = result + fileSeparator + packageName.substring(indexOfLastDot, packageName.length());
    }

    if (result.equals(""))
      result = fileSeparator + packageName;

    return result;
  }

/*
  public static String getStubName(String nameOfClass) {
    return Utils.getPackageName(nameOfClass) + "." + STUB_DEFAULT_PREFIX + Utils.getSimpleName(nameOfClass);
  }
*/

  public static boolean isNormalException(Class exc) {
    boolean result;

    if (Utils.JAVA_LANG_THROWABLE.isAssignableFrom(exc)) {
      // It is a subclass of Throwable
      if (Utils.JAVA_LANG_EXCEPTION.isAssignableFrom(exc)) {
        if (Utils.JAVA_LANG_RUNTIMEEXCEPTION.isAssignableFrom(exc))
          result = false;
        else
          result = true;
      } else
        result = false; // This must be an Error

    } else
      result = false;

    return result;
  }


  public static Class decipherPrimitiveType(String str) {
    if (str.equals("int"))
      return java.lang.Integer.TYPE;
    else if (str.equals("boolean"))
      return java.lang.Boolean.TYPE;
    else if (str.equals("byte"))
      return java.lang.Byte.TYPE;
    else if (str.equals("short"))
      return java.lang.Short.TYPE;
    else if (str.equals("long"))
      return java.lang.Long.TYPE;
    else if (str.equals("float"))
      return java.lang.Float.TYPE;
    else if (str.equals("double"))
      return java.lang.Double.TYPE;
    else if (str.equals("void"))
      return java.lang.Void.TYPE;
    else if (str.equals("char"))
      return java.lang.Character.TYPE;

    return null;
  }


  public static boolean isSuperTypeInArray(String className, Class[] types) {
    try {
      Class c = MOP.forName(className);
      return isSuperTypeInArray(c, types);
    } catch (ClassNotFoundException e) {
      throw new InternalException(e);
    }
  }


  public static boolean isSuperTypeInArray(Class c, Class[] types) {
    for (int i = 0; i < types.length; i++) {
      if (types[i].isAssignableFrom(c))
        return true;
    }
    return false;
  }
  
    public static Object makeDeepCopy(Object source) throws java.io.IOException {
    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
    oos.writeObject(source);
    oos.flush();
    oos.close();
    java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
    java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
    try {
      Object result = ois.readObject();
      ois.close();
      return result;
    } catch (ClassNotFoundException e) {
      throw new java.io.IOException("ClassNotFoundException e="+e);
    }
  }

  public static String convertClassNameToStubClassName (String classname)
  {
      /* OLD VERSION
	String packageName = Utils.getPackageName (classname);
	String stubClassSimpleName =  Utils.STUB_DEFAULT_PREFIX + Utils.getSimpleName(classname);
	return packageName + "." + stubClassSimpleName;
      */

	String packageName = "stubspa" + Utils.getPackageName (classname);
	String stubClassSimpleName =  Utils.STUB_DEFAULT_PREFIX + Utils.getSimpleName(classname);
	return packageName + "." + stubClassSimpleName;
  }

  public static boolean isStubClassName (String classname)
  {
      /* OLD VERSION
	 String simpleName;
	 
	 // Extracts the simple name from the fully-qualified class name
	 int index = classname.lastIndexOf(".");
	 if (index != -1)
	 {
	 simpleName = classname.substring (index+1);
	 }
	 else
	 {
	 simpleName = classname;
	 }
	 
	 if (simpleName.startsWith (Utils.STUB_DEFAULT_PREFIX))
	 {
	 return true;
	 }
	 else
	 {
	 return false;
	 }*/
      if (classname.startsWith ("stubspa"))
	  {
	     	 String simpleName;
	 
		 // Extracts the simple name from the fully-qualified class name
		 int index = classname.lastIndexOf(".");
		 if (index != -1)
		     {
			 simpleName = classname.substring (index+1);
		     }
		 else
		     {
			 simpleName = classname;
		     }
		 
		 if (simpleName.startsWith (Utils.STUB_DEFAULT_PREFIX))
		     {
			 return true;
		     }
		 else
		     {
			 return false;
		     }	      
	  }
      else
	  {
	      return false;
	  }
  }
    
  public static String convertStubClassNameToClassName (String stubclassname)
  {
      /* OLD VERSION
	String packageName = Utils.getPackageName (stubclassname);
	String stubClassSimpleName =  Utils.getSimpleName(stubclassname);	
	String classsimplename = stubClassSimpleName.substring (Utils.STUB_DEFAULT_PREFIX.length());
    return packageName + "." + classsimplename;
      */
     if (isStubClassName (stubclassname))
	 {
	     String temp = stubclassname.substring(7);
	     String packageName = Utils.getPackageName (temp);
	     String stubClassSimpleName =  Utils.getSimpleName(temp);	
	     String classsimplename = stubClassSimpleName.substring (Utils.STUB_DEFAULT_PREFIX.length());
	     String result =  packageName + "." + classsimplename;
//	     System.out.println ("CONVERT "+stubclassname+" -> "+result);
	     return result;
	 }
     else
	 {
	     return stubclassname;
	 }
  }
}






