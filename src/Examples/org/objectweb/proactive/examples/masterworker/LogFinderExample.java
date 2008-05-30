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
 */
package org.objectweb.proactive.examples.masterworker;

import org.apache.commons.cli.HelpFormatter;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskAlreadySubmittedException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extensions.masterworker.tasks.NativeTask;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * This test class is an example on how to use the Master/Worker API to log a set of log files contained inside a directory
 * A first task will list the content of the directory, filter log files names, and sort the files by decreasing size.
 * Finally the grep tasks will copy the content of each file locally to each worker then execute the grep command on it.
 * This is not intended to be a real-life log analyser as file copying is very expensive, but by this we simulate the idea of a replicated file system, which is mandatory for efficient log file analysis.
 * @author The ProActive Team
 *
 */
public class LogFinderExample extends AbstractExample {

    static ProActiveMaster<? extends NativeTask, ArrayList<String>> master;

    public static final String LOG_PATH = "/net/servers/logs/www-sop/";
    public static final String LOCAL_PATH = "/tmp/";
    private static final String[] PATTERNS = { "ProActive", "Oasis", "Caromel", "Viale" };

    /**
     * @param args
     * @throws TaskAlreadySubmittedException
     * @throws TaskException
     * @throws ProActiveException
     */
    public static void main(String[] args) throws MalformedURLException, TaskAlreadySubmittedException,
            TaskException, ProActiveException {
        //   Getting command line parameters and creating the master (see AbstractExample)
        init(args);

        if (master_vn_name == null) {
            master = new ProActiveMaster();
        } else {
            master = new ProActiveMaster(descriptor_url, master_vn_name);
        }

        registerShutdownHook(new Runnable() {
            public void run() {
                master.terminate(true);
            }
        });

        // Adding ressources
        if (vn_name == null) {
            master.addResources(descriptor_url);
        } else {
            master.addResources(descriptor_url, vn_name);
        }

        // Submitting the listing task
        List tasks = new ArrayList();
        tasks.add(new ListLogFiles());
        master.solve(tasks);

        tasks.clear();

        ArrayList<String> listFiles = master.waitOneResult();
        for (String fileName : listFiles) {
            tasks.add(new GrepCountNativeTask(fileName));
        }
        master.solve(tasks);

        int[] totalCount = new int[PATTERNS.length];
        for (int i = 0; i < totalCount.length; i++) {
            totalCount[i] = 0;
        }

        // Collecting the results
        try {
            while (!master.isEmpty()) {
                ArrayList<String> lines = master.waitOneResult();
                if (lines.size() > 0) {
                    for (int i = 0; i < totalCount.length; i++) {
                        int count = Integer.parseInt(lines.get(i));
                        totalCount[i] += count;
                        System.out.println("Found " + totalCount[i] + " occurences of \"" + PATTERNS[i] +
                            "\" so far.");
                    }
                }

            }
        } catch (TaskException e) {
            // We catch user exceptions
            e.printStackTrace();
        }
        for (int i = 0; i < totalCount.length; i++) {
            System.out.println("Found a total of " + totalCount[i] + " occurences of \"" + PATTERNS[i] +
                "\" in server log.");
        }

        System.exit(0);
    }

    /**
     * Initializing the example with command line arguments
     *
     * @param args command line arguments
     * @throws MalformedURLException
     */
    protected static void init(String[] args) throws MalformedURLException {

        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("NativeExample", command_options);
        AbstractExample.init(args);
    }

    /**
     * A task executing a grep command on a file
     *
     * @author fviale
     */
    public static class GrepCountNativeTask extends NativeTask {

        private static final String GREP_COMMAND = "grep -c -i";
        private static final String COPY_COMMAND = "cp";
        private String fileName;
        private boolean runCopy;

        /**
         *  Creating the task with the given filename
         */
        public GrepCountNativeTask(String fileName) {
            super("");
            if (!new File(LOCAL_PATH + fileName).exists()) {
                super.setCommand(COPY_COMMAND + " " + LOG_PATH + fileName + " " + LOCAL_PATH, null, null);
                runCopy = true;
            } else {
                runCopy = false;
            }
            this.fileName = fileName;
        }

        /** {@inheritDoc} */
        public ArrayList<String> run(WorkerMemory memory) throws IOException, URISyntaxException {
            // Run Copy command
            if (runCopy) {
                super.run(memory);
            }
            // Run Grep commands
            ArrayList<String> answer = new ArrayList<String>();
            for (int i = 0; i < PATTERNS.length; i++) {
                String newCommand = GREP_COMMAND + " " + PATTERNS[i] + " " + LOCAL_PATH + fileName;
                //System.out.println(newCommand);
                setCommand(newCommand, null, null);
                ArrayList<String> partAnswer = super.run(memory);
                if (partAnswer.size() > 0) {
                    answer.add(partAnswer.get(0));
                } else {
                    System.out.println("WARNING NO RESULT");
                }
            }
            return answer;
        }
    }

    /**
     * Task which lists the log files in the specified folder 
     */
    public static class ListLogFiles extends NativeTask {

        private static final String LIST_COMMAND = "ls " + LOG_PATH;

        /** Constructs a new ListLogFiles. */
        public ListLogFiles() {
            super(LIST_COMMAND);
        }

        /** {@inheritDoc} */
        public ArrayList<String> run(WorkerMemory memory) throws IOException, URISyntaxException {
            ArrayList<String> all_files = super.run(memory);
            ArrayList<String> all_logs = new ArrayList<String>();
            for (String line : all_files) {
                if (line.indexOf("_log") > -1) {
                    all_logs.add(line);
                    System.out.println(line);
                }
            }
            return decreasingSizeFileList(all_logs);
        }

        /**
         * Sorts the files in decending size order
         * @param input list of pathnames
         * @return sorted list of pathnames
         */
        private ArrayList<String> decreasingSizeFileList(ArrayList<String> input) {
            ArrayList<String> answer = new ArrayList<String>();

            SortedSet<File> files = new TreeSet<File>(new Comparator<File>() {

                public int compare(File o1, File o2) {
                    long diff = o1.length() - o2.length();
                    if (diff == 0)
                        return 0;
                    else if (diff > 0)
                        return 1;
                    else
                        return -1;

                }
            });
            File parent = new File(LOG_PATH);
            for (String fileName : input) {
                files.add(new File(parent, fileName));
            }
            for (File file : files) {
                answer.add(file.getName());
            }
            return answer;

        }

    }

}
