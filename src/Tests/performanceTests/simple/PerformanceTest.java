/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package performanceTests.simple;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.jdom.Document;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.benchmarks.timit.TimIt;
import org.objectweb.proactive.benchmarks.timit.result.BasicResultWriter;
import org.objectweb.proactive.benchmarks.timit.util.XMLHelper;
import org.objectweb.proactive.benchmarks.timit.util.basic.TimItBasicConfigurator;
import org.objectweb.proactive.benchmarks.timit.util.basic.TimItBasicReductor;
import org.objectweb.proactive.benchmarks.timit.util.charts.BasicComparativeChartBuilder;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;


public class PerformanceTest {
    private ProActiveDescriptor descriptorPad;
    private Node[] nodes;

    private final void initTest() throws Exception {
        // Access the nodes of the descriptor file
        this.descriptorPad = ProDeployment.getProactiveDescriptor(this.getClass()
                                                                  .getResource("/performanceTests/simple/descriptor.xml")
                                                                  .getPath());
        descriptorPad.activateMappings();
        VirtualNode vnode = descriptorPad.getVirtualNodes()[0];
        this.nodes = vnode.getNodes();
    }

    private final void performLocal() throws Exception {
        // First create the callee that will be local to the caller
        Callee callee = (Callee) ProActiveObject.newActive(Callee.class.getName(),
                null, nodes[0]);

        // Then create the caller and provide it a stub on the callee
        Caller caller = (Caller) ProActiveObject.newActive(Caller.class.getName(),
                new Object[] { callee }, nodes[0]);
        caller.performTest();
        // Kill these active objects
        callee.kill();
        caller.kill();
    }

    private final void performRemote() throws Exception {
        // First create the callee that will be remote to the caller
        Callee callee = (Callee) ProActiveObject.newActive(Callee.class.getName(),
                null, nodes[0]);

        // Then create the caller and provide it a stub on the callee
        Caller caller = (Caller) ProActiveObject.newActive(Caller.class.getName(),
                new Object[] { callee }, nodes[1]);
        caller.performTest();
        // Kill these active objects
        callee.kill();
        caller.kill();
    }

    private final void endTest() throws Exception {
        this.descriptorPad.killall(true);
        Thread.sleep(300);
        this.descriptorPad = null;
        TimIt.threadsCleaning();
        // Force gc
        System.gc();
    }

    private final void runTest(int num) {
        try {
            // Set output generation after reduction
            TimItBasicConfigurator.FIRE_STATISTICS_AFTER_REDUCTION = true;
            // A unique id for the output filename
            TimItBasicConfigurator.DEFAULT_OUTPUT_FILENAME_ID = "" + num;
            // Init
            initTest();
            // Customize the output filename suffix for local test
            TimItBasicConfigurator.DEFAULT_OUTPUT_FILENAME_SUFFIX = "_local";
            // First perform local tests
            performLocal();
            // Customize the output filename suffix for remote test
            TimItBasicConfigurator.DEFAULT_OUTPUT_FILENAME_SUFFIX = "_remote";
            // Then perform remote tests
            performRemote();
            // End test
            endTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static void main(String[] args) {
        // By default nbRuns is 5
        int nbRuns = 5;

        // Get the current PA version
        String currentPaVersion = TimItBasicReductor.getShortProActiveVersion();
        String otherPaVersion = null;

        // The number of precedent consecutive versions to compare with
        int nbCompare = 1;

        // Options handling 
        Options opt = new Options();
        opt.addOption("n", "nbRuns", true,
            "Specify the number of runs. Default is 5.");
        opt.addOption("o", "outputDir", true, "Specify the output directory.");
        opt.addOption("p", "printCurrentVersion", true,
            "Prints the current ProActive version.");
        opt.addOption("c", "compareCurrentWith", true,
            "Specify the version to compare with. By default the latest precedent version is used. This option invalidates the x option.");
        opt.addOption("x", "compareAmount", true,
            "Specify the number of precedent consecutive versions to compare with the current. Default is 1 (the latest precedent version).");
        try {
            BasicParser parser = new BasicParser();
            CommandLine cl = parser.parse(opt, args);

            if (cl.hasOption("n") && !"".equals(cl.getOptionValue("n"))) {
                nbRuns = Integer.valueOf(cl.getOptionValue("n"));
            }
            if (cl.hasOption("o") && !"".equals(cl.getOptionValue("o"))) {
                TimItBasicConfigurator.DEFAULT_OUTPUT_DIRECTORY = cl.getOptionValue(
                        "o");
            }
            if (cl.hasOption("p") && !"".equals(cl.getOptionValue("p"))) {
                System.out.println("*** The current ProActive version is : " +
                    currentPaVersion);
            }
            if (cl.hasOption("c") && !"".equals(cl.getOptionValue("c"))) {
                otherPaVersion = cl.getOptionValue("c");
            }
            if (cl.hasOption("x") && !"".equals(cl.getOptionValue("x"))) {
                nbCompare = Integer.valueOf(cl.getOptionValue("x"));
            }
        } catch (Exception e) {
            HelpFormatter help = new HelpFormatter();
            help.printHelp("Performance test options", opt);
            e.printStackTrace();
            System.exit(1);
        }

        PerformanceTest performanceTest = new PerformanceTest();

        for (int i = 0; i < nbRuns; i++) {
            performanceTest.runTest(i);
        }

        // Check if the folder exists
        File folder = new File(TimItBasicConfigurator.DEFAULT_OUTPUT_DIRECTORY);

        if (!folder.isDirectory()) {
            throw new RuntimeException("The ressource : " +
                TimItBasicConfigurator.DEFAULT_OUTPUT_DIRECTORY +
                " is not a directory.");
        }

        // Compute the average from generated files for the current version of ProActive
        File[] averageCurrentVerFiles = generateVersionAverageLocalAndRemote(folder,
                currentPaVersion,
                TimItBasicConfigurator.DEFAULT_OUTPUT_FILENAME_PREFFIX, true);

        System.out.println("*** For the version : " + currentPaVersion +
            " the latest files are :\n" + "\t\t - remote :" +
            averageCurrentVerFiles[0].getAbsolutePath() + "\n" +
            "\t\t - local  :" + averageCurrentVerFiles[1].getAbsolutePath());

        // Precedent version handling        
        File[] otherVerFiles = null;

        // If nbCompare == 1 compare with the latest precedent or specified
        if (nbCompare == 1) {
            if ((otherPaVersion == null) || "".equals(otherPaVersion)) {
                System.out.println("*** Looking for the precedent version of " +
                    currentPaVersion);
                // Try to find the latest precedent version from the current version			
                otherPaVersion = getLatestPrecedentVersion(folder,
                        currentPaVersion,
                        TimItBasicConfigurator.DEFAULT_OUTPUT_FILENAME_PREFFIX,
                        "_remote");
            }

            // If no precedent version was found the comparison is impossible
            if (otherPaVersion == null) {
                System.out.println(
                    "*** No precedent version was found for the current version : " +
                    currentPaVersion + " exiting.");
                System.exit(0);
            }

            otherVerFiles = getLatestFilesForAGivenVersion(folder,
                    otherPaVersion);

            System.out.println("*** The latest precedent version found is " +
                otherPaVersion + ". The latest files are :\n" +
                "\t\t - remote :" + otherVerFiles[0].getAbsolutePath() + "\n" +
                "\t\t - local  :" + otherVerFiles[1].getAbsolutePath());

            // Once we are sure that stats are available
            // generate the comparative chart for remote and local			
            BasicComparativeChartBuilder chart = new BasicComparativeChartBuilder(new File[] {
                        otherVerFiles[0], averageCurrentVerFiles[0]
                    },
                    "Comparison between version " + currentPaVersion + " and " +
                    otherPaVersion + ".",
                    "Caller performs several calls on a remote callee.");
            chart.buildComparativeChart(folder);

            chart = new BasicComparativeChartBuilder(new File[] {
                        otherVerFiles[1], averageCurrentVerFiles[1]
                    },
                    "Comparison between version " + currentPaVersion + " and " +
                    otherPaVersion + ".",
                    "Caller performs several calls on a local callee.");
            chart.buildComparativeChart(folder);
        } else {
            java.util.List<String> precedentVersionList = new java.util.ArrayList<String>();

            // Collect files for these
            java.util.List<File[]> precedentVersionFilesList = new java.util.ArrayList<File[]>();
            String tempVersion = currentPaVersion;
            String otherTempVersion = null;

            // Find precedent versions
            for (int i = 0; i < (nbCompare + 1); i++) {
                System.out.println("*** Looking for the precedent version of " +
                    tempVersion);
                // Try to find the latest precedent version from the tempVersion			
                otherTempVersion = getLatestPrecedentVersion(folder,
                        tempVersion,
                        TimItBasicConfigurator.DEFAULT_OUTPUT_FILENAME_PREFFIX,
                        "_remote");
                // If no precedent version was found exit from the loop
                if (otherTempVersion == null) {
                    System.out.println(
                        "*** No precedent version was found for the version : " +
                        tempVersion + ".");
                    break;
                }
                precedentVersionList.add(otherTempVersion);
                File[] tempFiles = getLatestFilesForAGivenVersion(folder,
                        otherTempVersion);
                System.out.println("*** The latest precedent version found is " +
                    otherTempVersion + ". The latest files are :\n" +
                    "\t\t - remote :" + tempFiles[0].getAbsolutePath() + "\n" +
                    "\t\t - local  :" + tempFiles[1].getAbsolutePath());
                precedentVersionFilesList.add(tempFiles);
                tempVersion = otherTempVersion;
                otherTempVersion = null;
            }

            // Remote stats files
            File[] remoteStatsFiles = new File[precedentVersionList.size() + 1];

            // Local stats files
            File[] localStatsFiles = new File[precedentVersionList.size() + 1];

            // Add current version files
            remoteStatsFiles[0] = averageCurrentVerFiles[0];
            localStatsFiles[0] = averageCurrentVerFiles[1];

            Iterator<File[]> filesIterator = precedentVersionFilesList.iterator();
            int count = 1;
            while (filesIterator.hasNext()) {
                File[] f = filesIterator.next();
                remoteStatsFiles[count] = f[0];
                localStatsFiles[count] = f[1];
                count++;
            }

            // Once we are sure that stats are available
            // generate the comparative chart for remote and local			
            BasicComparativeChartBuilder chart = new BasicComparativeChartBuilder(remoteStatsFiles,
                    "Comparison between version " + currentPaVersion + " and " +
                    precedentVersionList + ".",
                    "Caller performs several calls on a remote callee.");
            chart.buildComparativeChart(folder);

            chart = new BasicComparativeChartBuilder(localStatsFiles,
                    "Comparison between version " + currentPaVersion + " and " +
                    precedentVersionList + ".",
                    "Caller performs several calls on a local callee.");
            chart.buildComparativeChart(folder);
        }

        System.out.println(
            "*** The comparative charts has been generated in the folowing directory : " +
            folder);

        // Exit
        System.exit(0);
    }

    /**
     * [0] : remote
     * [1] : local
     * @param folder The directory where files are
     * @param version The version of files to be found
     * @return An array of 2 files
     */
    public final static File[] getLatestFilesForAGivenVersion(File folder,
        String version) {
        File[] res = new File[2];

        // Get the latest modified file with _remote suffix for the current Pa version 			
        File latestRemoteOutputCurrentVersion = getLastModified(folder,
                version,
                TimItBasicConfigurator.DEFAULT_OUTPUT_FILENAME_PREFFIX, "",
                "_remote");

        if ((latestRemoteOutputCurrentVersion == null) ||
                !latestRemoteOutputCurrentVersion.exists()) {
            throw new RuntimeException(
                "Cannot find the latest xxx_remote output file for the version : " +
                version + " in the directory : " + folder.getAbsolutePath());
        }
        res[0] = latestRemoteOutputCurrentVersion;

        // Get the latest modified file with _local suffix from the _remote one
        String[] splittedName = latestRemoteOutputCurrentVersion.getName()
                                                                .split("_");

        File latestLocalOutputCurrentVersion = getLastModified(folder, version,
                TimItBasicConfigurator.DEFAULT_OUTPUT_FILENAME_PREFFIX,
                splittedName[2], "_local");

        if ((latestLocalOutputCurrentVersion == null) ||
                !latestLocalOutputCurrentVersion.exists()) {
            throw new RuntimeException(
                "Cannot find the latest xxx_local output file for the version : " +
                version + " in the directory : " + folder.getAbsolutePath());
        }
        res[1] = latestLocalOutputCurrentVersion;
        return res;
    }

    /**
     * Returns the latest modified file for a given version
     * @param folder The list of files
     * @param version The version of the wanted file
     * @param versionPrefix The prefix of the version
     * @param optionalId The optionalId
     * @param filenameSufix The sufix of the filename
     * @return The latest modified file for a given version
     */
    public final static File getLastModified(File folder, String version,
        String versionPrefix, String optionalId, String filenameSufix) {
        File[] files = folder.listFiles();
        File res = null;
        if ((files == null) || (files.length <= 1)) {
            return res;
        }

        // Search for the latest modified file for the given version		
        long min = Long.MIN_VALUE;
        String filename;
        String currentVersion;
        String currentId;
        for (int i = 0; i < files.length; i++) {
            filename = files[i].getName();
            if (!filename.startsWith(versionPrefix) ||
                    !filename.endsWith(filenameSufix)) {
                continue;
            }

            if (!"".equals(optionalId)) {
                currentId = filename.split("_")[2];
                if (!currentId.equals(optionalId)) {
                    continue;
                }
            }
            currentVersion = filename.split("_")[1];
            if (version.equals(currentVersion) &&
                    (files[i].lastModified() > min)) {
                min = files[i].lastModified();
                res = files[i];
            }
        }
        return res;
    }

    /**
     * Returns the latest precedent version available the
     * @param folder The folder where the search will be done
     * @param version The current version
     * @param versionPrefix The version prefix
     * @param filenameSufix The version sufix
     * @return The latest precedent version vailable in the specified folder
     */
    public final static String getLatestPrecedentVersion(File folder,
        String version, String versionPrefix, String filenameSufix) {
        File[] files = folder.listFiles();
        String res = null;
        if ((files == null) || (files.length <= 1)) {
            return res;
        }
        int versionValue = Integer.valueOf(version);
        int tempVersion = 0;
        String filename;
        int endIndex;
        int currentVersionValue;
        for (int i = 0; i < files.length; i++) {
            filename = files[i].getName();
            if (!filename.startsWith(versionPrefix) ||
                    !filename.endsWith(filenameSufix)) {
                continue;
            }
            currentVersionValue = Integer.valueOf(filename.split("_")[1]);
            if ((currentVersionValue < versionValue) &&
                    (currentVersionValue > tempVersion)) {
                tempVersion = currentVersionValue;
            }
        }
        if (tempVersion > 0) {
            res = "" + tempVersion;
        }
        return res;
    }

    /**
     * [0] : remote
     * [1] : local
     * If average files already exists for the current version they are overwritted.
     *
     * @param files The files to be scanned
     * @param version The version of files to be found
     * @param versionPrefix
     * @param versionSufix
     * @param deleteUnused Delete unused files when average is computed
     * @return An array of 2 files
     */
    public final static File[] generateVersionAverageLocalAndRemote(
        File folder, String version, String versionPrefix, boolean deleteUnused) {
        File[] files = folder.listFiles();

        // First get all files for a given version and create documents		
        java.util.List<Document> listOfRemote = new java.util.ArrayList<Document>();
        java.util.List<Document> listOfLocal = new java.util.ArrayList<Document>();

        String filename;
        String currentVersion = null;
        int endIndex = 0;

        // Get all files
        for (int i = 0; i < files.length; i++) {
            filename = files[i].getName();
            if (!filename.startsWith(versionPrefix) ||
                    (filename.indexOf("AVG") >= 0)) {
                continue;
            }
            currentVersion = filename.split("_")[1];
            if (currentVersion.equals(version)) {
                if (filename.endsWith("_remote")) {
                    listOfRemote.add(XMLHelper.readFile(files[i]));
                    if (deleteUnused) {
                        files[i].deleteOnExit();
                    }
                }
                if (filename.endsWith("_local")) {
                    listOfLocal.add(XMLHelper.readFile(files[i]));
                    if (deleteUnused) {
                        files[i].deleteOnExit();
                    }
                }
            }
        }

        Document remoteAverageDoc = BasicResultWriter.getAverage(listOfRemote);
        File remoteAverageFile = new File(folder,
                versionPrefix + version + "_AVG" + "_remote");
        XMLHelper.writeFile(remoteAverageDoc, remoteAverageFile);

        Document localAverageDoc = BasicResultWriter.getAverage(listOfLocal);
        File localAverageFile = new File(folder,
                versionPrefix + version + "_AVG" + "_local");
        XMLHelper.writeFile(localAverageDoc, localAverageFile);

        return new File[] { remoteAverageFile, localAverageFile };
    }
}
