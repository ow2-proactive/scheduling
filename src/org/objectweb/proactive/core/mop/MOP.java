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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * A place where static methods go
 */
public abstract class MOP {

  /**
   * The name of the interface that caracterizes all stub classes
   */

  protected static String STUB_OBJECT_INTERFACE_NAME = "org.objectweb.proactive.core.mop.StubObject";
  protected static Class STUB_OBJECT_INTERFACE;
  /**
   * The root interface of all metabehaviors
   */

  protected static String ROOT_INTERFACE_NAME = "org.objectweb.proactive.core.mop.Reflect";
  protected static Class ROOT_INTERFACE;
  /**
   * If file is locked; time between retries
   */

  protected static long TIME_BETWEEN_RETRIES = 1000;
  /**
   * Class array representing no parameters
   */

  protected static final Class[] EMPTY_CLASS_ARRAY;
  /**
   * Empty object array
   */

  protected static final Object[] EMPTY_OBJECT_ARRAY;
  /**
   * Class array representing (Constructor Call, Object[])
   */

  protected static Class[] PROXY_CONSTRUCTOR_PARAMETERS_TYPES_ARRAY;
  /**
   * A Hashtable to cache (reified class, stub class constructor) couples.
   */

  protected static java.util.Hashtable stubTable;
  /**
   * A Hashtable to cache (proxy class, proxy class constructor) couples
   */

  protected static java.util.Hashtable proxyTable;
  /**
   * A Hashtable to cache (Class name, proxy class name) couples
   * this is meant for class-based reification
   */

  protected static java.util.Hashtable secondProxyTable;

  /**
   * A hashtable for caching (target object/its local stub reference)
   */

  //    protected static Hashtable proxyOnThisTable;

  /**
   * We need to know all CLASSPATh entries in order to locate the bytecode
   * file for a given class because at some point we want to know the date
   * of creation of this file
   */

  protected static java.io.File[] classPathEntries;
  /**
   * For performance and convenience purposes, we keep this value at hand
   */

  static String fileSeparator;
  /**
   * The compiler
   */

  private static Compiler comp;

  /**
   *	As this class is center to the API, its static initializer is
   *	a good place to initialize general stuff.
   */

  static {
    // Simply initializes these
    stubTable = new java.util.Hashtable();
    proxyTable = new java.util.Hashtable();
    secondProxyTable = new java.util.Hashtable();
    //        proxyOnThisTable = new Hashtable();

    // Initializes various constants
    EMPTY_CLASS_ARRAY = new Class[0];
    EMPTY_OBJECT_ARRAY = new Object[0];

    PROXY_CONSTRUCTOR_PARAMETERS_TYPES_ARRAY = new Class[2];

    try {
      PROXY_CONSTRUCTOR_PARAMETERS_TYPES_ARRAY[0] = forName("org.objectweb.proactive.core.mop.ConstructorCall");
    } catch (ClassNotFoundException e) {
      throw new CannotFindClassException("org.objectweb.proactive.core.mop.ConstructorCall");
    }

    PROXY_CONSTRUCTOR_PARAMETERS_TYPES_ARRAY[1] = EMPTY_OBJECT_ARRAY.getClass();

    try {
      STUB_OBJECT_INTERFACE = forName(STUB_OBJECT_INTERFACE_NAME);
    } catch (ClassNotFoundException e) {
      throw new CannotFindClassException(STUB_OBJECT_INTERFACE_NAME);
    }

    try {
      ROOT_INTERFACE = forName(ROOT_INTERFACE_NAME);
    } catch (ClassNotFoundException e) {
      throw new CannotFindClassException(ROOT_INTERFACE_NAME);
    }

    // Initializes classPathEntries
    String classPath;
    String pathSeparator;
    String pathElement;
    java.io.File pathFile;
    java.util.Vector classPathElements;
    java.util.Vector classPathFiles;
    java.util.Enumeration en;
    int index0, index1;
    boolean endReached;

    classPath = System.getProperty("java.class.path");
    pathSeparator = System.getProperty("path.separator");
    fileSeparator = System.getProperty("file.separator");
    classPathElements = new java.util.Vector();
    classPathFiles = new java.util.Vector();
    index0 = 0;
    index1 = classPath.indexOf(pathSeparator);
    endReached = false;
    while (!(endReached)) {
      if (index1 != -1) {
        pathElement = classPath.substring(index0, index1);
      } else {
        pathElement = classPath.substring(index0);
        endReached = true;
      }
      classPathElements.addElement(pathElement);
      index0 = index1 + pathSeparator.length();
      index1 = classPath.indexOf(pathSeparator, index1 + pathSeparator.length());
    }

    en = classPathElements.elements();
    while (en.hasMoreElements()) {
      pathElement = (String)en.nextElement();
      pathFile = new java.io.File(pathElement);
      if (pathFile != null) {
        if (pathFile.exists()) {
          classPathFiles.addElement(pathFile);
        }
      } else {
      }
    }

    classPathEntries = new java.io.File[classPathFiles.size()];

    en = classPathFiles.elements();
    java.io.File currentFile;
    java.io.File targetFile;
    int index = 0;
    while (en.hasMoreElements()) {
      currentFile = (java.io.File)en.nextElement();
      classPathEntries[index++] = currentFile;
    }
  }


  /**
   * Loads a class
   * @param s the name of the class to fetch
   * @return the Class object representing class s
   */
  public static Class forName(String s) throws java.lang.ClassNotFoundException {
    return Class.forName(s);
  }


  /**
   *	This method is here to perform a lazy instanciation of the compiler
   */
  static Compiler getCompiler() {
    if (comp == null)
      comp = new JDKCompiler();
    return comp;
  }


  /**
   * Generates a stub class
   * @param nameOfClass The name of the class needing stubs
   */
  public static void generateStubClass(String nameOfClass) throws ClassNotFoundException, ClassNotReifiableException {
    Class targetClass;
    targetClass = forName(nameOfClass);
    checkClassIsReifiable(targetClass);
    findStubConstructor(targetClass);
  }


  /**
   * Creates an instance of an object
   * @param nameOfClass The class to instanciate
   * @param constructorParameters Array of the constructor's parameters [wrapper]
   * @param nameOfProxy The name of its proxy class
   * @param proxyParameters The array holding the proxy parameter
   */
  public static Object newInstance(String nameOfClass, Object[] constructorParameters, String nameOfProxy, Object[] proxyParameters) throws ClassNotFoundException, ClassNotReifiableException, InvalidProxyClassException, ConstructionOfProxyObjectFailedException, ConstructionOfReifiedObjectFailedException {
    try {
      return newInstance(nameOfClass, nameOfClass, constructorParameters, nameOfProxy, proxyParameters);
    } catch (ReifiedCastException e) {
      throw new InternalException(e);
    }
  }


  /**
   * Creates an instance of an object
   * @param nameOfStubClass The name of the Stub class corresponding to the object
   * @param nameOfClass The class to instanciate
   * @param constructorParameters Array of the constructor's parameters [wrapper]
   * @param nameOfProxy The name of its proxy class
   * @param proxyParameters The array holding the proxy parameter
   */
  public static Object newInstance(String nameOfStubClass, String nameOfClass, Object[] constructorParameters, String nameOfProxy, Object[] proxyParameters) throws ClassNotFoundException, ClassNotReifiableException, ReifiedCastException, InvalidProxyClassException, ConstructionOfProxyObjectFailedException, ConstructionOfReifiedObjectFailedException {
    // For convenience, allows 'null' to be equivalent to an empty array
    if (constructorParameters == null) constructorParameters = EMPTY_OBJECT_ARRAY;
    if (proxyParameters == null) proxyParameters = EMPTY_OBJECT_ARRAY;

    // Throws a ClassNotFoundException
    Class targetClass = forName(nameOfClass);
    // Instanciates the stub object
    StubObject stub = createStubObject(nameOfStubClass, targetClass);
    // build the constructor call for the target object to create
    ConstructorCall reifiedCall = buildTargetObjectConstructorCall(targetClass, constructorParameters); 
    // Instanciates the proxy object
    Proxy proxy = createProxyObject(nameOfProxy, proxyParameters, reifiedCall);
    // Connects the proxy to the stub
    stub.setProxy(proxy);
    return stub;
  }


  /**
   * Creates an instance of an object
   * @param nameOfClass The class to instanciate
   * @param constructorParameters Array of the constructor's parameters [wrapper]
   * @param proxyParameters The array holding the proxy parameter
   */
  public static Object newInstance(String nameOfClass, Object[] constructorParameters, Object[] proxyParameters) throws ClassNotFoundException, ClassNotReifiableException, CannotGuessProxyNameException, InvalidProxyClassException, ConstructionOfProxyObjectFailedException, ConstructionOfReifiedObjectFailedException {
    String nameOfProxy = guessProxyName(forName(nameOfClass));
    return newInstance(nameOfClass, constructorParameters, nameOfProxy, proxyParameters);
  }


  /**
   * Reifies an object
   * @param proxyParameters Array holding the proxy parameters
   * @param target the object to reify
   */
  public static Object turnReified(Object[] proxyParameters, Object target) throws ClassNotReifiableException, CannotGuessProxyNameException, InvalidProxyClassException, ConstructionOfProxyObjectFailedException {
    try {
      return turnReified(guessProxyName(target.getClass()), proxyParameters, target);
    } catch (ClassNotFoundException e) {
      throw new CannotGuessProxyNameException();
    }
  }


  /**
   * Reifies an object
   * @param nameOfProxyClass the name of the object's proxy
   * @param proxyParameters Array holding the proxy parameters
   * @param target the object to reify
   */
  public static Object turnReified(String nameOfProxyClass, Object[] proxyParameters, Object target) throws ClassNotFoundException, ClassNotReifiableException, InvalidProxyClassException, ConstructionOfProxyObjectFailedException {
    try {
      return turnReified(target.getClass().getName(), nameOfProxyClass, proxyParameters, target);
    } catch (ReifiedCastException e) {
      throw new InternalException(e);
    }
  }


  /**
   * Reifies an object
   * @param proxyParameters Array holding the proxy parameters
   * @param nameOfStubClass The name of the object's stub class
   * @param target the object to reify
   */
  public static Object turnReified(Object[] proxyParameters, String nameOfStubClass, Object target) throws ClassNotFoundException, ReifiedCastException, ClassNotReifiableException, CannotGuessProxyNameException, InvalidProxyClassException, ConstructionOfProxyObjectFailedException {
    String nameOfProxy = guessProxyName(target.getClass());
    return turnReified(nameOfStubClass, nameOfProxy, proxyParameters, target);
  }


  /**
   * Reifies an object
   * @param nameOfProxyClass the name of the object's proxy
   * @param nameOfStubClass The name of the object's stub class
   * @param proxyParameters Array holding the proxy parameters
   * @param target the object to reify
   */
  public static Object turnReified(String nameOfStubClass, String nameOfProxyClass, Object[] proxyParameters, Object target) 
              throws ClassNotFoundException, ReifiedCastException, ClassNotReifiableException, InvalidProxyClassException, 
                     ConstructionOfProxyObjectFailedException {
    // For convenience, allows 'null' to be equivalent to an empty array
    if (proxyParameters == null) proxyParameters = EMPTY_OBJECT_ARRAY;
    // Throws a ClassNotFoundException
    Class targetClass = target.getClass();
    // Instanciates the stub object
    StubObject stub = createStubObject(nameOfStubClass, targetClass);
    // First, build the FakeConstructorCall object to pass to the constructor
    // of the proxy Object
    // FakeConstructorCall fakes a ConstructorCall object by returning
    // an already-existing object as the result of its execution
    ConstructorCall reifiedCall = new FakeConstructorCall(target);
    // Instanciates the proxy object
    Proxy proxy = createProxyObject(nameOfProxyClass, proxyParameters, reifiedCall);
    // Connects the proxy to the stub
    stub.setProxy(proxy);
    return stub;
  }


  /**
   * Checks if a stub class can be created for the class <code>cl</code>.
   *
   * A class cannot be reified if at least one of the following conditions are
   *  met : <UL>
   * <LI>This <code>Class</code> objects represents a primitive type
   * (except void)
   * <LI>The class is <code>final</code>
   * <LI>There is an ambiguity in constructors signatures
   * <LI>There is no noargs constructor
   * </UL>
   *
   * @author Julien Vayssière, INRIA
   * @param cl Class to be checked
   * @return <code>true</code> is the class exists and can be reified,
   *  <code>false</code> otherwise.
   */
  static void checkClassIsReifiable(String className) throws ClassNotReifiableException, ClassNotFoundException {
    checkClassIsReifiable(forName(className));
  }


  public static void checkClassIsReifiable(Class cl) throws ClassNotReifiableException {
    int mods = cl.getModifiers();
    if (cl.isInterface()) {
      // Interfaces are always reifiable, although some of the methods
      // they contain may not be reifiable
      return;
    } else {
      // normal case, this is a class
      if (cl.isPrimitive())
        throw new ClassNotReifiableException("Cannot reify primitive types: " + cl.getName());
      else if (Modifier.isFinal(mods))
        throw new ClassNotReifiableException("Cannot reify final classes: " + cl.getName());
      else if (!(checkNoArgsConstructor(cl)))
        throw new ClassNotReifiableException("Class " + cl.getName() + " needs to have an empty noarg constructor.");
      else return;
    }
  }


  /**
   * Checks if class <code>c</code> has a noargs constructor
   */

  protected static boolean checkNoArgsConstructor(Class cl) {
    try {
      cl.getConstructor(EMPTY_CLASS_ARRAY);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }


  /**
   * Checks if an object is a stub object
   *
   * Being a stub object is equivalent to implementing the StubObject
   * interface
   *
   * @param o the object to check
   * @return <code>true</code> if it is a stub object, <code>false</code>
   * otherwise */

  public static boolean isReifiedObject(Object o) {
    return (STUB_OBJECT_INTERFACE.isAssignableFrom(o.getClass()));
  }


  /**
   * Creates a stub class for the specified class
   * @param nameOfClass The name of the class
   * @return A class object representing the class, or NULL if failed
   */
    
    
    protected static Class createStubClass(String nameOfClass) {
	if (MOPProperties.getGenerateBytecode ())
	    {
		return createStubClassBytecodeVersion (nameOfClass);
	    }
	else
	    {
		return createStubClassSourceVersion (nameOfClass);	
	    }
    }

    protected static MOPClassLoader singleton;

  protected static Class createStubClassBytecodeVersion(String nameOfClass) {
      // We simply install a specific classloader that will create the stub
      // class if it does not already exist
      if (singleton==null)
	  {
	      singleton = MOPClassLoader.createMOPClassLoader ();
	  }

      try
	  {
	      Class cl = Class.forName(Utils.convertClassNameToStubClassName(nameOfClass), true, singleton);
	      return cl;
	  }
      catch (ClassNotFoundException e)
	  {
	      return null;
	  }
  }
            
    protected static Class createStubClassSourceVersion(String nameOfClass) {
	// Find the directory where to write the stub source file
	// If it does not exist, let's create it
    String stubName = Utils.convertClassNameToStubClassName(nameOfClass);
    String directoryName = MOPProperties.getStubsOutputDirectory() + Utils.getRelativePath(stubName);
    String sourceFileName = Utils.getSimpleName(stubName) + ".java";
    String logFileName = Utils.getSimpleName(stubName) + ".log";
    String lockFileName = Utils.getSimpleName(stubName) + ".lock";
    java.io.File sourceDirectory = new java.io.File(directoryName);
    boolean lockDeleteAllowed = false;
    java.io.File lockFile = null;
    
    if (!(sourceDirectory.exists()))
      sourceDirectory.mkdirs();

    // Determines if we have sufficient privileges to delete the lock file
    if (MOPProperties.getUseLockFiles()) {
      try {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
          sm.checkDelete(lockFileName);
        lockDeleteAllowed = true;
      } catch (java.lang.SecurityException e) {
        lockDeleteAllowed = false;
      }

      // Creates a File object to represent the lock file
      // this does not imply that the file is automatically created
      // if it does not exist
      lockFile = new java.io.File(sourceDirectory, lockFileName);

      // does lock already exist ?
      if (lockFile.exists()) {
        System.err.println("Lock file exits. Waiting for compile to finish");
        System.err.println("If it lasts too long, try removing file " + lockFile + " by hand");
        while (lockFile.exists()) {
          System.err.print(".");
          try {
            Thread.sleep(TIME_BETWEEN_RETRIES);
          } catch (InterruptedException e) {
          }
        }
        System.err.println("");

        // Lock no more exists
        // Checks whether the class has been succesfully compiled
        // (by someone else), or if it does not yet exits (if lock has been
        // manually removes)
        Class test;
        try {
          test = forName(stubName);
          // If the class is found, it means someone else has done the job
          // for us, then there's no need to continue
          return test;
        } catch (ClassNotFoundException e) {
          // Try it again
          return createStubClass(nameOfClass);
        }
      } else {
        // Creates the lock file before starting compiling
        // Don't create it if we cannot remove it latter
        if (lockDeleteAllowed) {
          try {
            java.io.FileOutputStream lockfos = new java.io.FileOutputStream(lockFile);
            // If we do not close the stream, we may never be able to delete the file
            lockfos.close();
          } catch (java.io.IOException e) {
            System.err.println("Cannot create lock file " + lockFile + ". Proceeding anyway.");
          }
        }
      }
    }// End of condition 'if we use lock files'
 

    java.io.File sourceFile = null;
    java.io.File logFile = null;
    java.io.Writer sourceWriter = null;
    java.io.Writer logWriter = null;
    // Creates the source code and the related log file
    //long t1 = System.currentTimeMillis();
    try {
      sourceFile = new java.io.File(directoryName, sourceFileName);
      logFile = new java.io.File(directoryName, logFileName);
      sourceWriter = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(sourceFile)));
      logWriter = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(logFile)));
      //sourceWriter = new java.io.OutputStreamWriter(new java.io.FileOutputStream(sourceFile));
      //logWriter = new java.io.OutputStreamWriter(new java.io.FileOutputStream(logFile));
      
      ReifiedClassModel target;
      try {
        target = new ReifiedClassModel(nameOfClass, sourceWriter, logWriter);
      } catch (ClassNotFoundException e) {
        throw new GenerationOfStubClassFailedException("Cannot find class " + nameOfClass);
      }
      target.create();

      sourceWriter.flush();
      logWriter.flush();
    } catch (java.io.IOException e) {
      throw new GenerationOfStubClassFailedException("File error with writing source file or log file:" + e);
    } finally {
      try {
	if (sourceWriter != null)
	    sourceWriter.close();
 	if (logWriter != null)
	    logWriter.close();
      } catch (java.io.IOException e) {}
    }
    //long t2 = System.currentTimeMillis();
    //System.out.println("Generated in "+(t2-t1));
    // Calling the compiler
    boolean compilationresult = false;

    System.err.print("Now compiling " + sourceFile.getName() + "... ");
    
    try {
      getCompiler().compile(sourceFile);
    } catch (java.io.IOException e) {
      throw new GenerationOfStubClassFailedException("Compilation of class " + stubName + " failed.", e);
    }
    
    if (MOPProperties.getUseLockFiles()) {
      // Deletes the lock
      if (lockDeleteAllowed) {
        if (! lockFile.delete()) {
          //System.err.println("\nCannot delete lock file " + lockFile + "\n");
        }
      }
    }

    // Whatever is the result of the compilation, we have to delete the
    // source file of the stub
    if (!(MOPProperties.getKeepSource())) {
      if (! sourceFile.delete()) {
        //System.err.println("Cannot delete source file " + sourceFile);
      }
    }

    System.out.println("OK");
    int tryCount = 0;
    while (true) {
      try {
        return forName(stubName);
      } catch (ClassNotFoundException e) {
        throw new GenerationOfStubClassFailedException("Cannot find stub class " + stubName + ", even though its compilation succeeded. Check CLASSPATH settings.");
      } catch (java.lang.ClassFormatError e) {
        // infamous error that may append randomly : we try again to load
        tryCount++;
        //System.out.println(" eh eh !!!!!!!!!!!!!!!!");
        if (tryCount >= 2)
          throw new GenerationOfStubClassFailedException("Error in the format of the generated class for " + stubName + ".");
      }
    }
  }


  /**
   * Finds the Stub Constructor for a specified class
   * @param nameOfClass the name of the class
   * @return The Constructor object.
   * @throws ClassNotFoundException if the class cannot be located
   */
  static Constructor findStubConstructor(String nameOfClass) throws ClassNotFoundException {
    return findStubConstructor(forName(nameOfClass));
  }


  /**
   * Finds the Stub Constructor for a specified class
   * @param targetClass the representation of the class
   * @return The Constructor object.
   */
  static Constructor findStubConstructor(Class targetClass) {
    Constructor stubConstructor;
    String nameOfClass = targetClass.getName();

    // Is it cached in Hashtable ?
    stubConstructor = (Constructor)stubTable.get(nameOfClass);

    // On cache miss, finds the constructor
    if (stubConstructor == null) {
      Class stubClass;
      try {
        // Checks if the stub is newer than the class
        // If so, regenarate and recompile it
        if (shouldRegenerateStub(nameOfClass)) {
          System.out.println("MOP: Stub class does not exist or is older than class " + nameOfClass + ". Regenerating stub class");
          stubClass = createStubClass(nameOfClass);
        } else {
          // completes normally if the stub class exists and has not
          // already been loaded
          stubClass = forName(Utils.convertClassNameToStubClassName(nameOfClass));
        }
      }
        // No stub class can be found, let's create it from scratch
      catch (ClassNotFoundException e) {
        stubClass = createStubClass(nameOfClass);
      }

      // Verifies that the stub has a noargs constructor and caches it
      try {
        stubConstructor = stubClass.getConstructor(EMPTY_CLASS_ARRAY);
        stubTable.put(nameOfClass, stubConstructor);
      } catch (NoSuchMethodException e) {
        throw new GenerationOfStubClassFailedException("Stub for class " + nameOfClass + "has no noargs constructor. This is a bug in ProActive.");
      }
    }
    return stubConstructor;
  }


  /**
   * Finds the Constructor of the proxy for a specified class
   * @param proxyClass The represenation of the proxy
   * @return the Constructor
   * @throws InvalidProxyClassException If the class is not a valid Proxy
   */
  static Constructor findProxyConstructor(Class proxyClass) throws InvalidProxyClassException {
    Constructor proxyConstructor;

    // Localizes the proxy class constructor
    proxyConstructor = (Constructor)proxyTable.get(proxyClass.getName());
    //System.out.println("MOP: The class of the proxy is " + proxyClass.getName());
 
    // Cache miss
    if (proxyConstructor == null) {
      try {
        proxyConstructor = proxyClass.getConstructor(PROXY_CONSTRUCTOR_PARAMETERS_TYPES_ARRAY);
        proxyTable.put(proxyClass.getName(), proxyConstructor);
      } catch (NoSuchMethodException e) {
        throw new InvalidProxyClassException("No constructor matching (ConstructorCall, Object[]) found in proxy class " + proxyClass.getName());
      }
    }
    return proxyConstructor;
  }


  protected static StubObject instanciateStubObject(Constructor stubConstructor) throws ConstructionOfStubObjectFailedException {
    try {
	Object o =  stubConstructor.newInstance(EMPTY_OBJECT_ARRAY);
      return (StubObject) o;
    } catch (InstantiationException e) {
      throw new ConstructionOfStubObjectFailedException("Constructor " + stubConstructor + " belongs to an abstract class.");
    } catch (IllegalArgumentException e) {
      throw new ConstructionOfStubObjectFailedException("Wrapping problem with constructor " + stubConstructor);
    } catch (IllegalAccessException e) {
      throw new ConstructionOfStubObjectFailedException("Access denied to constructor " + stubConstructor);
    } catch (InvocationTargetException e) {
      throw new ConstructionOfStubObjectFailedException("The constructor of the stub has thrown an exception: ", e.getTargetException());
    }
  }


  private static StubObject createStubObject(String nameOfStubClass, Class targetClass) throws ClassNotFoundException, ReifiedCastException, ClassNotReifiableException {
    // Throws a ClassNotFoundException
    Class stubClass = forName(nameOfStubClass);
 
    // Check that the type of the class is compatible with the type of the stub
    if (!(stubClass.isAssignableFrom(targetClass))) {
      throw new ReifiedCastException("Cannot convert " + targetClass.getName() + "into " + stubClass.getName());
    }
    // Throws a ClassNotReifiableException exception if not reifiable
    checkClassIsReifiable(stubClass);
    // Finds the constructor of the stub class
    // If the stub class has not yet been created,
    // it is created within this call
    Constructor stubConstructor = findStubConstructor(stubClass);
    // Instanciates the stub object
    return instanciateStubObject(stubConstructor);
  }

 
  // Instanciates the proxy object
  private static Proxy createProxyObject(String nameOfProxy, Object[] proxyParameters, ConstructorCall reifiedCall) 
      throws ConstructionOfProxyObjectFailedException, ClassNotFoundException, InvalidProxyClassException {
    // Throws a ClassNotFoundException
    Class proxyClass = forName(nameOfProxy);
    // Finds constructor of the proxy class
    Constructor proxyConstructor = findProxyConstructor(proxyClass);
    // Now calls the constructor of the proxy
    Object[] params = new Object[] { reifiedCall, proxyParameters };
    try {
     return (Proxy)proxyConstructor.newInstance(params);
    } catch (InstantiationException e) {
     throw new ConstructionOfProxyObjectFailedException("Constructor " + proxyConstructor + " belongs to an abstract class");
    } catch (IllegalArgumentException e) {
     throw new ConstructionOfProxyObjectFailedException("Wrapping problem with constructor " + proxyConstructor);
    } catch (IllegalAccessException e) {
     throw new ConstructionOfProxyObjectFailedException("Access denied to constructor " + proxyConstructor);
    } catch (InvocationTargetException e) {
       throw new ConstructionOfProxyObjectFailedException("The constructor of the proxy object has thrown an exception: ", e.getTargetException());
    }
  }


  private static ConstructorCall buildTargetObjectConstructorCall(Class targetClass, Object[] constructorParameters) throws ConstructionOfReifiedObjectFailedException {
    // First, build the ConstructorCall object to pass to the constructor
    // of the proxy Object. It represents the construction of the reified
    // object.
    Constructor targetConstructor;
    // Locates the right constructor (should use a cache here ?)
    Class[] targetConstructorArgs = new Class[constructorParameters.length];
    for (int i = 0; i < constructorParameters.length; i++) {
      //	System.out.println("MOP: constructorParameters[i] = " + constructorParameters[i]);
      targetConstructorArgs[i] = constructorParameters[i].getClass();
      //	System.out.println("MOP: targetConstructorArgs[i] = " + targetConstructorArgs[i]);
    }

    //System.out.println("MOP: targetClass is " + targetClass);

    //	System.out.println("MOP: targetConstructorArgs = " + targetConstructorArgs);
    //	System.out.println("MOP: targetConstructorArgs.length = " + targetConstructorArgs.length);

    try {
      //MODIFIED 4/5/00
      if (targetClass.isInterface()) {
        //there is no point in looking for the constructor of an interface
        //	System.out.println("MOP: WARNING Interface detected");
        targetConstructor = null;
      } else {
        targetConstructor = targetClass.getDeclaredConstructor(targetConstructorArgs);
      }
    } catch (NoSuchMethodException e) {
      // This may have failed because getConstructor does not allow subtypes
      targetConstructor = findReifiedConstructor(targetClass, targetConstructorArgs);

      if (targetConstructor == null)
      // This may have failed because some wrappers should be interpreted
      // as primitive types. Let's investigate it
      {
        targetConstructor = investigateAmbiguity(targetClass, targetConstructorArgs);
        if (targetConstructor == null)
          throw new ConstructionOfReifiedObjectFailedException("Cannot locate this constructor in class " + targetClass + " : " + targetConstructorArgs);
      }
    }
    return new ConstructorCallImpl(targetConstructor, constructorParameters);
  }
  
  /**
   * Try to guess the name of the proxy for a specified class
   * @param targetClass the source class
   * @return the name of the proxy class
   * @throws CannotGuessProxyNameException If the MOP cannot guess the name of the proxy
   */
  protected static String guessProxyName(Class targetClass) throws CannotGuessProxyNameException {
    int i;
    Class cl;
    Class myInterface = null;
    Class[] interfaces;
    Field myField = null;
    
    // Checks the cache
    String nameOfProxy = (String)secondProxyTable.get(targetClass.getName());
    if (nameOfProxy == null) {
      Class currentClass;
      // Checks if this class or any of its superclasses implements an
      //  interface that is a subinterface of ROOT_INTERFACE
      currentClass = targetClass;
      //System.out.println("MOP: guessProxyName for targetClass " + targetClass);
 
      while ((currentClass != null) && (myInterface == null)) {
        boolean multipleMatches = false;
        interfaces = currentClass.getInterfaces();
        for (i = 0; i < interfaces.length; i++) {
          if (ROOT_INTERFACE.isAssignableFrom(interfaces[i])) {
            if (multipleMatches == false) {
              myInterface = interfaces[i];
              multipleMatches = true;
            } else {
              // There are multiple interfaces in the current class
              // that inherit from ROOT_INTERFACE.
              System.err.println("More than one interfaces declared in class " + currentClass.getName() + " inherit from " + ROOT_INTERFACE + ". Using " + myInterface);
            }
          }
        }
        currentClass = currentClass.getSuperclass();
      }

      if (myInterface == null) {
        throw new CannotGuessProxyNameException("Class " + targetClass.getName() + " does not implement any interface that inherits from org.objectweb.proactive.core.mop.Reflect");
      }

      // Now look for the PROXY_CLASS_NAME field in this interface
      try {
        myField = myInterface.getField("PROXY_CLASS_NAME");
      } catch (NoSuchFieldException e) {
        throw new CannotGuessProxyNameException("No field PROXY_CLASS_NAME in interface " + myInterface);
      }

      try {
        nameOfProxy = (String)myField.get(null);
      } catch (IllegalAccessException e) {
        throw new CannotGuessProxyNameException("Cannot access field PROXY_CLASS_NAME in interface " + myInterface);
      }
      secondProxyTable.put(targetClass.getName(), nameOfProxy);
    }
    return nameOfProxy;
  }


  /**
   * Tries to solve ambiguity problems in constructors
   * @param targetClass the class
   * @param targetConstructorArgs The arguments which will determine wich constructor is to be used
   * @return The corresponding Constructor
   */
  protected static Constructor investigateAmbiguity(Class targetClass, Class[] targetConstructorArgs) {
    // Find the number of possible constructors ambiguities
    int n = 1;
    for (int i = 0; i < targetConstructorArgs.length; i++) {
      if (Utils.isWrapperClass(targetConstructorArgs[i]))
        n = n * 2;
    }
    if (n == 1) return null; // No wrapper found

    // For the moment, only try to convert all wrappers to their
    // corresponding primitive types and check if it matches
    for (int i = 0; i < targetConstructorArgs.length; i++) {
      if (Utils.isWrapperClass(targetConstructorArgs[i]))
        targetConstructorArgs[i] = Utils.getPrimitiveType(targetConstructorArgs[i]);
    }
    return findReifiedConstructor(targetClass, targetConstructorArgs);
  }


  /**
   * Finds the reified constructor 
   * @param targetClass The class
   * @param the effective arguments
   * @return The constructor
   */
  static Constructor findReifiedConstructor(Class targetClass, Class[] targetConstructorArgs) {
    Constructor[] publicConstructors;
    Constructor currentConstructor;
    Class[] currentConstructorParameterTypes;
    boolean match;

    publicConstructors = targetClass.getConstructors();
    // For each public constructor of the reified class
    for (int i = 0; i < publicConstructors.length; i++) {
      currentConstructor = publicConstructors[i];
      currentConstructorParameterTypes = currentConstructor.getParameterTypes();
      match = true;
      // Check if the parameters types of this constructor are
      // assignable from the actual parameter types.
      if (currentConstructorParameterTypes.length == targetConstructorArgs.length) {
        for (int j = 0; j < currentConstructorParameterTypes.length; j++) {
          if (!(currentConstructorParameterTypes[j].isAssignableFrom(targetConstructorArgs[j]))) {
            match = false;
            break;
          }
        }
      } else
        match = false;
      if (match == true)
        return currentConstructor;
    }
    return null;
  }


  /**
   *   Locates the file that contains the bytecode representation of the class
   *   whose name is <code>classname</code>. If the .class file is included as part
   *   of a jar or zip file, this method returns a File object that represents
   *   the archive file.
   */

  protected static java.io.File locateClassFile(String className) {
    java.io.File currentFile;
    java.io.File targetFile;

    // For each valid entry in the classpath
    for (int i = 0; i < classPathEntries.length; i++) {
      currentFile = classPathEntries[i];


      // If this File is a directory
      if (currentFile.isDirectory()) {
        // Creates class file's supposed filename
        String supposedFileName = currentFile.getAbsolutePath();
        supposedFileName = supposedFileName + Utils.getRelativePath(className);
        supposedFileName = supposedFileName + fileSeparator + Utils.getSimpleName(className) + ".class";

        targetFile = new java.io.File(supposedFileName);
        if (targetFile.exists())
          return targetFile;
      }
    }

    return null;
  }


  /**
   * Checks whether a file is a zip file
   * @param f the file
   * @return true if file is a zip
   */
  protected static boolean isZIPFile(java.io.File f) {
    try {
      java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(f);
    } catch (java.util.zip.ZipException e) {
      return false;
    } catch (java.io.IOException e) {
      return false;
    }
    return true;
  }


  /**
   * Checks whether the Stub should be re generated 
   * @param className the name of the source class
   * @return true if it should be generated
   */
  protected static boolean shouldRegenerateStub(String className) {
    // If date checking is turned off, simply return false
    if (! MOPProperties.getCheckStubDate()) return false;
    // Locates the file that contains the class
    java.io.File classFile = locateClassFile(className);

    // Locates the stub class
    String stubClassName = Utils.getPackageName(className) + ".Stub_" + Utils.getSimpleName(className);
    java.io.File stubClassFile = locateClassFile(stubClassName);

    // If we succeeded at locating both classes, check date difference
    if ((classFile != null) && (stubClassFile != null)) {
      return (stubClassFile.lastModified() - classFile.lastModified()) <= 0;
    }
    // By default, return false (which means don't regenerate stub class)
    return false;
  }


  /**
   * Dynamic cast
   * @param sourceObject The source object
   * @param targetTypeName the destination class
   * @return The resulting object
   * @throws ReifiedCastException if the class cast is invalid
   */
  public static Object castInto(Object sourceObject, String targetTypeName) throws ReifiedCastException {
    try {
      Class cl = forName(targetTypeName);
      return castInto(sourceObject, cl);
    } catch (ClassNotFoundException e) {
      throw new ReifiedCastException("Cannot load class " + targetTypeName);
      //		throw new ReifiedCastException ("Cannot cast "+sourceObject.getClass().getName()+" into "+targetTypeName);
    }
  }


  /**
   * Dynamic cast
   * @param sourceObject The source object
   * @param targetType the destination class
   * @return The resulting object
   * @throws ReifiedCastException if the class cast is invalid
   */
  public static Object castInto(Object sourceObject, Class targetType) throws ReifiedCastException {
    // First, check if sourceObject is a reified object
    if (!(isReifiedObject(sourceObject))) {
      throw new ReifiedCastException("Cannot perform a reified cast on an object that is not reified");
    }

    // Gets a Class object representing the type of sourceObject
    Class sourceType = sourceObject.getClass().getSuperclass();

    // Check if types are compatible
    // Here we assume that the 'type of the stub' (i.e, the type of the
    // reified object) is its direct superclass
    if (!((sourceType.isAssignableFrom(targetType)) || (targetType.isAssignableFrom(sourceType)))) {
      throw new ReifiedCastException("Cannot cast " + sourceObject.getClass().getName() + " into " + targetType.getName());
    }
    // Let's create a stub object for the target type
    Constructor stubConstructor = findStubConstructor(targetType);
    // Instanciates the stub object
    StubObject stub = instanciateStubObject(stubConstructor);
    // Connects the proxy of the old stub to the new stub
    stub.setProxy(((StubObject)sourceObject).getProxy());
    return stub;
  }
}
