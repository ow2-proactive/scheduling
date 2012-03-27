package org.ow2.proactive.scheduler.ext.matsci.middleman;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.ow2.proactive.scheduler.ext.common.util.StackTraceUtil;
import org.ow2.proactive.scheduler.ext.matsci.client.common.MatSciEnvironment;
import org.ow2.proactive.scheduler.ext.matsci.client.common.MatSciJVMProcessInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * JVMProcessInterfaceImpl
 *
 * @author The ProActive Team
 */
public class MatSciJVMProcessInterfaceImpl implements InitActive, EndActive, MatSciJVMProcessInterface {

    MatSciEnvironment matlab_env;

    MatSciEnvironment scilab_env;

    MatSciJVMProcessInterfaceImpl stubOnThis;

    private static PrintWriter outDebugWriter;
    private static FileWriter outFile;

    private static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");

    private static final String TMPDIR = System.getProperty("java.io.tmpdir");

    /**
     * host name
     */
    protected static String host = null;

    static {
        if (host == null) {
            try {
                host = java.net.InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public MatSciJVMProcessInterfaceImpl() {

    }

    public MatSciJVMProcessInterfaceImpl(MatSciEnvironment matlab_env, MatSciEnvironment scilab_env) {
        this.scilab_env = scilab_env;
        this.matlab_env = matlab_env;
    }

    /** Creates a log file in the java.io.tmpdir if debug is enabled */
    protected void createLogFileOnDebug() throws Exception {

        final File logFile = new File(this.TMPDIR, this.getClass().getSimpleName() + ".log");
        if (!logFile.exists()) {
            logFile.createNewFile();
        }

        outFile = new FileWriter(logFile, false);
        outDebugWriter = new PrintWriter(outFile);
    }

    private void closeLogFileOnDebug() {
        try {
            outDebugWriter.close();
            outFile.close();
        } catch (Exception e) {

        }
    }

    public static void printLog(Object origin, final Throwable ex) {
        final Date d = new Date();
        final String log = "[" + origin.getClass().getSimpleName() + "] " + StackTraceUtil.getStackTrace(ex);
        System.out.println(log);
        System.out.flush();
        if (outDebugWriter != null) {
            outDebugWriter.println(log);
            outDebugWriter.flush();
        }
    }

    public static void printLog(Object origin, final String message) {
        final Date d = new Date();
        final String log = "[" + origin.getClass().getSimpleName() + "] " + message;
        System.out.println(log);
        System.out.flush();
        if (outDebugWriter != null) {
            outDebugWriter.println(log);
            outDebugWriter.flush();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        stubOnThis = (MatSciJVMProcessInterfaceImpl) PAActiveObject.getStubOnThis();
        try {
            createLogFileOnDebug();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void endActivity(Body body) {
        closeLogFileOnDebug();
        PALifeCycle.exitSuccess();
    }

    /** {@inheritDoc} */
    public Integer getPID() {
        RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
        String processName = rtb.getName();

        Integer result = null;

        /* tested on: */
        /* - windows xp sp 2, java 1.5.0_13 */
        /* - mac os x 10.4.10, java 1.5.0 */
        /* - debian linux, java 1.5.0_13 */
        /* all return pid@host, e.g 2204@antonius */

        Pattern pattern = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(processName);
        if (matcher.matches()) {
            result = new Integer(Integer.parseInt(matcher.group(1)));
        }
        return result;

    }

    /** {@inheritDoc} */
    public boolean shutdown() {
        try {
            matlab_env.disconnect();
            matlab_env.terminate();
        } catch (Throwable e) {
        }
        try {
            scilab_env.disconnect();
            scilab_env.terminate();
        } catch (Throwable e) {
        }
        stubOnThis.destroyJVM();
        return true;
    }

    protected void destroyJVM() {
        closeLogFileOnDebug();
        PAActiveObject.terminateActiveObject(false);
    }

}
