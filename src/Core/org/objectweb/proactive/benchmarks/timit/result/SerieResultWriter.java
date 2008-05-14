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

import java.text.DecimalFormat;
import java.util.Iterator;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.objectweb.proactive.benchmarks.timit.TimIt;
import org.objectweb.proactive.benchmarks.timit.util.XMLHelper;


/**
 * This class generate final serie result file from generated benchmark
 * statistics result file. Generated file will be an XML file
 *
 * @author The ProActive Team
 */
@SuppressWarnings("unchecked")
public class SerieResultWriter {
    private Document document;
    private Element eTimit;
    private String filename;

    /**
     * Construct a new SerieResultWriter from his filename
     *
     * @param filename
     *            the name of the file to create
     */
    public SerieResultWriter(String filename) {
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
     * @param bResults
     *            the root Element of XML benchmark result file
     * @param name
     *            the name of the current serie execution
     * @param runs
     *            the number of runs for this benchmark statistics result
     * @param totalTimeoutErrors
     *            the total number of timeout errors which occured
     *            during the serie run
     */
    public void addResult(Element bResults, String name, int runs, int totalTimeoutErrors) {
        Element benchResult = new Element("FinalStatistics");
        this.eTimit.addContent(benchResult);
        benchResult.setAttribute(new Attribute("name", name));
        benchResult.setAttribute(new Attribute("runs", "" + runs));
        benchResult.setAttribute(new Attribute("timeoutErrors", "" + totalTimeoutErrors));
        benchResult.setAttribute(new Attribute("date", "" +
            (new java.sql.Timestamp(System.currentTimeMillis()))));

        // Timer statistics
        this.fillTimersResults(benchResult, bResults.getDescendants(new ElementFilter("timers")));

        // Event statistics
        this.fillEventsResults(benchResult, bResults.getDescendants(new ElementFilter("events")));

        // Informations
        this.fillInformations(benchResult);

        // Save modification into file
        XMLHelper.writeFile(this.document, this.filename);
    }

    //
    // -- PRIVATE METHODS ----------------------------------------------------
    //

    /**
     * Compute min/avg/max/dev values from list benchmark statistics run,
     * then fill in values in the sub tag eTimers
     */
    private void fillTimersResults(Element eTimers, Iterator itTimers) {
        Iterator initIterator;
        Element temp;

        if (!itTimers.hasNext()) {
            return;
        }

        // Merged results will be put in a clone of first result
        Element eCurrentTimers = (Element) itTimers.next();
        Element eCloneTimers = (Element) eCurrentTimers.clone();
        eTimers.addContent(eCloneTimers);
        // Init the final values
        initIterator = eCloneTimers.getDescendants();
        while (initIterator.hasNext()) {
            temp = (Element) initIterator.next();
            // temp.setAttribute("min","0"); // do not init min values
            temp.setAttribute("avg", "0");
            temp.setAttribute("max", "0");
            temp.setAttribute("dev", "0");
        }

        // Merging results...
        // The array of double values ie min avg max dev
        double[] tab1 = new double[4];
        double[] tab2 = new double[4];
        int run = 0;
        do {
            run++;
            Iterator targetIterator = eCurrentTimers.getDescendants();
            Iterator finalIterator = eCloneTimers.getDescendants();
            while (targetIterator.hasNext() && finalIterator.hasNext()) {
                Element e1 = (Element) targetIterator.next();
                Element e2 = (Element) finalIterator.next();
                if (!e1.getAttributeValue("name").equals(e2.getAttributeValue("name"))) {
                    throw new IllegalStateException("You are trying to finalize different timers !");
                }
                // Get current target element ie timer values
                tab1[0] = Double.valueOf(e1.getAttributeValue("min"));
                tab1[1] = Double.valueOf(e1.getAttributeValue("avg"));
                tab1[2] = Double.valueOf(e1.getAttributeValue("max"));
                // Get final values
                tab2[0] = Double.valueOf(e2.getAttributeValue("min"));
                tab2[1] = Double.valueOf(e2.getAttributeValue("avg"));
                tab2[2] = Double.valueOf(e2.getAttributeValue("max"));
                tab2[3] = Double.valueOf(e2.getAttributeValue("dev"));
                // Min
                if (tab1[0] < tab2[0]) {
                    tab2[0] = tab1[0];
                }
                // Avg
                tab2[1] += tab1[1];
                // Max
                if (tab1[2] > tab2[2]) {
                    tab2[2] = tab1[2];
                }
                // Deviation
                tab2[3] += (tab1[1] * tab1[1]);
                // Set back the final values
                e2.setAttribute("min", "" + tab2[0]);
                e2.setAttribute("avg", "" + TimIt.df.format(tab2[1]));
                e2.setAttribute("max", "" + tab2[2]);
                e2.setAttribute("dev", "" + tab2[3]);
            }
            if (itTimers.hasNext()) {
                eCurrentTimers = (Element) itTimers.next();
            } else {
                break;
            }
        } while (true);

        //
        // Finalize standard deviation, avg values and children sum
        double average;

        //
        // Finalize standard deviation, avg values and children sum
        double deviation;

        //
        // Finalize standard deviation, avg values and children sum
        double sqrt;

        //
        // Finalize standard deviation, avg values and children sum
        double childrenSum;
        Iterator children;
        Iterator finalIterator = eCloneTimers.getDescendants();
        while (finalIterator.hasNext()) {
            Element e1 = (Element) finalIterator.next();
            average = Double.valueOf(e1.getAttributeValue("avg")) / run;
            deviation = Double.valueOf(e1.getAttributeValue("dev"));
            // Set Avg value back
            e1.setAttribute("avg", "" + TimIt.df.format(average));
            // Compute Avg^2
            average = average * average;
            // Compute Deviation
            sqrt = (deviation / run) - average; // avoid truncatures problems
            deviation = Math.sqrt((sqrt > 0) ? sqrt : 0);
            // Set back the final deviation value
            e1.setAttribute("dev", "" + TimIt.df.format(deviation));

            // Children sum computation
            childrenSum = 0.0;
            children = e1.getChildren().iterator();
            while (children.hasNext()) {
                childrenSum += Double.valueOf(((Element) children.next()).getAttributeValue("avg"));
            }
            e1.setAttribute(new Attribute("sum", TimIt.df.format(childrenSum / run)));
        }
    }

    /**
     * Compute min/avg/max/dev values from list benchmark statistics run,
     * then fill in values in the sub tag eTimers
     */
    private void fillEventsResults(Element eEvents, Iterator itEvents) {
        Iterator initIterator;
        Element temp;
        DecimalFormat df = TimIt.df;

        if (!itEvents.hasNext()) {
            return;
        }

        // Merged results will be put in a clone of first result
        Element eCurrentEvents = (Element) itEvents.next();
        Element eCloneEvents = (Element) eCurrentEvents.clone();
        eEvents.addContent(eCloneEvents);
        // Init the final values
        initIterator = eCloneEvents.getChildren().iterator();
        while (initIterator.hasNext()) {
            temp = (Element) initIterator.next();
            try {
                // If this instruction works, then we can perform
                // a min/avg/max/dev computation on this value
                Double.valueOf(temp.getValue());
                Attribute min = new Attribute("min", "" + Double.MAX_VALUE);
                Attribute avg = new Attribute("avg", "0");
                Attribute max = new Attribute("max", "" + Double.MIN_VALUE);
                Attribute dev = new Attribute("dev", "0");
                temp.setAttribute(min);
                temp.setAttribute(avg);
                temp.setAttribute(max);
                temp.setAttribute(dev);
            } catch (NumberFormatException e) {
                // We can't perform a min/avg/max/dev computation on this value
                temp.setAttribute(new Attribute("value", "Too complex value, first run shown"));
            }
        }

        // Merging results...
        double fMin;

        // Merging results...
        double fAvg;

        // Merging results...
        double fMax;

        // Merging results...
        double fDev;

        // Merging results...
        double tValue;
        int run = 0;
        do {
            run++;
            Iterator targetIterator = eCurrentEvents.getDescendants(new ElementFilter("event"));
            Iterator finalIterator = eCloneEvents.getDescendants(new ElementFilter("event"));
            while (targetIterator.hasNext() && finalIterator.hasNext()) {
                Element e1 = (Element) targetIterator.next();
                Element e2 = (Element) finalIterator.next();
                if (!e1.getAttributeValue("name").equals(e2.getAttributeValue("name"))) {
                    throw new IllegalStateException("You are trying to finalize different events !");
                }
                try {
                    if (e2.getAttribute("value") == null) {
                        // initialize values
                        tValue = Double.valueOf(e1.getValue());
                        fMin = Double.valueOf(e2.getAttributeValue("min"));
                        fAvg = Double.valueOf(e2.getAttributeValue("avg"));
                        fMax = Double.valueOf(e2.getAttributeValue("max"));
                        fDev = Double.valueOf(e2.getAttributeValue("dev"));

                        // compute min / avg / max / dev
                        if (tValue < fMin) {
                            fMin = tValue;
                        }
                        fAvg += tValue;
                        if (tValue > fMax) {
                            fMax = tValue;
                        }
                        fDev += (tValue * tValue);

                        // Set back the final values
                        e2.setAttribute("min", "" + df.format(fMin));
                        e2.setAttribute("avg", "" + df.format(fAvg));
                        e2.setAttribute("max", "" + df.format(fMax));
                        e2.setAttribute("dev", "" + df.format(fDev));
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            if (itEvents.hasNext()) {
                eCurrentEvents = (Element) itEvents.next();
            } else {
                break;
            }
        } while (true);

        //
        // Finalize standard deviation and avg values
        double average;

        //
        // Finalize standard deviation and avg values
        double deviation;

        //
        // Finalize standard deviation and avg values
        double sqrt;
        @SuppressWarnings("unchecked")
        Iterator finalIterator = eCloneEvents.getChildren().iterator();
        while (finalIterator.hasNext()) {
            Element e1 = (Element) finalIterator.next();
            if (e1.getAttribute("value") == null) {
                average = Double.valueOf(e1.getAttributeValue("avg")) / run;
                deviation = Double.valueOf(e1.getAttributeValue("dev"));
                // Set Avg value back
                e1.setAttribute("avg", "" + TimIt.df.format(average));
                // Compute Avg^2
                average = average * average;
                // Compute Deviation
                sqrt = (deviation / run) - average; // avoid truncatures
                // problems

                deviation = Math.sqrt((sqrt > 0) ? sqrt : 0);
                // Set back the final deviation value
                e1.setAttribute("dev", "" + TimIt.df.format(deviation));
            }
        }
    }

    /**
     * Add some informations about the used VMs
     * @param benchResults
     */
    private void fillInformations(Element benchResults) {
        Element eInfos = new Element("informations");
        benchResults.addContent(eInfos);

        //
        // Informations about deployer machine
        //
        Element eDeployer = new Element("deployer");
        eInfos.addContent(eDeployer);
        // JVM version
        String jvmVersion = System.getProperty("java.vm.vendor") + " " + System.getProperty("java.vm.name") +
            " " + System.getProperty("java.vm.version") + " - Version " + System.getProperty("java.version");
        eDeployer.setAttribute(new Attribute("jvm", jvmVersion));

        // OS Version
        String osVersion = System.getProperty("os.arch") + " " + System.getProperty("os.name") + " " +
            System.getProperty("os.version");
        eDeployer.setAttribute(new Attribute("os", osVersion));

        // Processor count
        eDeployer.setAttribute(new Attribute("processors", "" + Runtime.getRuntime().availableProcessors()));
    }
}
