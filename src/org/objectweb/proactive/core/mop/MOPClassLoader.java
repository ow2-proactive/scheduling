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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;

public class MOPClassLoader extends URLClassLoader {
    protected static Hashtable classDataCache = new Hashtable ();

    public byte[] getClassData (String classname)
    {
	return (byte[]) classDataCache.get (classname);
    }


    public MOPClassLoader(ClassLoader parent, URL[] urls) {
        super(urls, parent);
    }

    public void launchMain(String[] args) throws Throwable {
        try {
            // Looks up the class that contains main
            Class cl = Class.forName(args[0], true, this);

            // Looks up method main
            Class[] argTypes = {args.getClass()};
            Method mainMethod = cl.getMethod("main", argTypes);

            // And calls it
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            Object[] mainArgs = {newArgs};
            mainMethod.invoke(null, mainArgs);
        } catch (ClassNotFoundException e) {
            System.out.println("Launcher: cannot find class " + args[0]);
        } catch (NoSuchMethodException e) {
            System.out.println("Launcher: class " + args[0] + " does not contain have method void 'public void main (String[])'");
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        return;
    }

    public static MOPClassLoader createMOPClassLoader() {
        // Gets the current classloader
        ClassLoader currentClassLoader = null;
        try {
            Class c = Class.forName("org.objectweb.proactive.core.mop.MOPClassLoader");
            currentClassLoader = c.getClassLoader();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Checks if the current classloader is actually an instance of
        // java.net.URLClassLoader, or of one of its subclasses.
        if (currentClassLoader instanceof java.net.URLClassLoader) {
//      System.out.println ("Current classloader is of type "+currentClassLoader.getClass().getName()+", compatible with URLClassLoader");
        } else {
            System.out.println("Current classloader is of type " + currentClassLoader.getClass().getName() + ", which is not compatible with URLClassLoader. Cannot install MOPClassLoader");
            return null;
        }

        // Retrieves the set of URLs from the current classloader
        URL[] urls = ((URLClassLoader) currentClassLoader).getURLs();

        // Creates a new MOPClassLoader
        return new MOPClassLoader(currentClassLoader, urls);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name, false);
    }

    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
       	   
		if (this.getParent() != null)
		    {
			Class cl = null;
			try
			    {			       
				cl = this.getParent().loadClass (name);
			
				return cl;
			    }
			catch (ClassNotFoundException e)
			    {
					      
				// proceeding
			    }
		    }
	
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            // Test if the name of the class is actually a request for
            // a stub class to be created
            if (Utils.isStubClassName(name)) {
	System.out.println ("Maybe I could generate this class: "+name);
                String classname = Utils.convertStubClassNameToClassName(name);
                //try
                {
                    BytecodeStubBuilder bsb = new BytecodeStubBuilder(classname);
                    byte[] data = bsb.create();

		    MOPClassLoader.classDataCache.put (classname, data);

//		    System.out.println ("Classfile created with length "+data.length);

                    // Now, try to define the class
                    // We use the method defineClass, as redefined in class SecureClassLoader,
                    // so that we can specify a SourceCode object
		    //                    Class c = this.defineClass(name, data, 0, data.length, this.getClass().getProtectionDomain().getCodeSource());
		     
		    //   this.getParent().findClass("toto");
//		    Class c = this.getParent().defineClass(name, data, 0, data.length, this.getClass().getProtectionDomain());
		    Class c = null;

		    // The following code invokes defineClass on the parent classloader by Reflection
		    try
			{
			    Class clc = Class.forName ("java.lang.ClassLoader");
			    Class[] argumentTypes = new Class[5];
			    argumentTypes [0] = name.getClass();
			    argumentTypes [1] = data.getClass();
			    argumentTypes [2] = Integer.TYPE;
			    argumentTypes [3] = Integer.TYPE;
			    argumentTypes [4] = Class.forName ("java.security.ProtectionDomain");
			    Method m = clc.getDeclaredMethod ("defineClass", argumentTypes);
			    m.setAccessible (true);
			    Object[] effectiveArguments = new Object [5];
			    effectiveArguments [0] = name;
			    effectiveArguments [1] = data;
			    effectiveArguments [2] = new Integer (0);
			    effectiveArguments [3] = new Integer (data.length);
			    effectiveArguments [4] = this.getClass().getProtectionDomain();
			    c = (Class) m.invoke (this.getParent(), effectiveArguments);
			}
		    catch (Exception ex)
			{
			    ex.printStackTrace();
			}
		   


//	  System.out.println ("Class object is "+c);
                    return c;
                }
            } else {
	System.out.println ("I give up on this one: "+name);
                throw e;
            }
        }
    }
}

