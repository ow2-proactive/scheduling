package org.ow2.proactive.scheduler.ext.matsci.client;

import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * AODataspaceRegistry
 *
 * @author The ProActive Team
 */
public class AODataspaceRegistry {

    private HashMap<String, FileSystemServerDeployer> dataspacesin = new HashMap<String, FileSystemServerDeployer>();
    private HashMap<String, FileSystemServerDeployer> dataspacesout = new HashMap<String, FileSystemServerDeployer>();

    private String inbasename;
    private String outbasename;

    private boolean debug;

    private PrintStream outDebug;

    public AODataspaceRegistry() {

    }

    public AODataspaceRegistry(String inbasename, String outbasename, String nodename, boolean debug) {
        this.inbasename = inbasename;
        this.outbasename = outbasename;
        this.debug = debug;
        this.outDebug = outDebug;

        String tmpPath = System.getProperty("java.io.tmpdir");
        File logFile = new File(tmpPath, "" + this.getClass().getSimpleName() + ".log");
        if (!logFile.exists()) {

            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            outDebug = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Pair<String, String> createDataSpace(String path) {
        if (debug) {
            outDebug.println("Looking up or creating dataspaces for :" + path);
        }
        File jcurr = new File(path);
        String jpath = null;
        try {
            jpath = jcurr.getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int ji = jpath.hashCode();
        String dirhash = java.lang.Integer.toHexString(ji);
        FileSystemServerDeployer indepl = null;
        FileSystemServerDeployer outdepl = null;
        if (dataspacesin.containsKey(dirhash)) {
            if (debug) {
                outDebug.println("Reusing existing dataspaces");
            }
            indepl = dataspacesin.get(dirhash);
            outdepl = dataspacesout.get(dirhash);

        } else {
            try {
                if (debug) {
                    outDebug.println("Creating new dataspaces");
                }
                indepl = new FileSystemServerDeployer(this.inbasename + "_" + dirhash, jpath, false, true);
                outdepl = new FileSystemServerDeployer(this.outbasename + "_" + dirhash, jpath, false, true);
                dataspacesin.put(dirhash, indepl);
                dataspacesout.put(dirhash, outdepl);
                outDebug.println("Input dataspace created at url : " + indepl.getVFSRootURL());
                outDebug.println("Output dataspace created at url : " + outdepl.getVFSRootURL());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Pair<String, String>(indepl.getVFSRootURL(), outdepl.getVFSRootURL());
    }

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
}
