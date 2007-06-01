package org.objectweb.proactive.examples.masterslave;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public abstract class AbstractExample {
    protected URL descriptor_url;
    protected String vn_name;
    protected String usage_message = "Usage: <java_command> descriptor_path virtual_node_name";
    protected int number_of_parameters = 2;

    public URL getDescriptor_url() {
        return descriptor_url;
    }

    public void setDescriptor_url(URL descriptor_url) {
        this.descriptor_url = descriptor_url;
    }

    public String getVn_name() {
        return vn_name;
    }

    public void setVn_name(String vn_name) {
        this.vn_name = vn_name;
    }

    /**
     * Initializing the example with command line arguments
     * @param args
     * @throws MalformedURLException
     */
    public void init(String[] args) throws MalformedURLException {
        this.init(args, 0, "");
    }

    /**
     * Initializing the example with command line arguments
     * @param args
     * @param number_of_extra_parameters number of extra parameters used by the example (except from descriptor and virtual node name)
     * @param text text to add in error message
     * @throws MalformedURLException
     */
    public void init(String[] args, int number_of_extra_parameters, String text)
        throws MalformedURLException {
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
     * Initializing the example with command line arguments, needs to be overriden
     * @param args
     */
    protected abstract void init_specialized(String[] args);
}
