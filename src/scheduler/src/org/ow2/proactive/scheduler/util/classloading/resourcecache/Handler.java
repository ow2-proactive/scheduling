package org.ow2.proactive.scheduler.util.classloading.resourcecache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.ow2.proactive.scheduler.util.classloading.TaskClassServer;


/** A custom protocol handler used by the task class loader
 * 
 * Resources are downloaded by the task class loader then stored by this protocol 
 * handler.
 */
public class Handler extends URLStreamHandler {
    /** URL scheme*/
    final static public String scheme = "resourcecache";

    final private String resourceName;
    final private AtomicReference<byte[]> resource;
    final private TaskClassServer tcs;

    public Handler(final String resourceName, final TaskClassServer tcs, final long ctime, final Timer timer) {
        this.resourceName = resourceName;
        this.resource = new AtomicReference<byte[]>();

        this.tcs = tcs;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                resource.set(null);
            }
        }, ctime);
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        byte[] b = this.resource.get();
        if (b == null) {
            // The resource has been evicted from the cache, re-download it

            String className = this.resourceName.replace('/', '.');
            className = className.replaceAll("\\.class$", "");

            // It is not clear if getClassByte returns null or throws an Exception
            try {
                b = this.tcs.getClassBytes(className);
            } catch (ClassNotFoundException e) {
            }
        }

        if (b == null) {
            throw new IOException("Resource " + this.resourceName +
                " is not in cache and cannot be downloaded");
        }

        return new ClassLoaderConnection(u, new ByteArrayInputStream(b));
    }

    static public class ClassLoaderConnection extends URLConnection {
        final private InputStream is;

        public ClassLoaderConnection(URL url, InputStream is) {
            super(url);
            this.is = is;
        }

        @Override
        public void connect() throws IOException {
            // DO NOTHING
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return is;
        }
    }
}
