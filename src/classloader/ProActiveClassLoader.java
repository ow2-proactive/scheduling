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

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;


/**
 * @author fhuet
 *
 */
public class ProActiveClassLoader extends RMIClassLoaderSpi {

    protected static Logger logger = Logger.getLogger(ProActiveClassLoader.class.getName());
    protected ClassLoader parent = Thread.currentThread().getContextClassLoader();
    protected RMIClassLoaderSpi defaultProvider = RMIClassLoader.getDefaultProviderInstance();

    //  protected MOPClassLoader mopClassLoader = new MOPClassLoader();
    public ProActiveClassLoader() {
        System.out.println("ProactiveClassLoader");
    }

    public Class loadClass(String codebase, String name,
        ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException {
        logger.debug("loadClass " + name + "with classloader " + defaultLoader +
            " with codebase " + codebase);

        try {
            if (defaultLoader == null) {
                if (codebase != null) {

                    URL[] urls = new URL[] { new URL(codebase) };
                    CustomURLClassLoader ucl = new CustomURLClassLoader(urls);

                    //CustomURLClassLoader.newInstance(urls);
                    Class c = ucl.loadClass(name);
                    logger.debug(c);

                    return c;
                } else {
                    return parent.loadClass(name);
                }
            } else {
                return defaultLoader.loadClass(name); //this.parent.loadClass(codebase, name, defaultLoader);
            }
        } catch (ClassNotFoundException e) {
            logger.debug("class not found, should call MOPClassLoader");
            e.printStackTrace();
            throw e;
        }
    }

    public Class loadProxyClass(String codebase, String[] interfaces,
        ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException {
        logger.debug("loadProxyClass ");

        // return super.loadProxyClass(codebase, interfaces, defaultLoader);
        return defaultProvider.loadProxyClass(codebase, interfaces,
            defaultLoader);
    }

    public ClassLoader getClassLoader(String codebase)
        throws MalformedURLException {
        logger.debug("getClassLoader " + codebase);
        return defaultProvider.getClassLoader(codebase);
    }

    public String getClassAnnotation(Class cl) {
        logger.debug("getClassAnnotation " + cl);
        return defaultProvider.getClassAnnotation(cl);

        //return null;
    }
}
