/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.resourcemanager.nodesource.dataspace;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;


/**
 * DataSpaceNodeConfigurationAgent is used to configure and close DataSpaces knowledge
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
public class DataSpaceNodeConfigurationAgent implements Serializable {

    private static final transient Logger logger = Logger.getLogger(DataSpaceNodeConfigurationAgent.class);

    /**
     * This property is used by scheduling when configuring node to define the location of the scratch dir and must be renamed carefully.
     * It is also defined in TaskLauncher.
     */
    public static final String NODE_DATASPACE_SCRATCHDIR = "node.dataspace.scratchdir";

    /**
     * This property is used by scheduling when configuring node to define the location of the cache dir. If the property is not defined,
     * the scratch location will be used to create the cache dir
     */
    public static final String NODE_DATASPACE_CACHEDIR = "node.dataspace.cachedir";

    /**
     * This property contains the invalidation period for a file, in ms. If set with a value greater than 0, each file older than the specified period will be deleted.
     */
    public static final String NODE_DATASPACE_CACHE_INVALIDATION_PERIOD = "node.dataspace.cache.invalidation.period";

    /**
     * This property contains the interval period used to start the cache cleaning process.
     */
    public static final String NODE_DATASPACE_CACHE_CLEANING_PERIOD = "node.dataspace.cache.cleaning.period";

    /**
     * Name of the CacheSpace for DataSpaces registration
     */
    public static final String CACHESPACE_NAME = "CACHESPACE";

    /**
     * Default subfolder name for the cache
     **/
    public static final String DEFAULT_CACHE_SUBFOLDER_NAME = "cache";

    /**
     * Default cache cleaning period (interval at which the cache is cleaned. Default to one hour
     **/
    public static final long DEFAULT_CACHE_CLEANING_PERIOD = 3600000L;

    /**
     * Default cache invalidation period (files older than this date will be deleted). Default to two weeks
     **/
    public static final long DEFAULT_CACHE_INVALIDATION_PERIOD = 1210000000L;

    /**
     * file system server controlling the cache
     */
    private transient FileSystemServerDeployer cacheServer;

    /**
     * Configuration of the cache server (used by the TaskLauncher to register the dataspace to the naming service
     */
    private static transient InputOutputSpaceConfiguration cacheSpaceConfiguration;

    /**
     * VFS direct interface to the cache, used by the cleaning mechanism to delete files
     */
    private static transient DefaultFileSystemManager fileSystemManager;

    /**
     * URL of the cache root folder
     */
    private static String rootCacheUri;

    /**
     * Timer used by the cleaning mechanism
     */
    private static transient Timer cleaningTimer;

    /**
     * Lock used by the cleaning mechanism to ensure that the cleaning is not run concurrently with a Task
     */
    private static transient ReentrantReadWriteLock cacheCleaningRWLock = new ReentrantReadWriteLock();


    /**
     * Create a new instance of DataSpaceNodeConfigurationAgent
     * Used by ProActive
     */
    public DataSpaceNodeConfigurationAgent() {
    }

    public boolean configureNode() {
        try {
            // configure node for Data Spaces
            String baseScratchDir = getBaseScratchDir();
            final BaseScratchSpaceConfiguration scratchConf = new BaseScratchSpaceConfiguration(
                    (String) null, baseScratchDir);
            DataSpacesNodes.configureNode(PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis()),
                    scratchConf);
        } catch (Throwable t) {
            logger.error("Cannot configure dataSpace", t);
            return false;
        }

        startCacheSpace();

        return true;
    }

    private String getBaseScratchDir() {
        String scratchDir;
        if (System.getProperty(NODE_DATASPACE_SCRATCHDIR) == null) {
            //if scratch dir java property is not set, set to default
            scratchDir = System.getProperty("java.io.tmpdir");
        } else {
            //else use the property
            scratchDir = System.getProperty(NODE_DATASPACE_SCRATCHDIR);
        }
        return scratchDir;
    }

    private String getCacheDir() {
        String cacheDir;
        if (System.getProperty(NODE_DATASPACE_CACHEDIR) == null) {
            //if scratch dir java property is not set, set to default
            cacheDir = (new File(getBaseScratchDir(), DEFAULT_CACHE_SUBFOLDER_NAME)).getAbsolutePath();
        } else {
            // else use the property
            cacheDir = System.getProperty(NODE_DATASPACE_CACHEDIR);
        }
        return cacheDir;
    }

    private static long getCacheCleaningPeriod() {
        long cacheCleaningPeriod = DEFAULT_CACHE_CLEANING_PERIOD;
        if (System.getProperty(NODE_DATASPACE_CACHE_CLEANING_PERIOD) != null) {
            cacheCleaningPeriod = Long.parseLong(System.getProperty(NODE_DATASPACE_CACHE_CLEANING_PERIOD));
        }
        return cacheCleaningPeriod;
    }

    private static long getCacheInvalidationPeriod() {
        long cacheInvalidationPeriod = DEFAULT_CACHE_INVALIDATION_PERIOD;
        if (System.getProperty(NODE_DATASPACE_CACHE_INVALIDATION_PERIOD) != null) {
            cacheInvalidationPeriod = Long.parseLong(System.getProperty(NODE_DATASPACE_CACHE_INVALIDATION_PERIOD));
        }
        return cacheInvalidationPeriod;
    }

    /**
     * Returns the configuration of the server cache
     *
     * @return
     */
    public static InputOutputSpaceConfiguration getCacheSpaceConfiguration() {
        return cacheSpaceConfiguration;
    }


    /**
     * Starts the Cache Space server
     *
     * @return
     */
    public boolean startCacheSpace() {
        if (cacheSpaceConfiguration == null) {
            try {
                String cacheLocation = getCacheDir();
                cacheServer = new FileSystemServerDeployer(CACHESPACE_NAME, cacheLocation, true, true);
                logger.info("Cache server started at " + Arrays.toString(cacheServer.getVFSRootURLs()));
                String hostname = InetAddress.getLocalHost().getHostName();
                cacheSpaceConfiguration = InputOutputSpaceConfiguration.createOutputSpaceConfiguration(Arrays.asList(cacheServer.getVFSRootURLs()), cacheLocation, hostname, CACHESPACE_NAME);
                fileSystemManager = VFSFactory.createDefaultFileSystemManager();
                rootCacheUri = new File(cacheLocation).toURI().toURL().toExternalForm();

                if (getCacheInvalidationPeriod() > 0) {
                    startCacheCleaningDaemon();
                }

            } catch (Exception e) {
                logger.error("Error occurred when starting the cache server", e);
                return false;
            }
        }
        return true;
    }

    /**
     * This method must be used by the TaskLauncher to prevent the cleaning timer to run concurrently to a Task
     * As a read lock is used, multiple readers can acquire the lock at the same time
     *
     * @throws InterruptedException
     */
    public static void lockCacheSpaceCleaning() throws InterruptedException {
        cacheCleaningRWLock.readLock().lockInterruptibly();
    }

    /**
     * This method must be used by the TaskLauncher at the end of a Task execution, allowing the cleaning timer to run
     * <p>
     * The cleaning timer will be released only when all read locks have been released.
     *
     * @throws InterruptedException
     */
    public static void unlockCacheSpaceCleaning() {
        cacheCleaningRWLock.readLock().unlock();
    }

    private void startCacheCleaningDaemon() {
        cleaningTimer = new Timer("CacheCleaningTimer", true);
        long cleaningInterval = getCacheCleaningPeriod();
        cleaningTimer.schedule(new CleanTimerTask(), cleaningInterval, cleaningInterval);
    }

    public BooleanWrapper closeNodeConfiguration() {

        try {
            cacheServer.terminate();

            cleaningTimer.cancel();

            DataSpacesNodes.closeNodeConfig(PAActiveObject
                    .getActiveObjectNode(PAActiveObject.getStubOnThis()));
        } catch (Throwable t) {
            logger.error("Cannot close dataSpace configuration !", t);
            throw new RuntimeException(t);
        }
        PAActiveObject.terminateActiveObject(false);
        return new BooleanWrapper(true);
    }

    /**
     * This class contains the logic of the cleaning mechanism
     */
    private static class CleanTimerTask extends TimerTask {

        @Override
        public void run() {
            try {
                long invalidationPeriod = getCacheInvalidationPeriod();
                long currentTime = System.currentTimeMillis();
                // lock the timer in write mode, this will prevent any Task to start during the cleaning process
                cacheCleaningRWLock.writeLock().lockInterruptibly();
                try {
                    FileObject rootFO = fileSystemManager.resolveFile(rootCacheUri);
                    FileObject[] files = rootFO.findFiles(Selectors.EXCLUDE_SELF);
                    for (FileObject file : files) {
                        if (currentTime - file.getContent().getLastModifiedTime() > invalidationPeriod) {
                            logger.info("[Cache Space cleaner] deleting " + file);
                            file.delete();
                        }
                    }
                } finally {
                    cacheCleaningRWLock.writeLock().unlock();
                }
            } catch (Exception e) {
                logger.error("Error when cleaning files in cache", e);
            }
        }
    }

}
