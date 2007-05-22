/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.benchmarks.timit.util.basic;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.benchmarks.timit.result.BasicResultWriter;
import org.objectweb.proactive.benchmarks.timit.util.service.TimItTechnicalService;


/**
 * A common timit reductor object that can be active to be able to receive
 * timers from timed active objects.
 *
 * @author vbodnart
 */
public final class TimItBasicReductor {

    /** A list of result bags */
    private List<ResultBag> results = new java.util.Vector<ResultBag>();

    /** The name of the output file */
    private String filename;

    /** Some global information to add to the output file */
    private String globalInformation;

    /** Used to generate or not an output file */
    private boolean generateOutputFile;

    /** Used to print or not output */
    private boolean printOutput;

    /** A counter of awaited results from timer containers */
    private int awaitedResults = 0;

    /**
     * An empty constructor because this object will be active.
     */
    public TimItBasicReductor() {
    }

    /**
     * This method must be called once. Registers a shutdown hook to this vm so
     * all stats generation will perform after all timers are received.
     */
    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    generateAllStatistics();
                }
            });
    }

    /**
     * Used to increment the awaited result each time the stub on this object is
     * given to a timer container.
     */
    public void incrementAwaitedResult() {
        awaitedResults++;
    }

    /**
     * Will be called by the shutdown hook to generate all results on this
     * machine.
     */
    public void generateAllStatistics() {
        this.generateOutputFile = Boolean.parseBoolean(TimItTechnicalService.getGenerateOutputFile());
        this.printOutput = Boolean.parseBoolean(TimItTechnicalService.getPrintOutput());
        //String filename = "TimItApplicationOutput" + System.currentTimeMillis();
        File folder = new File("output");
        folder.mkdir();
        BasicResultWriter finalWriter = new BasicResultWriter(folder.getAbsolutePath() +
                "/" + filename);
        finalWriter.addGlobalInformationElement(globalInformation);
        Iterator<ResultBag> resultsIterator = this.results.iterator();
        while (resultsIterator.hasNext()) {
            ResultBag currentResultBag = resultsIterator.next();
            currentResultBag.addResultsTo(finalWriter);
        }
        if (this.generateOutputFile) {
            finalWriter.writeToFile();
        }
        if (this.printOutput) {
            finalWriter.printMe();
        }
    }

    /**
     * Called by the timers container to send its timers. This method is
     * synchronous, since the caller must be sure that all its timers are added
     * to the global results before its termination.
     *
     * @param className
     *            The classname of the timed active object
     * @param shortUniqueID
     *            A short version of the caller unique id
     * @param timersList
     *            The list of timers attached to the timed active object
     * @param otherInformation
     *            Some information that could be the output of a toString method
     * @return Always return true to force synchronization
     */
    public boolean receiveTimers(String className, String shortUniqueID,
        List<BasicTimer> timersList, String otherInformation) {
        results.add(new ResultBag(className, shortUniqueID, timersList,
                otherInformation));
        // decrement the awaited results
        awaitedResults--;
        // If all result are received prepare to finish
        if (awaitedResults <= 0) {
            filename = ProActive.getBodyOnThis().getID().shortString();
            TimItBasicManager.getInstance().setReductorNull();
            awaitedResults = 0;

            try {
                ProActive.getBodyOnThis().terminate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // This is used to make the method synchronous
        return true;
    }

    /**
     * Called by the timers container to send its timers. This method is called
     * when the results are generated directly without the reductor.
     *
     * @param className
     *            The classname of the timed active object
     * @param shortUniqueID
     *            A short version of the caller unique id
     * @param timersList
     *            The list of timers attached to the timed active object
     * @param otherInformation
     *            Some information that could be the output of a toString method
     */
    public void receiveTimersDirectMode(String className, String shortUniqueID,
        List<BasicTimer> timersList, String otherInformation) {
        ResultBag b = new ResultBag(className, shortUniqueID, timersList,
                otherInformation);
        this.generateOutputFile = Boolean.parseBoolean(TimItTechnicalService.getGenerateOutputFile());
        this.printOutput = Boolean.parseBoolean(TimItTechnicalService.getPrintOutput());
        File folder = new File("output");
        folder.mkdir();
        this.filename = folder.getAbsolutePath() + "/output" +
            (Math.random() * 1000) + "" + this.hashCode();
        if (this.generateOutputFile) {
            b.printToFile();
        }
        if (this.printOutput) {
            b.printXMLFormattedTree();
        }
    }

    /**
     * @return the generateOutputFile
     */
    public boolean isGenerateOutputFile() {
        return generateOutputFile;
    }

    /**
     * @param generateOutputFile
     *            the generateOutputFile to set
     */
    public void setGenerateOutputFile(boolean generateOutputFile) {
        this.generateOutputFile = generateOutputFile;
    }

    /**
     * @return the printOutput
     */
    public boolean isPrintOutput() {
        return printOutput;
    }

    /**
     * @param printOutput
     *            the printOutput to set
     */
    public void setPrintOutput(boolean printOutput) {
        this.printOutput = printOutput;
    }

    /**
     * Returns the size of the result list.
     *
     * @return The size of the results list
     */
    public int getResultsSize() {
        return results.size();
    }

    /**
     * @param results
     *            the results to set
     */
    public void setResults(List<ResultBag> results) {
        this.results = results;
    }

    /**
     * A getter for the global information
     * @return The globalInformation string
     */
    public String getGlobalInformation() {
        return globalInformation;
    }

    /**
     * A setter for the globalInformation.
     * @param globalInformation
     */
    public void setGlobalInformation(String globalInformation) {
        this.globalInformation = globalInformation;
    }
}
