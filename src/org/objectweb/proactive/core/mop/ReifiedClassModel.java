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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 *   A class that is reifiable. This class contains all the code we need
 *   for generating the stub class for this class
 */
class ReifiedClassModel extends Object {

  private static final String STATIC_STUB_CODE = 
    "\n\n"+
    "  public void setProxy(Proxy p) {\n"+
    "    this.myProxy = p;\n"+
    "  }\n"+
    "\n\n"+
    "  public Proxy getProxy() {\n"+
    "    return this.myProxy;\n"+
    "  }\n"+
    "\n\n"+
    "  protected void finalize() throws Throwable {\n"+
    "    super.finalize();\n"+
    "    this.myProxy = null;\n"+
    "  }\n\n";

  private static final String STUB_IMPORT = 
    "\n"+
    "import java.lang.reflect.Method;\n"+
    "import java.lang.reflect.InvocationTargetException;\n"+
    "import org.objectweb.proactive.core.mop.StubObjectUtils;\n"+
    "import org.objectweb.proactive.core.mop.Proxy;\n"+
    "import org.objectweb.proactive.core.mop.MethodCall;\n"+
    "import org.objectweb.proactive.core.mop.InternalException;\n"+ 
    "import org.objectweb.proactive.core.mop.InitializationOfStubClassFailedException;\n"+
    "import org.objectweb.proactive.core.mop.MOP;\n\n";


  protected static final String TAB = "  ";
  protected Class cl;
  protected String className;
  protected String stubClassSimpleName;
  protected String packageName;
  protected Method[] methods;
  protected boolean isInterface;
  protected int numberOfMethods;
  
  protected java.io.Writer sourceWriter;
  protected java.io.Writer logWriter;
  
  
  /**
   *
   */
  public ReifiedClassModel(String className, java.io.Writer sourceWriter, java.io.Writer logWriter) throws ClassNotFoundException, java.io.IOException  {
    // Locates the class object that reflects information about
    // the class whose fully-qualified name is <code>classname</code>.
    // As a consequence of using MOP.forName, the class is
    // automatically loaded and linked if it were not already loaded.
    this.cl = MOP.forName(className);

    // Indicates if the type we're reifying is an interface
    this.isInterface = this.cl.isInterface();

    // Creates two StringBuffers to hold both the source code
    // of the stub and the log file
    // Using StringBuffers is better than using Strings because,
    // as Strings are immutable objects, appending a String to
    // another one by using the '+' operator results in allocating
    // a new String object and possibly garbage-collecting both
    // operands

    this.sourceWriter = sourceWriter;
    this.logWriter = logWriter;

    // Keep this info at hand for performance purpose
    this.className = className;

    //
    //        this.exceptions = new Vector();

    // Fills in all the infos about this class
    this.setInfos();
  }


  /**
   * This method is called by the constructor
   */

  protected void setInfos() throws java.io.IOException {
    Method currentMethod;
    Class currentType;
    Class[] excs;

    // This hashtable is used for keeping track of the method signatures
    // we have already met while going up the inheritance branch
    java.util.Hashtable temp = new java.util.Hashtable();

    // Creates the header of the log file
    logOutput("Log file for the reification of class " + this.cl + "\n");
    logOutput("Created " + new java.util.Date() + "\n\n");

    // Recursively applies getDeclaredMethods () to the reified class
    // and each of its superclasses up to java.lang.Object
    // We have to take care to only take account of overriden methods once
    Class currentClass = this.cl;
    // This Vector keeps track of all the methods accessible from this class
    java.util.Vector tempVector = new java.util.Vector();
    Method[] declaredMethods;
    String key;
    Class[] params;
    Object exists;

    if (this.cl.isInterface()) {
      Method[] allPublicMethods = this.cl.getMethods();
      for (int i = 0; i < allPublicMethods.length; i++) {
        tempVector.addElement(allPublicMethods[i]);
      }
    } else {

      do // Loops from the current class up to java.lang.Object
      {
        declaredMethods = currentClass.getDeclaredMethods();
			
        // For each method declared in this class
        for (int i = 0; i < declaredMethods.length; i++) {
          currentMethod = declaredMethods[i];
          // Build a key with the simple name of the method
          // and the names of its parameters in the right order
          key = "";
          key = key + currentMethod.getName();
          params = currentMethod.getParameterTypes();
          for (int k = 0; k < params.length; k++) {
            key = key + params[k].getName();
          }
          // Tests if we already have met this method in a subclass
          exists = temp.get(key);
          if (exists == null) {
            // The only method we ABSOLUTELY want to be called directly
            // on the stub (and thus not reified) is
            // the protected void finalize () throws Throwable
            if ((key.equals("finalize")) && (params.length == 0)) {
              // Do nothing, simply avoid adding this method to the list
            } else {
              // If not, adds this method to the Vector that
              // holds all the methods for this class
              tempVector.addElement(currentMethod);
              temp.put(key, currentMethod);
            }
          } else {
            // We already know this method because it is overriden
            // in a subclass. Then do nothing
          }
        }
        currentClass = currentClass.getSuperclass();
      } while (currentClass != null);
    }	
	
	
    // Turns the vector into an array of type Method[]
    this.methods = new Method[tempVector.size()];
    tempVector.copyInto(this.methods);
	
    // Determines which methods are valid for reification
    // It is the responsibility of method checkMethod in class Utils
    // to decide if a method is valid for reification or not
	
    logOutput("***** Instance methods *****\n");

    int initialNumberOfMethods = this.methods.length;
    java.util.Vector v = new java.util.Vector();

    for (int i = 0; i < initialNumberOfMethods; i++) {
      if (Utils.checkMethod(this.methods[i])) {
        v.addElement(this.methods[i]);
        logOutput("YES: ");
      } else
        logOutput("NO : ");
      logOutput(this.methods[i] + "\n");
    }
    Method[] validMethods = new Method[v.size()];
    v.copyInto(validMethods);
	
    // Installs the list of valid methods as an instance variable of this object
    this.methods = validMethods;

    logOutput("***** Statistics ******\n");
    this.numberOfMethods = this.methods.length;
    logOutput(this.numberOfMethods + " instance methods reified out of " + initialNumberOfMethods + "\n");

    this.packageName = Utils.getPackageName(this.className);
    logOutput("Package name is " + this.packageName + "\n");

    this.stubClassSimpleName = "Stub_" + Utils.getSimpleName(this.className);
    
    // We build a vector with all exception types
    /*    for (int i=0;i<this.methods.length;i++)
          {
          currentMethod = this.methods [i];
          excs = currentMethod.getExceptionTypes ();
          for (int j=0;j<excs.length;j++)
          {
          currentType = excs [j];
          if (!(this.exceptions.contains (currentType)))
          {
          this.exceptions.addElement (currentType);
          }
          }
          }*/
  }


  protected void createPackageNameDeclaration() throws java.io.IOException {
    if ((packageName != null) && (!packageName.equals(""))) {
      outputLine(0, "package " + this.packageName + ";\n");
    }
  }


  protected void createClassDeclaration() throws java.io.IOException {
    if (this.isInterface) {
      outputLine(0, "public class " + this.stubClassSimpleName + " extends Object implements " + className + ",");
      outputLine(0, "                        org.objectweb.proactive.core.mop.StubObject, java.io.Serializable");
      outputLine(0, "{");
    } else {
      outputLine(0, "public class " + this.stubClassSimpleName + " extends " + className + " implements");
      outputLine(0, "                        org.objectweb.proactive.core.mop.StubObject, java.io.Serializable");
      outputLine(0, "{");
    }
  }


  protected void createInstanceVariables() throws java.io.IOException {
    outputLine(1, "protected Proxy myProxy;");
    // If we are inside the constructor of the stub for an interface,
    // we cannot do anything else than callingthe method on the proxy (which
    // is <code>null</code>, in order to get a NullPointerException.
    // More specifically, we can't execute the method 'on the stub' because
    // there is no inherited implementation.
    if (!(this.isInterface)) {
      outputLine(1, "protected boolean outsideConstructor;\n");
    }
  }


  protected void createConstructor() throws java.io.IOException {
    // Here we check if the noarg constructor of the superclass
    // throws a checked exception or not	
	
    //	boolean needToHandleException = false;

    // First, we need to determine the list of checked exceptions for this constructor
    Class[] exceptions;
    try {
      Constructor noArg = cl.getConstructor(new Class[0]);
      exceptions = noArg.getExceptionTypes();
      // If the constructor throws at least one checked exception
      if (exceptions.length == 0) {
        outputLine(1, "public " + stubClassSimpleName + "()");
      } else {
        output(1, "public " + stubClassSimpleName + "() throws ");
        for (int i = 0; i < exceptions.length; i++) {
          output(exceptions[i].getName());
          if (i < (exceptions.length - 1)) {
            output(", ");
          }
        }
        output("\n");
      }
    } catch (NoSuchMethodException e) {
      // If the class has no no-arg constructor, it may mean that it is an interface
      if (!(cl.isInterface())) {
        System.err.println("Strange, class " + this.className + " has no no-arg constructor.");
      }
      outputLine(1, "public " + stubClassSimpleName + " ()");
    } finally {
      outputLine(1, "{");
      outputLine(2, "super();");
      if (! isInterface) {
        outputLine(2, "this.outsideConstructor = true;");
      }
      outputLine(1, "}");
    }
  }


  protected void createStaticVariables() throws java.io.IOException {
    int i, j;
    int numberOfSuperclasses = 0;
    Method currentMethod;
    String nameOfType;
    Class[] parameters;
    Class currentClass;

    currentClass = cl;
    while (currentClass != null) {
      numberOfSuperclasses++;
      currentClass = currentClass.getSuperclass();
    }

    outputLine(1, "static Class[] reifiedClassSuperclasses = new Class[" + numberOfSuperclasses + "];");
    outputLine(1, "static Method[] reifiedMethods = new Method[" + numberOfMethods + "];");

    outputLine(1, "static {");

    outputLine(2, "Class currentClass = null;");

    // For each method, creates a Class[] array for holding the types of the parameters
    // In the future, should avoid allocation several arrays with the same length
    for (i = 0; i < this.numberOfMethods; i++) {
      currentMethod = this.methods[i];
      parameters = currentMethod.getParameterTypes();
      outputLine(2, "Class[] method" + i + "Parameters = new Class[" + parameters.length + "];");
    }

    outputLine(2, "try {");
    outputLine(3, "currentClass = Class.forName(\"" + this.className + "\");");
    outputLine(2, "} catch (ClassNotFoundException e) {");
    outputLine(3, "throw new InitializationOfStubClassFailedException (e);");
    outputLine(2, "}");

    // Creates constants for localizing the reified Class and its superclasses
    for (i = 0; i < numberOfSuperclasses; i++) {
      outputLine(2, "reifiedClassSuperclasses [" + i + "] = currentClass;");
      outputLine(2, "currentClass = currentClass.getSuperclass();");
    }

    outputLine(2, "try {");

    boolean shouldCheckForClassNotFoundException = false;
    for (i = 0; i < this.numberOfMethods; i++) {
      currentMethod = this.methods[i];
      parameters = currentMethod.getParameterTypes();

      for (j = 0; j < parameters.length; j++) {
        if (parameters[j].isPrimitive()) {
          nameOfType = Utils.getWrapperClass(parameters[j]).getName() + ".TYPE";
          outputLine(3, "method" + i + "Parameters [" + j + "] = " + nameOfType + ";");
        } else {
          nameOfType = parameters[j].getName();
          outputLine(3, "method" + i + "Parameters [" + j + "] = Class.forName(\"" + nameOfType + "\");");
          shouldCheckForClassNotFoundException = true;
        }
      }

      output(3, "reifiedMethods[" + i + "] = ");

      // Find the class that declares this method
      // What we are looking for is the index of the declaring class
      // in the inheritance hierarchy
      // Let's go through the inheritance hierarchy
      currentClass = cl;
      int index = 0;
      while (currentClass != null) {
        try {
          currentClass.getDeclaredMethod(currentMethod.getName(), parameters);
          // We have found the declaring class
          output("reifiedClassSuperclasses[" + index + "].getDeclaredMethod (\"" + currentMethod.getName() + "\", method" + i + "Parameters);\n");
          currentClass = null;
        } catch (NoSuchMethodException e) {
          currentClass = currentClass.getSuperclass();
          index++;
        }
      }
    }
    if (shouldCheckForClassNotFoundException) {
      outputLine(2, "} catch (ClassNotFoundException e) {");
      outputLine(3, "throw new InitializationOfStubClassFailedException(e);");
    }
    outputLine(2, "} catch (NoSuchMethodException e) {");
    outputLine(3, "throw new InitializationOfStubClassFailedException(e);");
    outputLine(2, "}");
    outputLine(1, "}");
  }


  protected void createInstanceMethods() throws java.io.IOException {
    for (int i = 0; i < this.numberOfMethods; i++) {
      this.createInstanceMethodDeclaration(this.methods[i]);
      this.createInstanceMethodBody(i);
    }
  }


  protected void createInstanceMethodDeclaration(Method method) throws java.io.IOException {
    int modifiers;
    Class[] arguments;
    Class[] exceptions;
    int i;

    modifiers = method.getModifiers();
    arguments = method.getParameterTypes();
    exceptions = method.getExceptionTypes();
	
    // Feed a line
    output("\n");

    // Write modifiers
    if (this.isInterface) {
      output(1, Utils.getRidOfNativeAndAbstract(Modifier.toString(modifiers)) + " ");
    } else {
      // If it is a class, we want to get rid of the 'native' and 'abstract' modifiers
      String temp = Utils.getRidOfNative(Modifier.toString(modifiers));
      output(1, Utils.getRidOfAbstract(temp) + " ");
    }
	
    // Write returntype
    output(Utils.sourceLikeForm(method.getReturnType()) + " ");

    // Write methodname
    output(method.getName() + "(");

    // Write all arguments
    for (i = 0; i < arguments.length; i++) {
      output(Utils.sourceLikeForm(arguments[i]));
      output(" argument" + i);
      if (i < (arguments.length - 1))
        output(", ");
    }
    output(") ");

    // Declare thrown exceptions
    if (exceptions.length > 0)
      output("throws ");
    for (i = 0; i < exceptions.length; i++) {
      output(exceptions[i].getName());
      if (i < (exceptions.length - 1))
        output(", ");
    }
    output("\n");
  }


  protected void createInstanceMethodBody(int methodIndex) throws java.io.IOException {
    int i;
    Method method = this.methods[methodIndex];
    int n = method.getParameterTypes().length;
    Class[] excps = method.getExceptionTypes();
    Class[] argsTypes;
    boolean returnsVoid = false;
    boolean isAbstractMethod = Modifier.isAbstract(method.getModifiers());

    argsTypes = this.methods[methodIndex].getParameterTypes();
    outputLine(1, "{");

    // If this is a stub for an interface, or if the method is abstract
    // , there is no need for doing this stuff
    if ((!(this.isInterface)) && (!(isAbstractMethod))) {
      // Handles the switching off or reification when called from inside a constructor
      outputLine(2, "if (! outsideConstructor) {");

      if (method.getReturnType().equals(java.lang.Void.TYPE))
        returnsVoid = true;

      if (returnsVoid) {
        output(3, "super." + method.getName() + "(");
      } else {
        output(3, "return super." + method.getName() + "(");
      }

      for (i = 0; i < n; i++) {
        output("argument" + Integer.toString(i));
        if (i < (n - 1)) output(", ");
      }
      output(");\n");

      if (returnsVoid)
        outputLine(3, "return;");
      outputLine(2, "}");
    }
	
    // Back to normal case (reification activated)
    output(2, "Object[] parameters = {");
    for (i = 0; i < n; i++) {
      if (argsTypes[i].isPrimitive()) {
        output("new ");
        output(Utils.nameOfWrapper(argsTypes[i]) + "(argument" + i + ")");
      } else {
        output("argument" + i);
      }
      if (i < (n - 1))
        output(", ");
    }
    output("};\n");
    outputLine(2, "Object result = null;");
    outputLine(2, "String methodSignature = \""+method+"\";");

    outputLine(2, "try {");
    outputLine(3, "result = myProxy.reify(MethodCall.getMethodCall(reifiedMethods[" + methodIndex + "], parameters));");

    // Catch IllegalAccessException and throws a InternalException message
    outputLine(2, "} catch (IllegalAccessException e) {");
    outputLine(3, "StubObjectUtils.processIllegalAccessException(e, methodSignature);");

    // Catch IllegalArgumentException and throws a InternalException message
    outputLine(2, "} catch (IllegalArgumentException e) {");
    outputLine(3, "StubObjectUtils.processIllegalArgumentException(e, methodSignature);");

    // Catch InvocationTargetException
    outputLine(2, "} catch (InvocationTargetException e) {");
    if (Utils.isSuperTypeInArray("java.lang.Throwable", excps)) {
      outputLine(3, "throw e.getTargetException();");
    } else {
      outputLine(3, "Throwable t = e.getTargetException();");
      outputLine(3, "StubObjectUtils.processUndeclaredException(t);");
      if (Utils.isSuperTypeInArray("java.lang.Exception", excps)) {
        outputLine(3, "if (t instanceof Exception)");
        outputLine(4, "throw (Exception) t;");
        outputLine(3, "else StubObjectUtils.processThrowable(t, methodSignature);");
      } else {
        outputLine(3, "try {");
        outputLine(4, "throw t;");
        // Catch some of the declared exceptions
        // We ONLY catch declared exceptions whose declaration in the method
        // declaration is compulsory (that is, Exceptions that are not subclasses
        // of RuntimeException).
        // If we had decided to catch all Exceptions, the compiler would have
        // failed with a 'catch not reached' message. This happens, for example,
        // for method finalize () in class Object that declares it throws Throwable
        for (i = 0; i < excps.length; i++) {
          if (Utils.isNormalException(excps[i])) {
            outputLine(3, "} catch (" + excps[i].getName() + " ee) {");
            outputLine(4, "throw ee;");
          }
        }
        // Catch Exception
        outputLine(3, "} catch (Exception ee) {");
        outputLine(4, "StubObjectUtils.processException(ee, methodSignature);");
        // Catch Throwable
        outputLine(3, "} catch (Throwable ee) {");
        outputLine(4, "StubObjectUtils.processThrowable(ee, methodSignature);");
        outputLine(3, "}");
      }
    }
    outputLine(2, "}");

    if (method.getReturnType().equals(java.lang.Void.TYPE)) {
      // nothing
    } else if (method.getReturnType().isPrimitive()) {
      outputLine(2, "return ((" + Utils.nameOfWrapper(method.getReturnType()) + ") result)." + method.getReturnType().getName() + "Value();");
    } else if (method.getReturnType().isArray()) {
      outputLine(2, "return (" + Utils.sourceLikeForm(method.getReturnType()) + ") result;");
    } else {
      outputLine(2, "return (" + Utils.sourceLikeForm(method.getReturnType()) + ") result;");
    }
    outputLine(1, "}");
  }


  public void create() throws java.io.IOException {
    createPackageNameDeclaration();
    output(STUB_IMPORT);
    createClassDeclaration();
    createInstanceVariables();
    createStaticVariables();
    createConstructor();
    createInstanceMethods();
    output(STATIC_STUB_CODE);
    outputLine(0, "}");
  }
  
  
  
  private void outputLine(int indent, String text) throws java.io.IOException {
    output(indent, text);
    sourceWriter.write('\n');
  }
  
  private void output(int indent, String text) throws java.io.IOException {
    for (int i=0; i<indent; i++)
      sourceWriter.write(TAB);
    output(text);
  }
  
  private void output(String text) throws java.io.IOException {
    sourceWriter.write(text);
  }
  
  private void logOutput(String text) throws java.io.IOException {
    logWriter.write(text);
  }

}

