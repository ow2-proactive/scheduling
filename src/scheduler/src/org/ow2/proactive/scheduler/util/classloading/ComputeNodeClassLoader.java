package org.ow2.proactive.scheduler.util.classloading;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;


/** The system class loader of compute nodes
 * 
 * Computes node must have a dedicated class loader for each task; the task 
 * class loader. Unfortunately, due to ProActive restrictions, it is not possible
 * to set the task class loader as a thread context class loader (ProActive would not
 * be able to generate the stub classes).
 * 
 * Therefore, a custom system class loader must be set. It is known as the
 * {@link ComputeNodeClassLoader}. It inherits from the URLClassLoader and 
 * will load the class from the VM classpath. In addition, if the task class 
 * loader is set, the {@link ComputeNodeClassLoader} use it is a class is not 
 * available in the classpath.
 * 
 * When a task finish, the task class loader is unset. It will be garbage collected
 * and the classes will be unloaded.
 */

public class ComputeNodeClassLoader extends URLClassLoader {
    /** The curent task class loader, can be null */
    private AtomicReference<ClassLoader> tcl;

    final private ClassLoader parent;

    public ComputeNodeClassLoader(ClassLoader parent) {
        super(new URL[] {}, parent);
        this.parent = parent;

        // Add the classpath
        final String s = System.getProperty("java.class.path");
        final File[] path = (s == null) ? new File[0] : getClassPath(s);
        final URL[] urls = (s == null) ? new URL[0] : pathToURLs(path);
        for (URL url : urls) {
            super.addURL(url);
        }

        this.tcl = new AtomicReference<ClassLoader>(null);
    }

    /** Set the current classloader */
    public void setTaskClassLoader(ClassLoader tcl) {
        this.tcl.set(tcl);
    }

    /* Get from the resource from the parent class loader. If the resource
     * is not found then try to use the task class loader (if set)
     */
    @Override
    public URL getResource(String name) {
        URL url = null;
        url = super.getResource(name);

        if (url == null) {
            ClassLoader cl = tcl.get();
            if (cl != null) {
                url = cl.getResource(name);
            }
        }

        return url;
    }

    /* Get from the resource from the parent class loader. If the resource
     * is not found then try to use the task class loader (if set)
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream is = null;

        is = super.getResourceAsStream(name);

        if (is == null) {
            ClassLoader cl = tcl.get();
            if (cl != null) {
                is = cl.getResourceAsStream(name);
            }
        }

        return is;
    }

    /* Merge the results of the parent and the task class loaders. */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {

        Enumeration<URL> parent = super.getResources(name);

        Enumeration<URL> task = null;
        ClassLoader cl = tcl.get();
        if (cl != null) {
            task = cl.getResources(name);
        }

        Enumeration<URL>[] array;
        array = (task == null ? new Enumeration[] { parent } : new Enumeration[] { parent, task });
        return new CNEnumeration<URL>(array);
    }

    /* 
     * 
     * 
     */
    protected Class<?> findClass(final String name) throws ClassNotFoundException {

        Class<?> clazz = null;
        try {
            // Search in the classpath
            clazz = super.findClass(name);
        } catch (ClassNotFoundException e1) {
            try {
                // Search in the parent class loader (AppClassLoader -> Extensions -> Bootstrap)
                clazz = parent.loadClass(name);
            } catch (ClassNotFoundException e2) {
                // If set, search in the task class loader
                ClassLoader cl = tcl.get();
                if (cl != null) {
                    try {
                        clazz = cl.loadClass(name);
                    } catch (ClassNotFoundException e3) {
                        // Houston, we have a problem: Class  Not Found
                    }
                }
            }
        }

        if (clazz == null) {
            throw new ClassNotFoundException("Class not found " + name);
        }

        return clazz;
    }

    /* Override the ClassLoader.loadClass() method to use our own search algorithm */
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;

        // DO NOT load ourself. ComputeNodeClassLoader must only be loaded by the AppClassLoader
        if (!name.equals("org.ow2.proactive.scheduler.util.classloading.ComputeNodeClassLoader")) {
            try {
                // First, check if the class has already been loaded
                clazz = findLoadedClass(name);
                if (clazz == null) {
                    clazz = this.findClass(name);
                }
            } catch (ClassNotFoundException e) {
                // Miam miam miam
            }
        }

        if (clazz == null) { // It's not an "else"
            clazz = super.loadClass(name, resolve);
        }

        return clazz;
    }

    /*
     *  getClassPath, getFileURL and pathToURLs come from Open JDK 6
     */
    private static File[] getClassPath(String cp) {
        File[] path;
        if (cp != null) {
            int count = 0, maxCount = 1;
            int pos = 0, lastPos = 0;
            // Count the number of separators first
            while ((pos = cp.indexOf(File.pathSeparator, lastPos)) != -1) {
                maxCount++;
                lastPos = pos + 1;
            }
            path = new File[maxCount];
            lastPos = pos = 0;
            // Now scan for each path component
            while ((pos = cp.indexOf(File.pathSeparator, lastPos)) != -1) {
                if (pos - lastPos > 0) {
                    path[count++] = new File(cp.substring(lastPos, pos));
                } else {
                    // empty path component translates to "."
                    path[count++] = new File(".");
                }
                lastPos = pos + 1;
            }
            // Make sure we include the last path component
            if (lastPos < cp.length()) {
                path[count++] = new File(cp.substring(lastPos));
            } else {
                path[count++] = new File(".");
            }
            // Trim array to correct size
            if (count != maxCount) {
                File[] tmp = new File[count];
                System.arraycopy(path, 0, tmp, 0, count);
                path = tmp;
            }
        } else {
            path = new File[0];
        }
        return path;
    }

    private static URL[] pathToURLs(File[] path) {
        URL[] urls = new URL[path.length];
        for (int i = 0; i < path.length; i++) {
            urls[i] = getFileURL(path[i]);
        }
        return urls;
    }

    private static URL getFileURL(File file) {
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
        }

        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            // Should never happen since we specify the protocol...
            throw new InternalError();
        }
    }

    static public class CNEnumeration<E> implements Enumeration<E> {
        final private LinkedList<E> elements = new LinkedList<E>();
        private int index = 0;

        public CNEnumeration(Enumeration<E>[] enums) {
            for (Enumeration<E> en : enums) {
                while (en.hasMoreElements()) {
                    E e = en.nextElement();
                    elements.add(e);
                }
            }

        }

        public boolean hasMoreElements() {
            return index < elements.size();
        }

        public E nextElement() {
            if (!hasMoreElements()) {
                throw new NoSuchElementException();
            }

            return (E) elements.get(index++);
        }

        public void addElement(E e) {
            elements.add(e);
        }
    }

}
