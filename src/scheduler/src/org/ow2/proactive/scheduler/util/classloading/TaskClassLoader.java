/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.util.classloading;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

import org.ow2.proactive.scheduler.util.classloading.ComputeNodeClassLoader.CNEnumeration;
import org.ow2.proactive.scheduler.util.classloading.resourcecache.Handler;


/**
 * This classLoader is used on compute nodes provided by the resource manager to instantiate
 * the executable. If a class is not found locally, then this class is asked to the
 * taskClassCerver associated to this TaskClassLoader.
 *
 * @see TaskClassServer 
 * @author The ProActive team 
 *
 */
public class TaskClassLoader extends ClassLoader {

    /* Used to detect recursion.
     * 
     * A HahShet is attached to each thread, before calling the parent 
     * class loader we put an identifier into the set. If the identifier
     * is already present in the set then we MUST NOT delegate the call
     * to the parent. 
     * 
     * One set is used for each operation. They could probably be merged into
     * a single set.
     */
    private final ThreadLocal<HashSet<String>> findClass;
    private final ThreadLocal<HashSet<String>> getResource;
    private final ThreadLocal<HashSet<String>> getResourceAsStream;

    /* An unique resource identifier to enable collaboration between the task 
     * class loader and the protocol handler
     */
    private final AtomicLong counter = new AtomicLong();

    /** The associated classserver on the scheduler core side */
    // Can be null if no classpath has been set for the job
    final private TaskClassServer remoteServer;

    /**
     * Create a new classloader.
     * @param parent the parent classloader.
     * @param remoteServer The associated classserver on the scheduler core side.
     */
    public TaskClassLoader(ClassLoader parent, TaskClassServer remoteServer) {
        super(parent);

        this.remoteServer = remoteServer;

        this.findClass = new ThreadLocal<HashSet<String>>();
        this.getResource = new ThreadLocal<HashSet<String>>();
        this.getResourceAsStream = new ThreadLocal<HashSet<String>>();
    }

    /* Overrides ClassLoader.loadClass to always delagate the call to findClass */
    @Override
    public synchronized Class<?> loadClass(String className) throws ClassNotFoundException {
        return this.findClass(className);
    }

    /*
     * Try to download the class from the remote class server. 
     * If the class is not remotely available, then the parent class loader 
     * is used.
     * 
     * It would be faster to try the parent class loader first (avoid 1RTT to load
     * a class available in the classpath), but I'm not sure it will not break everything.
     * Needs more investigations.
     */
    protected Class<?> findClass(String className) throws ClassNotFoundException {

        Class<?> res = this.findLoadedClass(className);
        if (res == null) {
            if (remoteServer != null) {
                // The remote call should be try-catched to be safe in case of 
                // network failure. But it is currently impossible
                byte[] classBytes = this.remoteServer.getClassBytes(className);
                if (classBytes != null && classBytes.length != 0) {
                    res = this.defineClass(className, classBytes, 0, classBytes.length);
                }
            }
        }

        // Ask to the parent (ComputeNodeClassLoader) and avoid infinite recursion
        if (res == null) {
            if (findClass.get() == null) {
                findClass.set(new HashSet<String>());
            }

            if (findClass.get().add(className)) {
                res = super.loadClass(className, true);
                findClass.get().remove(className);
            }
        }

        if (res == null) {
            throw new ClassNotFoundException("Class not found: " + className);
        }

        return res;
    }

    /*
     * Since getResource() is usually followed by URL.openConnection(), the 
     * resource is proactively downloaded (and cached) from the remote class server.
     * This behavior avoid 1 RTT. 
     * 
     * A custom protocol handler is used. It caches the downloaded class on the 
     * local runtime. So, URL.openConnection() does not perform any remote operation.
     * 
     * The caching algorithm could be improved to avoid multiple download of the same
     * resource. The current implemenation is trivial but safe.
     */
    @Override
    public URL getResource(String name) {
        URL url = null;

        InputStream is = this.getResourceAsStream(name);
        if (is != null) {
            Long id = counter.getAndIncrement();
            Handler.cache.addResource(id.toString(), is);
            try {
                url = new URL(Handler.scheme, "unused", id.toString());
            } catch (MalformedURLException e) {
                // Miam miam miam, cannot happen
            }
        }

        if (url == null) {
            if (getResource.get() == null) {
                getResource.set(new HashSet<String>());
            }

            if (getResource.get().add(name)) {
                url = super.getResource(name);
                getResource.get().remove(name);
            }
        }

        return url;
    }

    /*
     * The same algorithm than findClass is used. Ask to the remote class server first,
     * then use the parent class loader.
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        if (name == null)
            return null;

        InputStream is = null;

        // The remote class server expects class name, not a path.
        String className = name.replace('/', '.');
        className = className.replaceAll("\\.class$", "");

        if (this.remoteServer != null) {
            // It is not clear if getClassByte returns null or throws an Exception
            try {
                byte[] buf = this.remoteServer.getClassBytes(className);
                if (buf != null) {
                    is = new ByteArrayInputStream(buf);
                }
            } catch (ClassNotFoundException e) {
            }
        }

        if (is == null) {
            if (getResourceAsStream.get() == null) {
                getResourceAsStream.set(new HashSet<String>());
            }

            if (getResourceAsStream.get().add(name)) {
                is = super.getResourceAsStream(name);
                getResourceAsStream.get().remove(name);
            }
        }

        return is;
    }

    /* 
     * This implementation is broken an does not delegate the call to the
     * parent class loader. 
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        CNEnumeration<URL> urls = null;

        URL url = this.getResource(name);

        urls = new CNEnumeration<URL>(new Enumeration[] {});
        if (url != null) {
            urls.addElement(url);
        }

        return urls;
    }
}
