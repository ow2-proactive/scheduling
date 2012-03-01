package org.ow2.proactive.scheduler.ext.matsci.client.embedded.util;

import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matsci.client.common.DataspaceRegistry;
import org.ow2.proactive.scheduler.ext.matsci.client.common.MatSciEnvironment;
import org.ow2.proactive.scheduler.ext.matsci.client.common.MatSciJVMProcessInterface;
import org.ow2.proactive.scheduler.ext.matsci.client.common.data.Pair;
import org.ow2.proactive.scheduler.ext.matsci.client.embedded.LoginFrame;

import javax.swing.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * StandardJVMSpawnHelper class used to start a Java Virtual Machine on the local host and deploy RMI interfaces on it.
 *
 * @author The ProActive Team
 */
public class StandardJVMSpawnHelper {

    private static StandardJVMSpawnHelper instance = null;

    private final static String POLICY_OPTION = "-Djava.security.policy=";
    private final static String LOG4J_OPTION = "-Dlog4j.configuration=file:";
    private final static String PA_CONFIGURATION_OPTION = "-Dproactive.configuration=";

    /**
     * Timeout used to deploy the JVM (times 50ms)
     */
    private static int TIMEOUT = 1200;

    /**
     * Default classpath (classpath of the current JVM)
     */
    private final static String DEFAULT_CLASSPATH = convertClasspathToAbsolutePath(System
            .getProperty("java.class.path"));

    /**
     * Default Java executable path (java path of the current JVM)
     */
    private final static String DEFAULT_JAVAPATH = System.getProperty("java.home") + File.separator + "bin" +
        File.separator + "java";

    /**
     * Options for the JVM
     */
    private ArrayList<String> jvmOptions = new ArrayList<String>();

    /**
     * Entries of the classpath
     */
    private String[] cpEntries;

    /**
     * Full classpath
     */
    private String classPath;

    /**
     * Path to ProActive Configuration
     */
    private String proactiveConf;

    /**
     * Path to log4J file
     */
    private String log4JFile;

    /**
     * Path to Java security policy file
     */
    private String policyFile;

    /**
     * Path to Java executable
     */
    private String javaPath;

    /**
     * Name of the Main class
     */
    private String className;

    /**
     * Deployed MatlabEnvironment Interface
     */
    private MatSciEnvironment matlab_environment;
    /**
     * Deployed MatlabEnvironment Interface
     */
    private MatSciEnvironment scilab_environment;

    /**
     * Deployed DataspaceRegistry Interface
     */
    private DataspaceRegistry registry;

    /*
     * Deployed MatSciJVMProcessInterface Interface
     */
    private MatSciJVMProcessInterface jvmitf;

    /**
     * Login Frame
     */
    LoginFrame lf;

    /**
     * RMI port to use
     */
    private int rmi_port = 1099;

    /**
     * Debug mode
     */
    private boolean debug = false;

    /**
     * Command line arguments
     */
    private ArrayList<String> arguments = new ArrayList<String>();

    /**
     * Names of the RMI interfaces
     */
    private ArrayList<String> itfNames = new ArrayList<String>();

    /**
     * Path to tmp dir
     */
    private static String tmpPath = System.getProperty("java.io.tmpdir");

    /**
     * Stream to the Debug file
     */
    private PrintStream outDebug;

    /**
     * Minimum RMI port number
     */
    private static final int MIN_PORT_NUMBER = 1000;

    /**
     * Maximum RMI port number
     */
    private static final int MAX_PORT_NUMBER = 9999;

    private StandardJVMSpawnHelper() {
        itfNames.add("org.ow2.proactive.scheduler.ext.matlab.middleman.AOMatlabEnvironment");
        itfNames.add("org.ow2.proactive.scheduler.ext.scilab.middleman.AOScilabEnvironment");
        itfNames.add(DataspaceRegistry.class.getName());
        itfNames.add(MatSciJVMProcessInterface.class.getName());
    }

    public static StandardJVMSpawnHelper getInstance() {
        if (instance == null) {
            instance = new StandardJVMSpawnHelper();
        }
        return instance;
    }

    public void setDebug(boolean d) {
        this.debug = d;
    }

    public void setRmiPort(int port) {
        this.rmi_port = port;
    }

    public void setTimeout(int timeout) {
        TIMEOUT = timeout;
    }

    public void setJavaPath(String jpath) {
        File test = new File(jpath);
        if (!test.exists() || !test.canExecute()) {
            throw new IllegalArgumentException(jpath + " does not exist or is not readable.");
        }
        this.javaPath = jpath;
    }

    public void setClasspathEntries(String[] entries) {
        StringBuffer absoluteClasspath = new StringBuffer();
        String pathSeparator = File.pathSeparator;
        for (String e : entries) {
            absoluteClasspath.append(new java.io.File(e).getAbsolutePath());
            absoluteClasspath.append(pathSeparator);
        }
        this.cpEntries = entries;
        this.classPath = absoluteClasspath.substring(0, absoluteClasspath.length() - 1);
    }

    public void setProActiveConfiguration(String confpath) {
        File test = new File(confpath);
        if (!test.exists() || !test.canRead()) {
            throw new IllegalArgumentException(confpath + " does not exist or is not readable.");
        }
        this.proactiveConf = confpath;
    }

    public void setLog4JFile(String logpath) {
        File test = new File(logpath);
        if (!test.exists() || !test.canRead()) {
            throw new IllegalArgumentException(logpath + " does not exist or is not readable.");
        }
        this.log4JFile = logpath;
    }

    public void setPolicyFile(String policy) {
        File test = new File(policy);
        if (!test.exists() || !test.canRead()) {
            throw new IllegalArgumentException(policy + " does not exist or is not readable.");
        }
        this.policyFile = policy;
    }

    public void addInterfaceName(String name) {
        this.itfNames.add(name);
    }

    public void addJvmOptions(String[] options) {
        for (String o : options) {
            jvmOptions.add(o);
        }
    }

    public void setClassName(String cn) {
        this.className = cn;
    }

    public void addArgument(String arg) {
        this.arguments.add(arg);
    }

    public MatSciEnvironment getMatlabEnvironment() {
        return matlab_environment;
    }

    public MatSciEnvironment getScilabEnvironment() {
        return scilab_environment;
    }

    public DataspaceRegistry getRegistry() {
        return registry;
    }

    public MatSciJVMProcessInterface getJvmInterface() {
        return jvmitf;
    }

    /**
     * Tests if the given port is available to deploy a rmiregistry
     *
     * @param port
     * @return
     */
    public static boolean available(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    /**
     * Starts a new JVM or lookup an existing one
     *
     * @return a map containing RMI interfaces found (name/stub), and the RMI port used
     * @throws IOException
     */
    public Pair<HashMap<String, Object>, Integer> deployOrLookup() throws Exception {
        try {
            HashMap<String, Object> stubs = new HashMap<String, Object>();
            boolean stubsFound = false;
            boolean av = false;

            try {
                Registry registry = LocateRegistry.getRegistry(rmi_port);

                for (String name : itfNames) {
                    stubs.put(name, registry.lookup(name));
                }
                stubsFound = true;
                this.matlab_environment = (MatSciEnvironment) stubs
                        .get("org.ow2.proactive.scheduler.ext.matlab.middleman.AOMatlabEnvironment");
                this.scilab_environment = (MatSciEnvironment) stubs
                        .get("org.ow2.proactive.scheduler.ext.scilab.middleman.AOScilabEnvironment");
                this.registry = (DataspaceRegistry) stubs.get(DataspaceRegistry.class.getName());
                this.jvmitf = (MatSciJVMProcessInterface) stubs
                        .get(MatSciJVMProcessInterface.class.getName());

            } catch (Exception e) {

            }

            if (stubsFound) {
                return new Pair<HashMap<String, Object>, Integer>(stubs, rmi_port);
            }

            do {
                av = available(rmi_port);
                if (!av) {
                    int new_rmi_port = rmi_port + (int) Math.round(Math.random() * 10);
                    if (new_rmi_port > MAX_PORT_NUMBER) {

                    }
                    System.out.println("Port " + rmi_port + " in use, trying port " + new_rmi_port);
                    rmi_port = new_rmi_port;
                }
            } while (!av);

            ArrayList<String> cmd = new ArrayList<String>();
            if (javaPath != null) {
                cmd.add(javaPath);
            } else {
                cmd.add(DEFAULT_JAVAPATH);
            }

            if (policyFile != null) {
                cmd.add(POLICY_OPTION + policyFile);
            }

            if (log4JFile != null) {
                cmd.add(LOG4J_OPTION + log4JFile);
            }

            if (proactiveConf != null) {
                cmd.add(PA_CONFIGURATION_OPTION + proactiveConf);
            }

            jvmOptions.add("-Drmi.port=" + rmi_port);

            if (jvmOptions.size() > 0) {
                cmd.addAll(jvmOptions);
            }

            if (className == null) {
                throw new IllegalStateException("Missing class name.");
            }

            cmd.add(className);

            for (String arg : arguments) {
                cmd.add(arg);
            }

            File logFile = new File(tmpPath, "" + this.getClass().getSimpleName() + ".log");
            if (!logFile.exists()) {

                logFile.createNewFile();

            }

            outDebug = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true)));

            ProcessBuilder pb = new ProcessBuilder(cmd);
            Map<String, String> env = pb.environment();
            if (classPath != null) {
                env.put("CLASSPATH", classPath);
            } else {
                env.put("CLASSPATH", DEFAULT_CLASSPATH);
            }

            if (debug) {
                System.out.println("Running Java command :");
                System.out.println(cmd);
            }
            Process process = pb.start();

            IOTools.LoggingThread lt1;
            if (debug) {
                lt1 = new IOTools.LoggingThread(process, "[MIDDLEMAN]", System.out, System.err, outDebug);

            } else {
                lt1 = new IOTools.LoggingThread(process);
            }
            Thread t1 = new Thread(lt1, "MIDDLEMAN");
            t1.setDaemon(true);
            t1.start();

            Exception lasterr = null;
            if (itfNames.size() > 0) {
                boolean deployed = false;

                int cpt = 0;
                Registry registry = null;
                while (!deployed && cpt < TIMEOUT) {
                    try {
                        if (registry == null) {
                            registry = LocateRegistry.getRegistry(rmi_port);
                        }
                        for (String name : itfNames) {
                            stubs.put(name, registry.lookup(name));
                        }
                        deployed = true;
                    } catch (Exception e) {
                        lasterr = e;
                        if (debug) {
                            e.printStackTrace(outDebug);
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                            break;
                        }
                        cpt++;
                    }
                }
                if (cpt >= TIMEOUT) {
                    throw new IllegalStateException("Timeout occured when waiting for deployment.", lasterr);
                }
                this.matlab_environment = (MatSciEnvironment) stubs
                        .get("org.ow2.proactive.scheduler.ext.matlab.middleman.AOMatlabEnvironment");
                this.scilab_environment = (MatSciEnvironment) stubs
                        .get("org.ow2.proactive.scheduler.ext.scilab.middleman.AOScilabEnvironment");
                this.registry = (DataspaceRegistry) stubs.get(DataspaceRegistry.class.getName());
                this.jvmitf = (MatSciJVMProcessInterface) stubs
                        .get(MatSciJVMProcessInterface.class.getName());
                return new Pair<HashMap<String, Object>, Integer>(stubs, rmi_port);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * Starts the Login GUI
     */
    public void startLoginGUI() {
        if (scilab_environment == null) {
            throw new IllegalStateException("Environment not initialized");
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                lf = new LoginFrame(scilab_environment, true);
                lf.start();
            }
        });
    }

    /**
     * Returns the number of login attempts
     *
     * @return
     */
    public int getNbAttempts() {
        if (lf != null)
            return lf.getNbAttempts();
        return 0;
    }

    private static String convertClasspathToAbsolutePath(String classpath) {
        StringBuffer absoluteClasspath = new StringBuffer();
        String pathSeparator = File.pathSeparator;
        java.util.StringTokenizer st = new java.util.StringTokenizer(classpath, pathSeparator);
        while (st.hasMoreTokens()) {
            absoluteClasspath.append(new java.io.File(st.nextToken()).getAbsolutePath());
            absoluteClasspath.append(pathSeparator);
        }
        return absoluteClasspath.substring(0, absoluteClasspath.length() - 1);
    }

}
