package org.objectweb.proactive.ic2d.p2PMonitoring;

import javassist.ClassClassPath;
import javassist.ClassPool;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class P2PMonitoringPlugin extends AbstractUIPlugin {
    //The shared instance.
    private static P2PMonitoringPlugin plugin;

    /**
     * The constructor.
     */
    public P2PMonitoringPlugin() {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        System.out.println("P2PMonitoringPlugin.start()");
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(this.getClass()));
        //       try{
        //			RuntimeFactory.getDefaultRuntime();
        //		}
        //		catch(ProActiveException e) {
        //			//TODO log?
        //			e.printStackTrace();
        //		}
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static P2PMonitoringPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path.
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("P2PMonitoring", path);
    }
}
