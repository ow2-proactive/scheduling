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
package org.ow2.proactive.resourcemanager.updater;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 *
 * A wrapper class to use with agents.
 * It either detects that node.jar must be updated, then downloads it start a node.
 * When an agent detects it and restart the node it just proxies the request to RMNodeStarer.
 *
 */
public class RMNodeUpdater extends RMNodeStarter {

    /**
     * Url used to download node.jar
     */
    private static final String NODE_URL_PROPERTY = "node.jar.url";

    static final String OPTION_NODE_URL = "nju";

    public static final String OPTION_NODE_JAR_URL_NAME = "nodeJarUrl";

    public static final String OPTION_NODE_JAR_SAVE_AS_NAME = "nodeJarSaveAs";

    protected String nodeJarUrl;

    /**
     * Local path where the node jar should be stored
     */
    private static final String NODE_JAR_SAVEAS_PROPERTY = "node.jar.saveas";

    static final String OPTION_NODE_SAVEAS = "njs";

    protected String nodeJarSaveAs;

    /**
     * If true, then the updater never terminates, it will
     */
    private static final String NODE_UPDATER_AUTOMATIC_RELAUNCH = "node.updater.automatic.relaunch";

    static final String OPTION_NODE_AUTOMATIC = "nja";

    protected boolean automaticRelaunch = false;

    /**
     * optional One-Jar property, path used to expand librairies
     */
    private static final String ONEJAR_EXPAND_DIR_PROPERTY = "one-jar.expand.dir";

    /**
     * Java Property prefix used to enter extra java command line options
     * For example -Xmn256m can be configured by using -DXtraOption1=Xmn256m
     */
    public static final String XTRA_OPTION = "XtraOption";

    public static final String LAST_DOT_AND_AFTER = "\\.[^\\.]*$";

    private static final Logger logger = initLogger();

    public static final int NUMBER_OF_ATTEMPTS = 10;

    private enum JarStatus {
        UP_TO_DATE,
        OUT_DATED,
        NOT_READY
    }

    public RMNodeUpdater() {
        // empty constructor
    }

    private static Logger initLogger() {
        if (System.getProperty("log4j.configuration") == null) {
            // While logger is not configured and it not set with sys properties, use Console logger
            Logger.getRootLogger().getLoggerRepository().resetConfiguration();
            BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%m%n")));
            Logger.getRootLogger().setLevel(Level.INFO);
        }
        return Logger.getLogger(RMNodeUpdater.class);
    }

    private JarStatus isLocalJarUpToDate(String url, String filePath) {

        try {
            URLConnection urlConnection = new URL(url).openConnection();
            File file = new File(filePath);

            logger.info("Url date=" + new Date(urlConnection.getLastModified()));
            logger.info("File date=" + new Date(file.lastModified()));

            if (!file.exists() ||
                (urlConnection.getLastModified() != 0 && file.lastModified() < urlConnection.getLastModified())) {
                logger.info("Local jar " + file + " is obsolete or not present");
                return JarStatus.OUT_DATED;
            } else if (urlConnection.getLastModified() == 0) {
                logger.info("Server url: " + url + " is not ready");
                return JarStatus.NOT_READY;
            } else {
                logger.info("Local jar " + file + " is up to date");
                return JarStatus.UP_TO_DATE;
            }

        } catch (IOException e) {
            logError("Error when contacting the remote url " + url, e);
        }

        return JarStatus.NOT_READY;
    }

    private static void logError(String message, Exception e) {
        if (logger.isDebugEnabled()) {
            logger.error(message, e);
        } else {
            logger.error(message + " : " + e.getMessage());
        }
    }

    private long getRemoteLastModifiedMillis(String url) {
        try {
            URLConnection urlConnection = new URL(url).openConnection();
            return urlConnection.getLastModified();
        } catch (IOException e) {
            logError("Error when contacting the remote url " + url, e);
            return 0;
        }
    }

    /**
     * Check if the node jar is up-to-date and download it
     * @return true if the local jar is up-to-date or has been successfully renewed. false in case of error
     */
    @SuppressWarnings({ "squid:squid:S2095" })
    private boolean makeNodeUpToDate() {
        switch (isLocalJarUpToDate(nodeJarUrl, nodeJarSaveAs)) {
            case OUT_DATED:
                logger.info("Downloading node.jar from " + nodeJarUrl + " to " + nodeJarSaveAs);
                File destination = new File(nodeJarSaveAs);
                destination.mkdirs();
                File lockFile = null;
                FileLock lock = null;
                FileChannel channel = null;
                try {
                    if (destination.exists()) {

                        lockFile = new File(StandardSystemProperty.JAVA_IO_TMPDIR.value(), "lock");
                        if (!lockFile.exists()) {
                            lockFile.createNewFile();
                        }

                        logger.info("Getting the lock on " + lockFile.getAbsoluteFile());
                        channel = new RandomAccessFile(lockFile, "rw").getChannel();
                        lock = channel.lock();

                        if (isLocalJarUpToDate(nodeJarUrl, nodeJarSaveAs) == JarStatus.UP_TO_DATE) {
                            logger.warn("Another process downloaded node.jar");
                            return false;
                        }
                    }
                    fetchUrl(nodeJarUrl, destination);
                    // Align the local file modification time with the remote url.
                    destination.setLastModified(getRemoteLastModifiedMillis(nodeJarUrl));
                    logger.info("Download finished");

                    cleanExpandDirectory(nodeJarSaveAs);
                    return true;

                } catch (Exception e) {
                    logError("Cannot download node.jar from " + nodeJarUrl, e);
                    return false;
                } finally {
                    releaseResources(lock, channel, lockFile);
                }
            case UP_TO_DATE:
                return true;
            default:
                // server is not ready
                return false;

        }
    }

    private void releaseResources(FileLock lock, FileChannel channel, File lockFile) {
        if (lock != null && lockFile != null && channel != null) {
            logger.info("Releasing the lock on " + lockFile.getAbsoluteFile());
            try {
                lock.release();
                channel.close();
            } catch (IOException e) {
                logError("Error when closing ressources ", e);
            }
        }
    }

    /**
     * Fetch the node.jar at the given url and store it to a file
     * Disable ssl handshake if https
     * @param jarUrl url to fetch
     * @param destination local file
     * @throws IOException
     */
    private static void fetchUrl(String jarUrl, File destination) throws IOException {
        if (jarUrl.startsWith("https")) {
            trustEveryone();

        }
        FileUtils.copyURLToFile(new URL(jarUrl), destination);
    }

    /**
     * Clean the directory used by One-Jar to expand libraries. After a node.jar update, we clean this directory to prevent jar conflicts
     * @param jarFilePath path to the node jar file
     */
    private void cleanExpandDirectory(String jarFilePath) {
        File directoryToClean;
        File jarFile = new File(jarFilePath);
        String oneJarExpandDir = System.getProperty(ONEJAR_EXPAND_DIR_PROPERTY);
        if (oneJarExpandDir == null) {
            // Default scheme used by one-jar
            String jar = jarFile.getName().replaceFirst(LAST_DOT_AND_AFTER, "");
            directoryToClean = new File(StandardSystemProperty.JAVA_IO_TMPDIR.value(), jar);
        } else {
            directoryToClean = new File(oneJarExpandDir);
        }

        boolean jarFileInsideExpandDirectory = isJarFileInsideExpandDirectory(jarFile, directoryToClean);
        try {
            if (jarFileInsideExpandDirectory) {
                FileUtils.moveFileToDirectory(jarFile, directoryToClean.getParentFile(), false);
            }

            FileUtils.deleteQuietly(directoryToClean);

            if (jarFileInsideExpandDirectory) {
                FileUtils.moveFileToDirectory(new File(directoryToClean.getParentFile(), jarFile.getName()),
                                              directoryToClean,
                                              true);
            }
        } catch (IOException e) {
            logger.fatal("Error when moving node jar file to parent directory, check parent folder permissions, aborting...",
                         e);
            System.exit(ExitStatus.FAILED_TO_LAUNCH.exitCode);
        }
    }

    private boolean isJarFileInsideExpandDirectory(File jarFile, File expandDirectory) {
        return jarFile.getParentFile().getAbsoluteFile().equals(expandDirectory.getAbsoluteFile());
    }

    @Override
    protected void fillOptions(final Options options) {
        super.fillOptions(options);

        // The url used to download node.jar
        final Option nodeJarUrlOption = new Option(OPTION_NODE_URL,
                                                   OPTION_NODE_JAR_URL_NAME,
                                                   true,
                                                   "url used to download the node.jar, e.g. http://localhost:8080/rest/node.jar");
        nodeJarUrlOption.setRequired(false);
        nodeJarUrlOption.setArgName("url");
        options.addOption(nodeJarUrlOption);

        // The location where to store the local node.jar
        final Option nodeJarSaveAsOption = new Option(OPTION_NODE_SAVEAS,
                                                      OPTION_NODE_JAR_SAVE_AS_NAME,
                                                      true,
                                                      "local path where to store the downloaded node.jar");
        nodeJarSaveAsOption.setRequired(false);
        nodeJarSaveAsOption.setArgName("path");
        options.addOption(nodeJarSaveAsOption);

        // Node updater in automatic relaunch mode
        final Option nodeUpdaterAutomaticRelaunchOption = new Option(OPTION_NODE_AUTOMATIC,
                                                                     "nodeUpdaterAutomaticRelaunch",
                                                                     false,
                                                                     "If set, the Node Updater will automatically relaunch when the subprocess terminates (always available)");
        nodeUpdaterAutomaticRelaunchOption.setRequired(false);
        options.addOption(nodeUpdaterAutomaticRelaunchOption);

    }

    @Override
    protected String fillParameters(final CommandLine cl, final Options options) {
        String parentResult = super.fillParameters(cl, options);

        if (cl.hasOption(OPTION_NODE_URL)) {
            nodeJarUrl = cl.getOptionValue(OPTION_NODE_URL);
        } else if (System.getProperty(NODE_URL_PROPERTY) != null) {
            nodeJarUrl = System.getProperty(NODE_URL_PROPERTY);
        } else {
            logger.error("Option " + OPTION_NODE_JAR_URL_NAME + " must be specified or java property " +
                         NODE_URL_PROPERTY + " must be set");
            System.exit(ExitStatus.RMNODE_PARSE_ERROR.exitCode);
        }

        if (cl.hasOption(OPTION_NODE_SAVEAS)) {
            nodeJarSaveAs = cl.getOptionValue(OPTION_NODE_SAVEAS);
        } else if (System.getProperty(NODE_JAR_SAVEAS_PROPERTY) != null) {
            nodeJarSaveAs = System.getProperty(NODE_JAR_SAVEAS_PROPERTY);
        } else {
            logger.error("Option " + OPTION_NODE_JAR_SAVE_AS_NAME + " must be specified or java property " +
                         NODE_JAR_SAVEAS_PROPERTY + " must be set");
            System.exit(ExitStatus.RMNODE_PARSE_ERROR.exitCode);
        }

        if (cl.hasOption(OPTION_NODE_AUTOMATIC)) {
            automaticRelaunch = true;
        } else if (System.getProperty(NODE_UPDATER_AUTOMATIC_RELAUNCH) != null) {
            automaticRelaunch = "true".equals(System.getProperty(NODE_UPDATER_AUTOMATIC_RELAUNCH));
        }
        return parentResult;
    }

    @Override
    protected String parseCommandLine(String[] args) {
        final Options options = new Options();

        fillOptions(options);

        final CommandLineParser parser = new DefaultParser();

        CommandLine cl;
        try {
            cl = parser.parse(options, args);
            //now we update this object's fields given the options.
            String nodeName = fillParameters(cl, options);
            //check the user supplied values
            //performed after fillParameters to be able to override fillParameters in subclasses
            checkUserSuppliedParameters();
            return nodeName;
        } catch (ParseException pe) {
            logger.error("Error when parsing arguments", pe);
            System.exit(ExitStatus.RMNODE_PARSE_ERROR.exitCode);
        }

        return null;
    }

    public static void main(String[] args) {

        RMNodeUpdater rmNodeUpdater = new RMNodeUpdater();

        rmNodeUpdater.parseCommandLine(args);

        rmNodeUpdater.updateNodeAndLaunchJVM(args);
    }

    private void updateNodeAndLaunchJVM(String[] args) {
        do {
            try {
                int attempts = 0;
                while (!makeNodeUpToDate() && attempts < NUMBER_OF_ATTEMPTS) {
                    Thread.sleep(5000);
                    attempts++;
                }
                if (attempts >= NUMBER_OF_ATTEMPTS) {
                    if (!automaticRelaunch) {
                        throw new IllegalStateException("Could not make node up to date after " + NUMBER_OF_ATTEMPTS +
                                                        "attempts, aborting");
                    } else {
                        // in case of auto relaunch, we will keep trying
                        continue;
                    }
                }

                logger.info("Launching a computing node");
                ProcessBuilder pb = generateSubProcess(args, nodeJarSaveAs);

                final Process p = pb.start();
                Thread shutdownHook = createShutDownHook(p);
                Runtime.getRuntime().addShutdownHook(shutdownHook);
                p.waitFor();
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (InterruptedException e) {
                logger.warn("", e);
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                logger.error("Error when starting sub process", e);
                Thread.currentThread().interrupt();
            }

        } while (automaticRelaunch && !Thread.currentThread().isInterrupted());
    }

    private static Thread createShutDownHook(final Process p) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    p.destroy();
                } catch (Exception e) {
                    // ignore
                }
            }
        });
    }

    /**
     * Build the java subprocess which will be spawned from this JVM.
     * @param args current JVM arguments
     * @param jarFile up-to-date node jar file
     * @return
     */
    private ProcessBuilder generateSubProcess(String[] args, String jarFile) {
        ProcessBuilder pb;
        List<String> command = new ArrayList<>();
        if (StandardSystemProperty.OS_NAME.value().toLowerCase().contains("windows")) {
            command.add((new File(StandardSystemProperty.JAVA_HOME.value(), "bin/java.exe")).getAbsolutePath());
        } else {
            command.add((new File(StandardSystemProperty.JAVA_HOME.value(), "bin/java")).getAbsolutePath());
        }
        command.addAll(buildJVMOptions());
        command.add("-jar");
        command.add(jarFile);
        command.addAll(removeOptionsUnrecognizedByRMNodeStarter(args));
        logger.info("Starting Java command: " + command);
        pb = new ProcessBuilder(command);
        pb.inheritIO();
        File nodeJarParentFolder = (new File(jarFile)).getParentFile();
        pb.directory(nodeJarParentFolder);
        if (pb.environment().containsKey("CLASSPATH")) {
            pb.environment().remove("CLASSPATH");
        }
        return pb;
    }

    private List<String> removeOptionsUnrecognizedByRMNodeStarter(String[] args) {
        List<String> argList = Lists.newArrayList(args);
        for (Iterator<String> iterator = argList.iterator(); iterator.hasNext();) {
            String arg = iterator.next();
            if (arg.equals("-" + OPTION_NODE_URL) || arg.equals("-" + OPTION_NODE_SAVEAS)) {
                // remove this option + parameter
                iterator.remove();
                iterator.next();
                iterator.remove();
            } else if (arg.equals("-" + OPTION_NODE_AUTOMATIC)) {
                // remove this option
                iterator.remove();
            }
        }
        return argList;
    }

    /**
     * Builds the list of JVM options to use on the forked process.
     * @return list of command line options
     */
    private List<String> buildJVMOptions() {
        ArrayList<String> commandLineProperties = new ArrayList<>();

        Set<String> standardPropertySet = Sets.union(allSystemProperties(), allInternalProperties());

        for (String propertyName : System.getProperties().stringPropertyNames()) {
            if (!standardPropertySet.contains(propertyName)) {
                commandLineProperties.add("-D" + propertyName + "=" + System.getProperty(propertyName));
            }
        }
        commandLineProperties.addAll(allNonStandardXOptionsConvertedToProperties());
        return commandLineProperties;
    }

    /**
     * Returns a set containing all standard java system properties, which will not be forwarded to the new JVM
     * Only java.io.tmpdir and java.library.path will be forwarded.
     * @return set of property names
     */
    private Set<String> allSystemProperties() {
        Set<String> standardPropertySet = new HashSet<>();
        for (StandardSystemProperty stdProperty : StandardSystemProperty.values()) {
            if (stdProperty != StandardSystemProperty.JAVA_IO_TMPDIR &&
                stdProperty != StandardSystemProperty.JAVA_LIBRARY_PATH) {
                // tmp dir and java library path can be overridden by user
                standardPropertySet.add(stdProperty.key());
            }
        }
        return standardPropertySet;
    }

    /**
     * Returns a set containing all internal or sun-proprietary java system properties, which will not be forwarded to the new JVM
     * @return set of property names
     */
    private Set<String> allInternalProperties() {
        Set<String> internalPropertySet = new HashSet<>();
        for (String propertyName : System.getProperties().stringPropertyNames()) {
            if (propertyName.startsWith("sun.") || (propertyName.startsWith(XTRA_OPTION))) {
                internalPropertySet.add(propertyName);
            } else {
                switch (propertyName) {
                    case "file.encoding.pkg":
                    case "user.script":
                    case "user.country":
                    case "java.runtime.version":
                    case "java.awt.graphicsenv":
                    case "java.endorsed.dirs":
                    case "user.variant":
                    case "user.timezone":
                    case "java.runtime.name":
                    case "java.vendor.url.bug":
                    case "java.security.manager":
                    case "java.awt.printerjob":
                    case "awt.toolkit":
                    case "java.vm.info":
                        internalPropertySet.add(propertyName);
                        break;
                    default:
                        // do nothing
                }

            }
        }
        return internalPropertySet;
    }

    /**
     * Builds a list of options which converts java properties starting with XTRA_OPTION prefix to java command line arguments
     * @return a list of command line options
     * @link RMNodeUpdater.XTRA_OPTION
     */
    private List<String> allNonStandardXOptionsConvertedToProperties() {
        List<String> xOptions = new ArrayList<>();
        for (String propertyName : System.getProperties().stringPropertyNames()) {
            if (propertyName.startsWith(XTRA_OPTION)) {
                xOptions.add("-" + System.getProperty(propertyName));
            }
        }
        return xOptions;
    }

    private static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] { new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // always trusted
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // always trusted
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) { // should never happen
            logger.error("Error occurred when modifying the ssl strategy", e);
        }
    }

}
