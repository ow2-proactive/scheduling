package org.objectweb.proactive.extra.gcmdeployment;

import java.io.File;


public class Helpers {

    /**
    * Checks that descriptor exist, is a file and is readable
    * @param descriptor The File to be checked
    * @throws IllegalArgumentException If the File is does not exist, is not a file or is not readable
    */
    public static File checkDescriptorFileExist(File descriptor)
        throws IllegalArgumentException {
        if (!descriptor.exists()) {
            throw new IllegalArgumentException(descriptor.getName() +
                " does not exist");
        }
        if (!descriptor.isFile()) {
            throw new IllegalArgumentException(descriptor.getName() +
                " is not a file");
        }
        if (!descriptor.canRead()) {
            throw new IllegalArgumentException(descriptor.getName() +
                " is not readable");
        }

        return descriptor;
    }

    static public String escapeCommand(String command) {
        // At each step, the command must be protected with " or ' and the command
        // passed as parameter must be escaped. This can be quite difficult since
        // Runtime.getRuntime().exec() only take an array of String as parameter...    	
        String res = command.replaceAll("'", "'\\\\''");
        return "'" + res + "'";
    }
}
