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
package org.ow2.proactive.scheduler.ext.matsci.middleman;

import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.proactive.scheduler.ext.matsci.client.common.DataspaceRegistry;
import org.ow2.proactive.scheduler.ext.matsci.client.common.data.Pair;
import org.ow2.proactive.scheduler.ext.matsci.client.common.data.UnReifiable;

import java.io.*;
import java.util.HashMap;


/**
 * AODataspaceRegistry
 *
 * @author The ProActive Team
 */
public class AODataspaceRegistry implements DataspaceRegistry {

    /**
     * Input Dataspaces created locally for a specific directory
     */
    private HashMap<String, FileSystemServerDeployer> dataspacesin = new HashMap<String, FileSystemServerDeployer>();

    /**
     * Output Dataspaces created locally for a specific directory
     */
    private HashMap<String, FileSystemServerDeployer> dataspacesout = new HashMap<String, FileSystemServerDeployer>();

    /**
     * Base name of Input Spaces created
     */
    private String inbasename;

    /**
     * Base name of Output Spaces created
     */
    private String outbasename;

    /**
     * file storing logs coming from this class
     */
    private File logFile;

    /**
     * debug mode
     */
    private boolean debug;

    /**
     * print stream to the log file
     */
    private PrintStream outDebug;

    public AODataspaceRegistry() {

    }

    /** {@inheritDoc} */
    public void init(String inbasename, String outbasename, boolean debug) {
        this.inbasename = inbasename;
        this.outbasename = outbasename;
        this.debug = debug;

        if (debug) {
            String tmpPath = System.getProperty("java.io.tmpdir");
            logFile = new File(tmpPath, "" + this.getClass().getSimpleName() + ".log");
            if (!logFile.exists()) {

                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** {@inheritDoc} */
    public UnReifiable<Pair<String, String>> createDataSpace(String path) {
        if (debug) {
            try {
                outDebug = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (debug) {
            System.out.println("Looking up or creating dataspaces for :" + path);
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
                System.out.println("Reusing existing dataspaces");
                outDebug.println("Reusing existing dataspaces");
            }

            indepl = dataspacesin.get(dirhash);
            outdepl = dataspacesout.get(dirhash);

        } else {
            try {
                if (debug) {
                    System.out.println("Creating new dataspaces");
                    outDebug.println("Creating new dataspaces");
                }

                indepl = new FileSystemServerDeployer(this.inbasename + "_" + dirhash, jpath, false, true);
                outdepl = new FileSystemServerDeployer(this.outbasename + "_" + dirhash, jpath, false, true);
                dataspacesin.put(dirhash, indepl);
                dataspacesout.put(dirhash, outdepl);
                if (debug) {
                    System.out.println("Input dataspace created at url : " + indepl.getVFSRootURL());
                    System.out.println("Output dataspace created at url : " + outdepl.getVFSRootURL());
                    outDebug.println("Input dataspace created at url : " + indepl.getVFSRootURL());
                    outDebug.println("Output dataspace created at url : " + outdepl.getVFSRootURL());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (debug) {
            try {
                outDebug.flush();
                outDebug.close();
            } catch (Exception e) {

            }
        }
        if (debug) {
            System.out.flush();
        }
        return new UnReifiable<Pair<String, String>>(new Pair<String, String>(indepl.getVFSRootURL(), outdepl
                .getVFSRootURL()));
    }

}
