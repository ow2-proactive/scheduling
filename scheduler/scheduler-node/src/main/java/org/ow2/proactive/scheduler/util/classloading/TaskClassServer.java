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
package org.ow2.proactive.scheduler.util.classloading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.jar.JarFile;
import java.util.zip.CRC32;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.util.JarUtils;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;


/**
 * This class defines a classserver based on ProActive remote objects. It creates classpath files in
 * the scheduler temporary directory (see pa.scheduler.classserver.tmpdir property), and serves classes
 * contained in these files.
 * @author The ProActive team
 * @since ProActive Scheduling 0.9
 */
public class TaskClassServer {

    public static final Logger logger = Logger.getLogger(TaskClassServer.class);

    // temp directory for unjaring classpath : if not defined, java.io.tmpdir is used.
    private static final String tmpTmpJarFilesDir = PASchedulerProperties.SCHEDULER_CLASSSERVER_TMPDIR
            .getValueAsStringOrNull();
    private static final String tmpJarFilesDir = tmpTmpJarFilesDir != null ? tmpTmpJarFilesDir +
        (tmpTmpJarFilesDir.endsWith(File.separator) ? "" : File.separator) : System
            .getProperty("java.io.tmpdir") + File.separator;

    // indicate if cache should be used
    private static final boolean useCache = PASchedulerProperties.SCHEDULER_CLASSSERVER_USECACHE
            .getValueAsBoolean();

    // cache for byte[] classes
    private final Hashtable<String, byte[]> cachedClasses;

    // jobid of the served job
    private final JobId servedJobId;

    /** Local paths to spaces accepted in job classpath path elements */
    private final DataSpacesFileObject globalSpace, userSpace;

    // root classpath (directory *or* jar file)
    private final ArrayList<File> classpathSources;

    /**
     * Empty constructor for remote object creation.
     */
    public TaskClassServer() {
        this.cachedClasses = null;
        this.servedJobId = null;
        this.globalSpace = null;
        this.userSpace = null;
        this.classpathSources = null;
    }

    /**
     * Create a new class server.
     *
     * @param jid the jobId of the job that will be served by this class server
     */
    public TaskClassServer(JobId jid, DataSpacesFileObject globalSpace, DataSpacesFileObject userSpace) {
        this.servedJobId = jid;
        this.globalSpace = globalSpace;
        this.userSpace = userSpace;
        this.classpathSources = new ArrayList<File>();
        this.cachedClasses = useCache ? new Hashtable<String, byte[]>() : null;
    }

    /**
     * Activate this TaskClassServer. The activation creates all needed files (jar file, classes directory and crc file)
     * in the temporary directory defined by {@link org.ow2.proactive.scheduler.core.properties.PASchedulerProperties.SCHEDULER_CLASSSERVER_TMPDIR} property.
     * @param jobEnvironement the job environment that contains the job classpath
     * @param jobGlobalSpace the globalspace defined in the job, if not null it overrides the default globalspace
     * @param jobUserSpace the userspace defined in the job, if not null it overrides the default userspace
     * @throws IOException if the files cannot be created
     */
    public void activate(JobEnvironment jobEnvironement, String jobGlobalSpace, String jobUserSpace)
            throws IOException {
        // check if the classpath exists already in the deflated classpathes
        // for now, only in case of recovery
        // TODO cdelbe : look for cp only with crc to avoid mutliple tcs for the same classpath

        // Support for dataspaces in job classpath
        String[] pathElements = jobEnvironement.getJobClasspath();
        this.addSourcesRelativeToSpaces(pathElements, jobGlobalSpace, jobUserSpace);

        byte[] userClasspathJarFile = jobEnvironement.clearJobClasspathContent();
        boolean deflateJar = jobEnvironement.containsJarFile();

        // open files
        File jarFile = new File(this.getPathToJarFile());
        File dirClasspath = new File(this.getPathToClassDir());
        File crcFile = new File(this.getPathToCrcFile());

        boolean classpathAlreadyExists = jarFile.exists() || (deflateJar && dirClasspath.exists());
        boolean reuseExistingFiles = false;

        // check if an already defined classpath can be reused
        if (classpathAlreadyExists) {
            try {
                // the classpath for this job has already been deflated
                reuseExistingFiles = true;
                // check crc ...
                if (crcFile.exists()) {
                    logger.debug("Classpath files for job " + servedJobId +
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
                logger.warn("", e);
                // if any exception occurs, cancel
                reuseExistingFiles = false;
            }
        }

        // delete old classpath if it cannot be reused
        if (classpathAlreadyExists && !reuseExistingFiles) {
            logger.debug("Deleting classpath files for job " + servedJobId);
            // delete classpath files
            jarFile.delete();
            TaskClassUtils.deleteDirectory(dirClasspath);
            crcFile.delete();
        }

        // if no files can be reused, create new ones.
        if (!reuseExistingFiles) {
            logger.debug("Creating classpath files for job " + servedJobId);
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
        this.classpathSources.add(deflateJar ? dirClasspath : jarFile);

        logger.info("Activated TaskClassServer for classpath sources " + this.classpathSources + " for job " +
            this.servedJobId);
    }

    /**
     * Desactivate this TaskClassServer. The classpath files are deleted, and the
     * classfiles cache is cleared.
     */
    public void desactivate() {
        logger.info("Desactivated TaskClassServer for classpaths " + this.classpathSources + " for job " +
            this.servedJobId);
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
        logger.debug("Looking for class " + classname);
        byte[] cb = useCache ? this.cachedClasses.get(classname) : null;
        if (cb != null) {
            logger.debug("Class " + classname + " has " + (cb == null ? "not" : "") + " been found in " +
                this.classpathSources);
            return cb;
        }

        logger.debug("Class " + classname + " is not available in class cache");
        try {
            for (File source : this.classpathSources) {
                if (source.isFile()) {
                    cb = TaskClassUtils.lookIntoJarFile(classname, new JarFile(source));
                } else {
                    cb = TaskClassUtils.lookIntoDirectory(classname, source);
                }
                if (cb != null) {
                    if (useCache) {
                        logger.debug("Class " + classname + " is added in class cache");
                        this.cachedClasses.put(classname, cb);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("", e);
            throw new ClassNotFoundException("Class " + classname + " has not been found in " +
                this.classpathSources, e);
        }

        // TODO cdelbe : return null or throw an exception. Should return null only...
        logger.debug("Class " + classname + " has " + (cb == null ? "not" : "") + " been found in " +
            this.classpathSources);
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

    private void addSourcesRelativeToSpaces(String[] pathElements, String jobGlobalSpace, String jobUserSpace) {
        // The globalspace defined by a job will override the default
        File globalSpaceDir = this.overrideIfNeeded(this.globalSpace, jobGlobalSpace);
        // The userspace defined by a job will override the default
        File userSpaceDir = this.overrideIfNeeded(this.userSpace, jobUserSpace);
        // Add sources relative to GLOBALSPACE and USERSPACE
        for (String pathElement : pathElements) {
            this.addToSources(pathElement, userSpaceDir, SchedulerConstants.USERSPACE_NAME);
            this.addToSources(pathElement, globalSpaceDir, SchedulerConstants.GLOBALSPACE_NAME);
        }
    }

    // Overrides a local dataspace with a local path
    private File overrideIfNeeded(DataSpacesFileObject ds, String localPath) {
        if (localPath != null) {
            File dir = new File(localPath);
            if (dir.exists()) {
                return dir;
            }
        }
        String localDsPath = ds.getRealURI().replace("file://", "");
        return new File(localDsPath);
    }

    // Adds to classpath sources '$SPACENAME' based pathElements
    // for example: dest=c:\tmp pathElement=c:\temp\$GLOBALSPACE\lib.jar
    // then c:\tmp\lib.jar will be added
    private void addToSources(String pathElement, File dest, String spacename) {
        // use split() to get string just after the spacename tag
        String[] splitted = pathElement.split("\\$" + spacename);
        if (splitted.length == 2) { // first element is always empty
            File pathElementFile = new File(dest, splitted[1]);
            if (pathElementFile.exists()) {
                this.classpathSources.add(pathElementFile);
            }
        }
    }
}
