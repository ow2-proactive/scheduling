/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.scripting.helper.selection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jruby.RubyArray;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;
import org.objectweb.proactive.annotation.PublicAPI;
import org.python.core.PyList;


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
    //import org.uwin.registry.RegException;
    //import org.uwin.registry.RegFolder; >>> use uwin-1.0.0.jar & uwin-1.0.0.dll if needed

    /** Less than operator */
    public static final int LESS_THAN = 1;
    /** Greater than operator */
    public static final int GREATER_THAN = 2;
    /** Equal than operator */
    public static final int EQUAL = 3;
    /** Pattern matching operator */
    public static final int MATCH = 4;

    private static final String winTestCuda = "deviceQueryWin.exe";
    private static final String unixTestCuda = "deviceQueryUnix";
    private static final boolean isWindows = System.getProperty("os.name").contains("Windows");

    /**
     * Hack to manage different package name for NativeArray, NativeJavaObject and Scriptable classes
     * Java 6 or later package : sun.org.mozilla.javascript.internal
     * Java 5 or earlier package : org.mozilla.javascript
     */
    private static Class<?> nativeArray;
    private static Class<?> nativeJavaObject;
    private static Class<?> scriptable;

    static {
        String packageNameJ6 = "sun.org.mozilla.javascript.internal.";
        String packageNameJ5 = "org.mozilla.javascript.";
        try {
            nativeArray = Class.forName(packageNameJ6 + "NativeArray");
            nativeJavaObject = Class.forName(packageNameJ6 + "NativeJavaObject");
            scriptable = Class.forName(packageNameJ6 + "Scriptable");
        } catch (ClassNotFoundException e) {
            try {
                nativeArray = Class.forName(packageNameJ5 + "NativeArray");
                nativeJavaObject = Class.forName(packageNameJ5 + "NativeJavaObject");
                scriptable = Class.forName(packageNameJ5 + "Scriptable");
            } catch (ClassNotFoundException e1) {
                //it won't works with javascript engine
                e1.printStackTrace();
            }
        }
    }

    /**
     * Check all given conditions in the given configuration file path.<br>
     * This method returns true if (and only if) every conditions match the given file.
     *
     * @param configFilePath configuration path
     * @param params the conditions object (must be given as RubyArray (ruby), NativeArray (js) or PyList (python) )
     * @return true if every conditions match the given file.
     */
    public static boolean checkProperties(String configFilePath, Object params) {
        Condition[] conditions = null;

        //convert into the proper array
        if (params instanceof RubyArray) {
            conditions = convertRubyArrayToConditionArray((RubyArray) params);
        } else if (nativeArray.isInstance(params)) {
            conditions = convertNativeArrayToConditionArray(nativeArray.cast(params));
        } else if (params instanceof PyList) {
            conditions = convertPyListToConditionArray((PyList) params);
        } else {
            System.err.println("Unmanaged conditions parameter : " + params +
                ". It must be a script language array.");
            return false;
        }

        boolean ok = true;

        //open properties file
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(configFilePath);
            properties.load(fis);
            fis.close();
            //Check properties for each condition
            for (Condition condition : conditions) {
                if (!checkProperty(properties, condition)) {
                    ok = false;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ok = false;
        }

        //All conditions have been validated if ok == true
        return ok;
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
                case MATCH:
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
            props.load(new FileInputStream(configFilePath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        //checking condition in properties
        return checkProperty(props, condition);
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
        } catch (NullPointerException ex) {
            //ex.printStackTrace();
        } catch (PatternSyntaxException ex) {
            //ex.printStackTrace();
        } catch (Exception ex) {
            //ex.printStackTrace();
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
     * Check if their is enough free memory.
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

    //    /**
    //     * Check if a Windows key registry exist
    //     * Exemple: HKEY_LOCAL_MACHINE\HARDWARE\DESCRIPTION\System\CentralProcessor
    //     *
    //     * @param path the path of the registry key which is required
    //     * @return true if key registry exist
    //     */
    //    public static boolean checkKeyRegistry(String path) {
    //        if (path == null){
    //        	System.err.println("Given path was NULL.");
    //            return false;
    //        }
    //        if (!isWindows){
    //    		System.err.println("Error trying to check key registry : the system must be under Windows.");
    //    		return false;
    //    	}
    //
    //        try {
    //            new RegFolder(path, false);
    //            return true;
    //        } catch (RegException ex) {
    //            return false;
    //        }
    //
    //        return false;
    //    }

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

    //    /**
    //     * Check if free space (for a specify path) is greater than space
    //     *
    //     * @param space the minimum required space
    //     * @param path the path which have the required space
    //     * @return true if free space (for a specify path) is greater or equal than space
    //     */
    //    public static boolean checkFreeSpaceDiskAvailable(Long space, String path) {
    //        if (path == null || space == null){
    //            return false;
    //        }
    //
    //        try {
    //            File file = new File(path);
    //            if (space <= file.getFreeSpace()) {
    //                return true;
    //            }
    //            return false;
    //        } catch (NullPointerException ex) {
    //            ex.printStackTrace();
    //            return false;
    //        }
    //    }
    //
    //    /** Check if default free space (tmpdir) is greater than parameter
    //     * @param space the minimum required space
    //     * @return true if free space (tmpdir) is greater than space
    //     */
    //    public static boolean checkFreeSpaceDiskAvailableForTmpDir(Long space) {
    //        return checkFreeSpaceDiskAvailable(space, System.getProperty("java.io.tmpdir"));
    //    }

    /**
     * Convert ruby array into java array
     *
     * @param params the ruby array to convert
     */
    private static Condition[] convertRubyArrayToConditionArray(RubyArray params) {
        Condition[] conditions = new Condition[params.getLength()];
        for (int i = 0; i < params.getLength(); i++) {
            IRubyObject obj = params.entry(i);
            Condition converted_result = (Condition) (JavaUtil.convertRubyToJava(obj));
            conditions[i] = converted_result;
        }

        return conditions;
    }

    /**
     * Convert javaScript array into java array
     *
     * @param nativeArray the javaScript array to convert
     */
    private static Condition[] convertNativeArrayToConditionArray(Object natArray) {
        try {
            //kind of awful code due to different package name between script API in J5 and J6
            Method get = nativeArray.getMethod("get", int.class, scriptable);
            Method unwrap = nativeJavaObject.getMethod("unwrap");
            int l = ((Long) nativeArray.getMethod("getLength").invoke(natArray)).intValue();
            Condition[] conditions = new Condition[l];
            for (int i = 0; i < l; i++) {
                conditions[i] = (Condition) unwrap.invoke(get.invoke(natArray, i, natArray));
            }
            return conditions;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert python array into java array
     *
     * @param pyList the python array to convert
     */
    private static Condition[] convertPyListToConditionArray(PyList pyList) {
        Condition[] conditions = new Condition[pyList.__len__()];

        for (int i = 0; i < pyList.__len__(); i++) {
            conditions[i] = (Condition) pyList.get(i);
        }

        return conditions;
    }
}