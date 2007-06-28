package org.objectweb.proactive.examples.masterslave;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.objectweb.proactive.extra.masterslave.ProActiveMaster;


public abstract class AbstractExample {
    protected URL descriptor_url;
    protected String vn_name;
    protected String usage_message = "Usage: <java_command> descriptor_path virtual_node_name";
    protected int number_of_parameters = 2;

    /**
     * Returns the url of the descriptor which defines the slaves
     * @return descriptor url
     */
    public URL getDescriptor_url() {
        return descriptor_url;
    }

    /**
     * Sets the url of the descriptors which defines the slaves
     * @param descriptor_url
     */
    public void setDescriptor_url(URL descriptor_url) {
        this.descriptor_url = descriptor_url;
    }

    /**
     * Returns the virtual node name of the slaves
     * @return virtual node name
     */
    public String getVn_name() {
        return vn_name;
    }

    /**
     * Sets the virtual node name of the slaves
     * @param vn_name virtual node name
     */
    public void setVn_name(String vn_name) {
        this.vn_name = vn_name;
    }

    /**
     * Initializing the example with command line arguments
     * @param args command line arguments
     * @throws MalformedURLException
     */
    public void init(String[] args) throws MalformedURLException {
        this.init(args, 0, "");
    }

    /**
     * Initializing the example with command line arguments
     * @param args command line arguments
     * @param number_of_extra_parameters number of extra parameters used by the example (except from descriptor and virtual node name)
     * @param text text to add in error message
     * @throws MalformedURLException
     */
    protected void init(String[] args, int number_of_extra_parameters,
        String text) throws MalformedURLException {
        if (args.length == (number_of_parameters + number_of_extra_parameters)) {
            File descriptorFile = new File(args[0]);
            if (!descriptorFile.exists()) {
                System.err.println("" + descriptorFile + " does not exist");
                System.exit(1);
            } else if (!descriptorFile.canRead()) {
                System.err.println("" + descriptorFile + " can't be read");
                System.exit(1);
            } else if (!descriptorFile.isFile()) {
                System.err.println("" + descriptorFile +
                    " is not a regular file");
                System.exit(1);
            }
            descriptor_url = descriptorFile.toURI().toURL();
            vn_name = args[1];
            init_specialized(args);
        } else {
            System.err.println(usage_message + text);
            System.exit(1);
        }
    }

    /**
     * Register a shutdown hook on this example which will terminate the master
     */
    protected void registerHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownThread()));
    }

    /**
     * Initializing the example with command line arguments, needs to be overriden
     * @param args
     */
    protected abstract void init_specialized(String[] args);

    /**
     * Returns the actual master of the example
     * @return
     */
    protected abstract ProActiveMaster getMaster();

    /**
     * Internal class which handles shutdown of Master/Slave applications
     * @author fviale
     *
     */
    protected class ShutdownThread implements Runnable {
        public ShutdownThread() {
        }

        public void run() {
            getMaster().terminate(true);
        }
    }
}
