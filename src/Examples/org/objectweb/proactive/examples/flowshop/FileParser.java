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
package org.objectweb.proactive.examples.flowshop;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;


/**
 * Parse a file contains a FlowShop description problem : number of job,
 * number of machine and for each job operation's time.
 */
public class FileParser {

    /**
     * Parse a file contains a FlowShop description problem : number of job,
     * number of machine and for each job operation's time.
     *
     * @param inputFile
     * @return FlowShop
     * @throws IOException
     * @throws BabFileFormatExcpetion
     */
    public static FlowShop parseFile(File inputFile, boolean isTaillard) throws IOException,
            BabFileFormatException {
        Main.logger.info("Start reading file at " + inputFile.getPath());

        StreamTokenizer st = new StreamTokenizer(new FileReader(inputFile));

        int nbJobs = 0;
        int nbMachines = 0;
        int[][] jobs = null;

        st.nextToken();
        // Reading number of jobs
        if (st.ttype == StreamTokenizer.TT_NUMBER) {
            nbJobs = (int) st.nval;
            Main.logger.debug("Number of jobs was read " + nbJobs);
            st.nextToken();
        } else {
            throw new BabFileFormatException("The number of jobs is awaited here: " + st);
        }

        // Reading number of machines
        if (st.ttype == StreamTokenizer.TT_NUMBER) {
            nbMachines = (int) st.nval;
            Main.logger.debug("Number of machines was read " + nbMachines);
            st.nextToken();
        } else {
            throw new BabFileFormatException("The number of machines is awaited here: " + st);
        }

        // Reading all jobs
        jobs = new int[nbJobs][];
        if (!isTaillard) {
            for (int currentJob = 0; currentJob < nbJobs; currentJob++) {
                int[] operations = new int[nbMachines];

                // Reading the 0
                if (!((st.ttype == StreamTokenizer.TT_NUMBER) && (st.nval == 0))) {
                    throw new BabFileFormatException("A 0 is awaited here: " + st);
                }
                st.nextToken();

                // Reading the secret value
                if (!((st.ttype == StreamTokenizer.TT_NUMBER))) {
                    throw new BabFileFormatException("A secret number is awaited here: " + st);
                }
                st.nextToken();

                // Reading all operations
                for (int currentOperation = 0; currentOperation < nbMachines; currentOperation++) {
                    if (st.ttype == StreamTokenizer.TT_NUMBER) {
                        operations[currentOperation] = (int) st.nval;
                        Main.logger.debug("Time for operation " + currentOperation + "/" + currentJob +
                            " was read " + operations[currentOperation]);
                        st.nextToken();
                    } else {
                        throw new BabFileFormatException("A time for an operation is awaited here: " + st);
                    }
                }
                jobs[currentJob] = operations;
            }
        } else {
            //each line contains the operation's time on ieme machine for all jobs
            for (int i = 0; i < jobs.length; i++) {
                jobs[i] = new int[nbMachines];
            }
            for (int operation = 0; operation < nbMachines; operation++) {
                for (int job = 0; job < nbJobs; job++) {
                    if (st.ttype == StreamTokenizer.TT_NUMBER) {
                        jobs[job][operation] = (int) st.nval;
                        st.nextToken();
                    } else {
                        throw new BabFileFormatException("A time for an operation is awaited here: " + st);
                    }
                }
            }
            for (int i = 0; i < jobs.length; i++) {
                Main.logger.debug("Time for job " + i + " was read " + Permutation.string(jobs[i]));
            }
        }

        // All done
        Main.logger.info("Reading file at " + inputFile.getPath() + " done");
        return new FlowShop(nbMachines, jobs);
    }
}
