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
package classloader;

import org.apache.log4j.Logger;

import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;


/**
 * @author fhuet
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CustomURLClassLoader extends URLClassLoader {

    protected static Logger logger = Logger.getLogger(CustomURLClassLoader.class.getName());
    protected URLClassPath ucp;

    public CustomURLClassLoader(URL[] urls) {
        super(urls);
        ucp = new URLClassPath(urls);
    }

    public CustomURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        logger.debug("Looking for class " + name);

        String path = name.replace('.', '/').concat(".class");
        Resource res = ucp.getResource(path, false);
        logger.debug("Got resource " + res);
        if (res != null) {
            try {
                byte[] b = res.getBytes();
                Class clc = Class.forName("java.lang.ClassLoader");
                Class[] argumentTypes = new Class[5];
                argumentTypes[0] = name.getClass();
                argumentTypes[1] = b.getClass();
                argumentTypes[2] = Integer.TYPE;
                argumentTypes[3] = Integer.TYPE;
                argumentTypes[4] = Class.forName(
                        "java.security.ProtectionDomain");

                Method m = clc.getDeclaredMethod("defineClass", argumentTypes);
                m.setAccessible(true);

                Object[] effectiveArguments = new Object[5];
                effectiveArguments[0] = name;
                effectiveArguments[1] = b;
                effectiveArguments[2] = new Integer(0);
                effectiveArguments[3] = new Integer(b.length);
                effectiveArguments[4] = this.getClass().getProtectionDomain();
                logger.debug("Context class loader " +
                    Thread.currentThread().getContextClassLoader());

                Class tmpClass = (Class) m.invoke(Thread.currentThread()
                                                        .getContextClassLoader(),
                        effectiveArguments);
                logger.debug("Calling defineClass");

              //  Class tmpClass = 
                 super.defineClass(name, b, 0, b.length);
                return tmpClass;
            } catch (Throwable ex) {
                ex.printStackTrace();
                throw new ClassNotFoundException(ex.getMessage());
            }
        } else {
            throw new ClassNotFoundException(name);
        }
    }

//    public synchronized Class loadClass(String name)
//        throws ClassNotFoundException {
//        logger.debug(name);
//        return super.loadClass(name);
//    }
//
//    protected synchronized Class loadClass(String name, boolean resolve)
//        throws ClassNotFoundException {
//
//        Class c = super.loadClass(name, resolve);
//        return c;
//    }
}
