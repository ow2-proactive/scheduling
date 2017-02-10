/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Library;
import com.sun.jna.Native;


/**
 * Environment this class uses JNA to change the System Environment variables from the current JVM
 * <p>
 * It changes both the shallow HashMap copy of the environment held by Java and the real libC environment
 *
 * @author The ProActive Team
 */
public class Environment {

    public static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Environment.class);

    /*
     * // for JNI:
     * public static class LibC {
     * public native int setenv(String name, String value, int overwrite);
     * public native int unsetenv(String name);
     * 
     * LibC() {
     * System.loadLibrary("Environment_LibC");
     * }
     * }
     * static LibC libc = new LibC();
     */

    public interface WinLibC extends Library {
        int _putenv(String name);
    }

    public interface LinuxLibC extends Library {
        int setenv(String name, String value, int overwrite);

        int unsetenv(String name);
    }

    public static class POSIX {

        Object libc = null;

        public POSIX() {
            String osName = System.getProperty("os.name");
            OperatingSystem operatingSystem = OperatingSystem.resolveOrError(osName);
            OperatingSystemFamily family = operatingSystem.getFamily();
            String[] linuxLibcNames = new String[] { "c", "libc.so.6", "libc.so.5", "libc.so.7" };
            libc = null;
            switch (family) {
                case LINUX:
                case UNIX:
                case MAC:
                    for (String libName : linuxLibcNames) {
                        try {
                            libc = Native.loadLibrary(libName, LinuxLibC.class);
                            break;
                        } catch (Throwable e) {
                            logger.debug("[Warning] could not load library '" + libName + "', skipping...", e);
                        }
                    }
                    break;
                case WINDOWS:
                    try {
                        libc = Native.loadLibrary("msvcrt", WinLibC.class);
                    } catch (Throwable e) {
                        logger.debug("[Warning] could not load library 'msvcrt', skipping...", e);
                    }
                    break;
                default:
                    // we don't initialize libc
            }
            if (libc == null) {
                logger.warn("Couldn't load the C library native env modification will be disabled.");
            }
        }

        public int setenv(String name, String value, int overwrite) {
            if (libc != null) {
                if (libc instanceof LinuxLibC) {
                    return ((LinuxLibC) libc).setenv(name, value, overwrite);
                } else {
                    return ((WinLibC) libc)._putenv(name + "=" + value);
                }
            }
            return 0;
        }

        public int unsetenv(String name) {
            if (libc != null) {
                if (libc instanceof LinuxLibC) {
                    return ((LinuxLibC) libc).unsetenv(name);
                } else {
                    return ((WinLibC) libc)._putenv(name + "=");
                }
            }
            return 0;
        }
    }

    private static POSIX posix;

    static POSIX getPOSIXinstance() {
        if (posix == null) {
            posix = new POSIX();
        }
        return posix;
    }

    public static int unsetenv(String name) {
        Map<String, String> map = getenv();
        map.remove(name);
        Map<String, String> env2 = getwinenv();
        env2.remove(name);
        return getPOSIXinstance().unsetenv(name);
    }

    public static int setenv(String name, String value, boolean overwrite) {
        if (name.lastIndexOf("=") != -1) {
            throw new IllegalArgumentException("Environment variable cannot contain '='");
        }
        Map<String, String> map = getenv();
        boolean contains = map.containsKey(name);
        if (!contains || overwrite) {
            map.put(name, value);
            Map<String, String> env2 = getwinenv();
            env2.put(name, value);
        }
        return getPOSIXinstance().setenv(name, value, overwrite ? 1 : 0);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getwinenv() {
        try {
            Class<?> sc = Class.forName("java.lang.ProcessEnvironment");
            Field caseinsensitive = sc.getDeclaredField("theCaseInsensitiveEnvironment");
            caseinsensitive.setAccessible(true);
            return (Map<String, String>) caseinsensitive.get(null);
        } catch (Exception e) {
            logger.debug("Error when reading the JVM System Environment Cache : " + e.getMessage());
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getenv() {
        try {
            Map<String, String> theUnmodifiableEnvironment = System.getenv();
            Class<?> cu = theUnmodifiableEnvironment.getClass();
            Field m = cu.getDeclaredField("m");
            m.setAccessible(true);
            return (Map<String, String>) m.get(theUnmodifiableEnvironment);
        } catch (Exception ex2) {
            logger.debug("Error when reading the JVM System Environment Cache : " + ex2.getMessage());
        }
        return new HashMap<>();
    }

    public static void main(String[] args) throws InterruptedException {
        setenv("TEST", "foo", true);
        String val = getenv().get("TEST");
        System.out.println(val);
        Thread.sleep(10000);
    }

}
