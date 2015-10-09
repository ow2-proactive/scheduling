/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scripting.helper.selection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * SelectionUtils provides static methods for selection script.<br />
 * This class also defines the public operator that can be used in a script.<br />
 * So, when creating a Condition object pleased used LESS_THAN, GREATER_THAN, EQUAL or MATCH.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
public class SelectionUtils {

    /** Less than operator */
    public static final int LESS_THAN = 1;
    /** Greater than operator */
    public static final int GREATER_THAN = 2;
    /** Equal than operator */
    public static final int EQUAL = 3;
    /** Pattern matching operator */
    public static final int CONTAINS = 4;

    private static final String winTestCuda = "deviceQueryWin.exe";
    //TODO get cuda check on UNIX
    private static final String unixTestCuda = "deviceQueryUnix";
    private static final boolean isWindows = System.getProperty("os.name").contains("Windows");
    private static boolean isJ6 = !System.getProperty("java.version").contains("1.5");

    /**
     * Check all given conditions in the given configuration file path.<br>
     * This method returns true if (and only if) every conditions match the given file.
     *
     * @param configFilePath configuration path
     * @param params the conditions object (which is a collection of condition objects )
     * @return true if every conditions match the given file.
     */
    public static boolean checkProperties(String configFilePath, Conditions conditions) {
        //open properties file
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(configFilePath);
            properties.load(fis);
            fis.close();
            //Check properties for each condition
            for (Condition condition : conditions) {
                if (!checkProperty(properties, condition)) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //All conditions have been validated if ok == true
        return true;
    }

    /**
     * Check the condition in the given properties. <br />
     * Return true if the condition is accepted according to the given properties.
     *
     * @param props the properties
     * @param condition the condition to test
     * @return true if the condition is accepted according to the given configuration file.
     */
    private static boolean checkProperty(Properties props, Condition condition) {
        String key = condition.getName();
        try {
            switch (condition.getOperator()) {
                case LESS_THAN:
                    return (Double.parseDouble(props.getProperty(key)) < Double.parseDouble(condition
                            .getValue()));
                case GREATER_THAN:
                    return (Double.parseDouble(props.getProperty(key)) > Double.parseDouble(condition
                            .getValue()));
                case EQUAL:
                    return props.getProperty(key).equals(condition.getValue());
                case CONTAINS:
                    return props.getProperty(key).contains(condition.getValue());
                default:
                    System.out.println("Invalid operator, please use INFERIOR, SUPERIOR, MATCH or EQUAL");
                    return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Check the condition in the given configuration file. <br />
     * Return true if the condition is accepted according to the given configuration file.
     *
     * @param configFilePath configuration file path
     * @param condition the condition to test
     * @return true if the condition is accepted according to the given configuration file.
     */
    public static boolean checkProperty(String configFilePath, Condition condition) {
        // Opening of the property file
        Properties props = new Properties();
        try {
            FileInputStream stream = new FileInputStream(configFilePath);
            props.load(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        //checking condition in properties
        return checkProperty(props, condition);
    }

    /**
     * Check if the host name is the given one.
     *
     * @param hostName the host name to check
     * @return true if the given host is equals (ignore case) to the physic host name
     */
    public static boolean checkHostName(String hostName) {
        if (hostName == null) {
            System.err.println("Given HOST name was NULL");
            return false;
        }
        try {
            String localHost = InetAddress.getLocalHost().getHostName();
            Pattern regex = Pattern.compile(hostName, Pattern.CASE_INSENSITIVE);
            Matcher regexMatcher = regex.matcher(localHost);
            return (regexMatcher.find());
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Check if the host matches the given IP address.<br>
     * The IP can be given as x.x.x.x or using the token * to match a network for example. (ie x.x.x.*)
     *
     * @param network the network to match
     * @return true if the IP address matches the network
     */
    public static boolean checkIp(String network) {
        String[] networkClasses = network.trim().split("\\.");
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(networks)) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    // Check if inetAddress is from particular address
                    try {
                        String ip = inetAddress.getHostAddress();
                        String[] ipClasses = ip.trim().split("\\.");

                        // Check foreach network class
                        boolean res = true;
                        for (int i = 0; i < ipClasses.length; i++) {
                            networkClasses[i] = networkClasses[i].replaceAll("\\*", ".*");
                            Pattern regex = Pattern.compile(networkClasses[i], Pattern.CASE_INSENSITIVE);
                            Matcher regexMatcher = regex.matcher(ipClasses[i]);
                            if (!regexMatcher.find()) {
                                res = false;
                            }
                        }
                        // Match found
                        if (res == true) {
                            return true;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check if the given file path exist or not.
     *
     * @param filePath the file path to check
     * @return true if the given file path exists
     */
    public static boolean checkFileExist(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * Check the OS Name (case-insensitive)
     *
     * @param exp the regular expression which is required
     * @return true if the OS name is the one expected
     */
    public static boolean checkOSName(String exp) {
        if (exp == null) {
            System.err.println("Given OS name was NULL");
            return false;
        }

        String localOS = System.getProperty("os.name");

        try {
            Pattern regex = Pattern.compile(exp, Pattern.CASE_INSENSITIVE);
            Matcher regexMatcher = regex.matcher(localOS);
            return (regexMatcher.find());
        } catch (PatternSyntaxException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check the OS Architecture
     *
     * @param osArch the require OS Architecture
     * @return true if the OS architecture is the one expected
     */
    public static boolean checkOSArch(String osArch) {
        if (osArch == null) {
            System.err.println("Given OS architecture was NULL");
            return false;
        }

        String localOSArch = System.getProperty("os.arch");
        if (localOSArch.toUpperCase().contains(osArch.toUpperCase())) {
            return true;
        }
        return false;
    }

    /**
     * Check the OS Version
     *
     * @param osVersion the required OS Version
     * @return true if the OS version is the one expected
     */
    public static boolean checkOSVersion(String osVersion) {
        if (osVersion == null) {
            System.err.println("Given OS version was NULL");
            return false;
        }

        String localOSVersion = System.getProperty("os.version");
        if (localOSVersion.contains(osVersion)) {
            return true;
        }
        return false;
    }

    /**
     * Check a Java Property
     *
     * @param propertyName the name of the property.
     * @param propertyValue the excepted value.
     * @return true if the couple exists
     */
    public static boolean checkJavaProperty(String propertyName, String propertyValue) {
        if (propertyName == null) {
            System.err.println("Given property Name was NULL");
            return false;
        }
        if (propertyValue == null) {
            System.err.println("Given property Value was NULL");
            return false;
        }
        try {
            String vmPropValue = System.getProperty(propertyName);
            Pattern regex = Pattern.compile(propertyValue, Pattern.CASE_INSENSITIVE);
            Matcher regexMatcher = regex.matcher(vmPropValue);
            return (regexMatcher.find());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check if CUDA is set-up (for Windows System only)
     *
     * @return true if CUDA is detected on the system.
     */
    public static boolean checkCudaWin() {
        if (!isWindows) {
            System.err.println("Error trying to check Cuda library : the system must be under Windows.");
            return false;
        }
        File tmp = null;
        try {
            InputStream is = SelectionUtils.class.getResourceAsStream(winTestCuda);
            tmp = File.createTempFile("wcuda", ".exe");
            FileOutputStream fos = new FileOutputStream(tmp);
            //copy
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            is.close();
            fos.close();
            //execute
            Process p = Runtime.getRuntime().exec(tmp.getAbsolutePath());
            p.waitFor();
            return (p.exitValue() > 0);
        } catch (IllegalMonitorStateException ex) {
            ex.printStackTrace();
        } catch (InterruptedException in) {
            in.printStackTrace();
        } catch (Exception err) {
            err.printStackTrace();
        } finally {
            if (tmp != null) {
                tmp.delete();
            }
        }
        return false;
    }

    /**
     * Check if there is enough free memory.
     *
     * @param requiredMemory the minimum amount of memory which is required (measured in byte)
     * @return true if the required memory is equal or lesser than the available memory, false otherwise.
     */
    public static boolean checkFreeMemory(long requiredMemory) {
        if (Runtime.getRuntime().freeMemory() >= requiredMemory) {
            return true;
        }
        return false;
    }

    /**
     * Check if a file exist in folders contains in PATH environment variable
     *
     * @param fileName the name of the file which is required
     * @return true if fileName exist in the "PATH" environment variable
     */
    public static boolean checkExec(String fileName) {
        String path = System.getenv("PATH");
        String[] tokens = path.split(File.pathSeparator);
        for (String folder : tokens) {
            // Browse each folder
            File directory = new File(folder);

            if (!directory.exists()) {
                System.err.println(folder + " doesn't exist");
            } else if (!directory.isDirectory()) {
                System.err.println(folder + "' is not a directory");
            } else {
                File[] subfiles = directory.listFiles();
                for (int i = 0; i < subfiles.length; i++) {
                    // check if it matches
                    if (subfiles[i].getName().equals(fileName)) {
                        return true;
                    }
                }
            }
        }
        System.err.println(fileName + " is not in PATH environment variable.");
        return false;
    }

    /**
     * Check if a wireless network interface exist
     *
     * @return true if a wireless network interface has been found
     */
    public static boolean checkWifi() {
        Enumeration<NetworkInterface> interfaces;
        try {
            // Get all interfaces
            interfaces = NetworkInterface.getNetworkInterfaces();

            // Check for each by name
            while (interfaces.hasMoreElements()) {
                NetworkInterface currentInterface = interfaces.nextElement();
                if (currentInterface.getName().contains("wlan")) {
                    return true;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if free space (for a specify path) is greater than space
     *
     * @param space the minimum required space
     * @param path the path which have the required space
     * @return true if free space (for a specify path) is greater or equal than space
     */
    public static boolean checkFreeSpaceDiskAvailable(Long space, String path) {
        if (!isJ6) {
            System.err.println("Check only available with java 6 or later.");
            return false;
        }

        if (path == null || space == null) {
            return false;
        }

        try {
            File file = new File(path);
            Method m = File.class.getDeclaredMethod("getFreeSpace");
            if (space <= (Long) m.invoke(file)) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Check if default free space (tmpdir) is greater than parameter
     *
     * @param space the minimum required space
     * @return true if free space (tmpdir) is greater than space
     */
    public static boolean checkFreeSpaceDiskAvailableForTmpDir(Long space) {
        return checkFreeSpaceDiskAvailable(space, System.getProperty("java.io.tmpdir"));
    }

}