/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.scheduler.util.classloading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.jar.JarFile;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.util.JarUtils;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * This class defines a classserver based on ProActive remote objects. It creates classpath files in
 * the scheduler temporary directory (see pa.scheduler.classserver.tmpdir property), and serves classes
 * contained in these files.
 * @author The ProActive team 
 * @since ProActive Scheduling 0.9
 */
public class TaskClassServer {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CORE);

    // temp directory for unjaring classpath : if not defined, java.io.tmpdir is used.
    private static final String tmpTmpJarFilesDir = PASchedulerProperties.SCHEDULER_CLASSSERVER_TMPDIR
            .getValueAsString();
    private static final String tmpJarFilesDir = tmpTmpJarFilesDir != null ? tmpTmpJarFilesDir +
        (tmpTmpJarFilesDir.endsWith(File.separator) ? "" : File.separator) : System
            .getProperty("java.io.tmpdir") +
        File.separator;

    // indicate if cache should be used
    private static final boolean useCache = PASchedulerProperties.SCHEDULER_CLASSSERVER_USECACHE
            .getValueAsBoolean();

    // cache for byte[] classes
    private Hashtable<String, byte[]> cachedClasses;

    // root classpath (directory *or* jar file)
    private File classpath;

    // jobid of the served job
    private JobId servedJobId;

    /**
     * Empty constructor for remote object creation.
     */
    public TaskClassServer() {
    }

    /**
     * Create a new class server. 
     * 
     * @param jid the jobId of the job that will be served by this class server
     */
    public TaskClassServer(JobId jid) {
        this.servedJobId = jid;
        this.cachedClasses = useCache ? new Hashtable<String, byte[]>() : null;
    }

    /**
     * Activate this TaskClassServer. The activation creates all needed files (jar file, classes directory and crc file)
     * in the defined temporary directory (see pa.scheduler.classserver.tmpdir property).
     * @param userClasspathJarFile the content of the classpath
     * @param deflateJar true if the classpath contains jar file, false otherwise
     * @throws IOException if the files cannot be created
     */
    public void activate(byte[] userClasspathJarFile, boolean deflateJar) throws IOException {
        // check if the classpath exists already in the deflated classpathes
        // for now, only in case of recovery
        // TODO cdelbe : look for cp only with crc to avoid mutliple tcs for the same classpath

        // open files 
        File jarFile = new File(this.getPathToJarFile());
        File dirClasspath = new File(this.getPathToClassDir());
        File crcFile = new File(this.getPathToCrcFile());

        boolean classpathAlreadyExists = jarFile.exists() || (deflateJar && dirClasspath.exists());
        boolean reuseExistingFiles = false;

        // check if an already classpath can be reused
        if (classpathAlreadyExists) {
            try {
                // the classpath for this job has already been deflated
                reuseExistingFiles = true;
                // check crc ...
                if (crcFile.exists()) {
                    logger_dev.debug("Classpath files for job " + servedJobId +
                        " already exists... Checking files");
                    BufferedReader crcReader = new BufferedReader(new FileReader(crcFile));
                    String read = crcReader.readLine();
                    CRC32 actualCrc = new CRC32();
                    actualCrc.update(userClasspathJarFile);
                    if (Long.parseLong(read) != actualCrc.getValue()) {
                        // the classpath cannot be reused
                        reuseExistingFiles = false;
                    }
                } else {
                    // no crc : cancel
                    reuseExistingFiles = false;
                }
                // check deflated cp if any
                if (deflateJar && !dirClasspath.exists()) {
                    reuseExistingFiles = false;
                }
            } catch (Exception e) {
                logger_dev.warn("", e);
                // if any exception occurs, cancel 
                reuseExistingFiles = false;
            }
        }

        // delete old classpath if it cannot be reused
        if (classpathAlreadyExists && !reuseExistingFiles) {
            logger_dev.debug("Deleting classpath files for job " + servedJobId);
            // delete classpath files
            jarFile.delete();
            TaskClassUtils.deleteDirectory(dirClasspath);
            crcFile.delete();
        }

        // if no files can be reused, create new ones.
        if (!reuseExistingFiles) {
            logger_dev.debug("Creating classpath files for job " + servedJobId);
            // create jar file
            FileOutputStream fos = new FileOutputStream(jarFile);
            fos.write(userClasspathJarFile);
            fos.flush();
            fos.close();

            //create tmp directory for delfating classpath
            if (deflateJar) {
                dirClasspath.mkdir();
                JarUtils.unjar(new JarFile(jarFile), dirClasspath);
            }

            // create crc file
            FileWriter fosCrc = new FileWriter(crcFile);
            CRC32 crc = new CRC32();
            crc.update(userClasspathJarFile);
            fosCrc.write("" + crc.getValue());
            fosCrc.flush();
            fosCrc.close();
        }

        // set the actual classpath
        this.classpath = deflateJar ? dirClasspath : jarFile;
        logger_dev.info("Activated TaskClassServer for " + (deflateJar ? "deflated" : "") + " classpath " +
            this.classpath.getAbsolutePath() + " for job " + this.servedJobId);
    }

    /**
     * Desactivate this TaskClassServer. The classpath files are deleted, and the
     * classfiles cache is cleared.
     */
    public void desactivate() {
        logger_dev.info("Desactivated TaskClassServer for classpath " + this.classpath.getAbsolutePath() +
            " for job " + this.servedJobId);
        // delete classpath files
        File jarFile = new File(this.getPathToJarFile());
        File deflatedJarFile = new File(this.getPathToClassDir());
        File crcFile = new File(this.getPathToCrcFile());
        jarFile.delete();
        TaskClassUtils.deleteDirectory(deflatedJarFile);
        crcFile.delete();
        // delete cache
        if (this.cachedClasses != null) {
            this.cachedClasses.clear();
        }
    }

    /**
     * Return the byte[] representation of the classfile for the class classname.
     * @param classname the name of the looked up class
     * @return the byte[] representation of the classfile for the class classname.
     * @throws ClassNotFoundException if the class classname cannot be found
     */
    public byte[] getClassBytes(String classname) throws ClassNotFoundException {
        logger_dev.debug("Looking for class " + classname);
        byte[] cb = useCache ? this.cachedClasses.get(classname) : null;
        if (cb == null) {
            logger_dev.debug("Class " + classname + " is not available in class cache");
            try {
                cb = this.classpath.isFile() ? TaskClassUtils.lookIntoJarFile(classname, new JarFile(
                    classpath)) : TaskClassUtils.lookIntoDirectory(classname, classpath);
                if (useCache) {
                    logger_dev.debug("Class " + classname + " is added in class cache");
                    this.cachedClasses.put(classname, cb);
                }
            } catch (IOException e) {
                logger_dev.error("", e);
                throw new ClassNotFoundException("Class " + classname + " has not been found in " +
                    classpath.getAbsolutePath() + ". Caused by " + e);
            }
        }
        // TODO cdelbe : return null or throw an exception. Should return null only...
        logger_dev.debug("Class " + classname + " has " + (cb == null ? "not" : "") + " been found in " +
            classpath.getAbsolutePath());
        return cb;
    }

    /**
     * Return the path to the associated jar file
     * @return the path to the associated jar file
     */
    private String getPathToJarFile() {
        return tmpJarFilesDir + servedJobId.toString() + ".jar";
    }

    /**
     * Return the path to the associated crc file
     * @return the path to the associated crc file
     */
    private String getPathToCrcFile() {
        return tmpJarFilesDir + servedJobId.toString() + ".crc";
    }

    /**
     * Return the path to the associated classfiles directory
     * @return the path to the associated classfiles directory
     */
    private String getPathToClassDir() {
        return tmpJarFilesDir + servedJobId.toString();
    }

}
