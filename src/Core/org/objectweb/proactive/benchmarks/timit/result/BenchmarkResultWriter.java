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
package org.objectweb.proactive.benchmarks.timit.result;

import java.util.Iterator;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.objectweb.proactive.benchmarks.timit.util.BenchmarkStatistics;
import org.objectweb.proactive.benchmarks.timit.util.EventStatistics;
import org.objectweb.proactive.benchmarks.timit.util.HierarchicalTimerStatistics;
import org.objectweb.proactive.benchmarks.timit.util.XMLHelper;


/**
 * This class generate benchmark statistics result files from values given by
 * the TimIt reductor. Generated file will be an XML file
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 */
public class BenchmarkResultWriter {
    private Document document;
    private Element eTimit;
    private String filename;

    /**
     * Construct a new BenchmarkResultWriter from his filename
     *
     * @param filename
     *            the name of the file to create
     */
    public BenchmarkResultWriter(String filename) {
        this.eTimit = new Element("timit");
        this.document = new Document(this.eTimit);
        this.filename = filename;
    }

    /**
     * @return the root Element of the XML result file
     */
    public Element getRoot() {
        return this.eTimit;
    }

    /**
     * Add a new result to write to the file. Output file is updated every time
     * you invoke this method.
     *
     * @param bstats
     *            the BenchmarkStatistics containing all results
     * @param name
     *            the name of the current benchmark execution
     */
    public void addResult(BenchmarkStatistics bstats, String name) {
        Element benchResult = new Element("BenchmarkStatistics");
        this.eTimit.addContent(benchResult);
        benchResult.setAttribute(new Attribute("name", name));
        benchResult.setAttribute(new Attribute("date",
                "" + (new java.sql.Timestamp(System.currentTimeMillis()))));

        // Timer statistics
        Element eTimers = new Element("timers");
        benchResult.addContent(eTimers);
        this.fillTimersResults(eTimers, bstats.getTimerStatistics());

        // Event statistics
        Element eEvents = new Element("events");
        benchResult.addContent(eEvents);
        this.fillEventsResults(eEvents, bstats.getEventsStatistics());

        // Add informations from workers
        Element informationElement = new Element("Information");
        informationElement.addContent("" + bstats.getInformation());
        benchResult.addContent(informationElement);
    }

    /**
     * Write results into the file
     *
     */
    public void writeResult() {
        // Save modification into file
        XMLHelper.writeFile(this.document, this.filename);
    }

    /**
     * This method remove benchmarks that have extrem total time values.
     * (ie. it remove the min and the max)
     */
    public void removeExtremums() {
        Document doc = XMLHelper.readFile(this.filename);
        Element root = doc.getRootElement();
        Iterator<Element> it = root.getDescendants(new ElementFilter("timers"));

        // Look for the max and min values...
        double vMax = Double.MIN_VALUE;
        double vMin = Double.MAX_VALUE;
        Element max = null;
        Element min = null;
        int size = 0;

        while (it.hasNext()) {
            size++;
            Element timers = it.next();
            Element eTimer = timers.getChild("timer");
            if (eTimer == null) {
                continue;
            }
            double value = Double.valueOf(eTimer.getAttributeValue("avg"));
            if (value > vMax) {
                vMax = value;
                max = timers;
            }
            if (value < vMin) {
                vMin = value;
                min = timers;
            }
        }

        // ... end remove them if there is more than 3 runs
        if ((size >= 3) && (max != null) && (min != null)) {
            max.getParentElement().detach();
            min.getParentElement().detach();
        }

        // then save the result
        XMLHelper.writeFile(doc, this.filename);
    }

    //
    // -- PRIVATE METHODS ----------------------------------------------------
    //

    /**
     * Fill in pure timing values from 'timer' in the sub tag 'eTimers'
     */
    private void fillTimersResults(Element eTimers,
        HierarchicalTimerStatistics timer) {
        if (timer == null) {
            return;
        }

        Element currentElement = null;
        Element rootElement = null;
        Element parentElement = null;
        Attribute nameAttr = null;
        Attribute minAttr;
        Attribute avgAttr;
        Attribute maxAttr;
        Attribute devAttr;
        String[] nameArray = timer.getNameArray();
        int i;
        int j;
        int k;
        int nb = timer.getNb();
        String rootName = nameArray[0];

        for (i = 0; i < nb; i++) {
            for (j = 0; j < nb; j++) {
                for (k = 0; k < nb; k++) {
                    if (timer.getMin(i, j, k) != -1) {
                        if (nameArray[k] != null) {
                            if (nameArray[k].equals(rootName)) {
                                currentElement = new Element("timer");
                                nameAttr = new Attribute("name", rootName);
                                rootElement = currentElement;
                            } else {
                                if (nameArray[j] != null) {
                                    if (nameArray[k].equals(nameArray[j])) {
                                        currentElement = new Element("timer");
                                        nameAttr = new Attribute("name",
                                                nameArray[j]);
                                        // If there is a root error
                                        if (rootElement == null) {
                                            throw new IllegalStateException(
                                                "-- Timer " + nameArray[j] +
                                                " has a null root. Please check your start-stop pairs for this timer.");
                                        }
                                        rootElement.addContent(currentElement);
                                        parentElement = currentElement;
                                    } else {
                                        currentElement = new Element("timer");
                                        nameAttr = new Attribute("name",
                                                nameArray[k]);
                                        parentElement.addContent(currentElement);
                                    }
                                }
                            }
                            if (nameAttr != null) {
                                currentElement.setAttribute(nameAttr);
                            }
                            minAttr = new Attribute("min",
                                    timer.getFormMin(i, j, k));
                            if (currentElement != null) {
                                currentElement.setAttribute(minAttr);
                            }
                        }
                    }

                    if (timer.getAverage(i, j, k) != -1) {
                        avgAttr = new Attribute("avg",
                                timer.getFormAverage(i, j, k));
                        if (currentElement != null) {
                            currentElement.setAttribute(avgAttr);
                        }
                    }

                    if (timer.getMax(i, j, k) != -1) {
                        maxAttr = new Attribute("max", timer.getFormMax(i, j, k));
                        if (currentElement != null) {
                            currentElement.setAttribute(maxAttr);
                        }
                    }

                    if (timer.getDeviation(i, j, k) != -1) {
                        devAttr = new Attribute("dev",
                                timer.getFormDeviation(i, j, k));
                        if (currentElement != null) {
                            currentElement.setAttribute(devAttr);
                        }
                    }
                }
            }
        }
        if (rootElement != null) {
            eTimers.addContent(rootElement);
        }
    }

    /**
     * Fill in pure event values from 'events' in the sub tag 'eEvents'
     */
    private void fillEventsResults(Element eEvents, EventStatistics events) {
        if (events == null) {
            return;
        }

        Element currentElement;
        Attribute nameAttr;
        String eventValue;

        // Add events observers
        for (int i = 0; i < events.getNb(); i++) {
            currentElement = new Element("event");
            eEvents.addContent(currentElement);
            nameAttr = new Attribute("name", events.getName(i));
            eventValue = events.getEventValue(i).toString();
            currentElement.setAttribute(nameAttr);

            try {
                currentElement.setText("" + Double.valueOf(eventValue));
            } catch (NumberFormatException e) {
                currentElement.setText(".\n" + eventValue);
            }
        }
    }
}
