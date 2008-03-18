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
package org.objectweb.proactive.ic2d.timit.data;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.objectweb.proactive.api.PAVersion;
import org.objectweb.proactive.benchmarks.timit.result.BasicResultWriter;
import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;
import org.objectweb.proactive.benchmarks.timit.util.basic.ResultBag;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeNodeObject;


/**
 * Used to export the timing data to an xml file.
 * @author vbodnart
 *
 */
public class XMLExporter {
    /**
     * If true the timers hierarchy will be flatten during the exportation
     */
    public static final boolean FLAT_STYLE_GENERATION = true;

    /**
     * The list of sources timers that will be dumped to an xml file
     */
    private final List<BasicChartObject> chartObjectSources;

    /**
     * Creates an instance of the XMLExporter.
     * @param chartObjectSources The list of source objects
     */
    public XMLExporter(final List<BasicChartObject> chartObjectSources) {
        this.chartObjectSources = chartObjectSources;
    }

    /** 
     * Exports the data to an xml output file then prints a log to
     * the use console.<p>
     * Only absolute path here.
     * @param Absolute path of the destination file
     */
    public final void exportTo(final String filename) {
        // Used to log the exportation time
        final long startTime = System.currentTimeMillis();

        // Declare and set the default namespace      
        final BasicResultWriter finalWriter = new BasicResultWriter(filename, "urn:proactive:timit",
            "timitSchema.xsd");

        // Load date formatter
        final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

        // Get Current date
        final Date now = new Date(System.currentTimeMillis());

        // Can possibly add the current JVM version
        finalWriter.addGlobalInformationElement(df.format(now), PAVersion.getProActiveVersion());

        // Here goes specific outputFile generation
        if (FLAT_STYLE_GENERATION) {
            Element e = finalWriter.getETimit();// getRootElement
            Namespace defaultNS = e.getNamespace();

            for (final BasicChartObject c : this.chartObjectSources) {

                Element ao = new Element("ao", defaultNS);
                e.addContent(ao);

                // Set the uniqueID value (short string)
                ao.setAttribute(new Attribute("uniqueID", c.getAoObject().getUniqueID().shortString()));
                // Set the ao name value
                ao.setAttribute(new Attribute("name", c.getAoObject().getName()));
                // Set the ao class name value
                ao.setAttribute(new Attribute("className", c.getAoObject().getClassName()));
                // Set the ao node name value
                ao.setAttribute(new Attribute("nodeName", c.getAoObject().getParent().getName()));
                // Set the ao virtualNode name value
                ao.setAttribute(new Attribute("virtualNodeName", c.getAoObject().getParent()
                        .getVirtualNodeName()));
                // Set the ao host name value
                ao.setAttribute(new Attribute("hostName", c.getAoObject().getParent().getParent().getName()));
                // Set the last refresh date value
                ao.setAttribute(new Attribute("lastRefresh", c.getBarChartBuilder()
                        .getFormattedLastRefreshDate()));

                // Create the timers element
                Element timersElement = new Element("timers", defaultNS);
                ao.addContent(timersElement);

                // Fill the timers list with original basic timers
                for (final TimerTreeNodeObject t : c.getTimersList()) {
                    // Add only if non empty timer
                    if (t.getCurrentTimer().getTotalTime() != 0L) {
                        Element timer = new Element("timer", defaultNS);
                        timersElement.addContent(timer);
                        fillTimerElement(timer, t);
                    }
                }
            }
        } else {
            // Create the global list of result bags
            final List<ResultBag> results = new java.util.ArrayList<ResultBag>(this.chartObjectSources.size());

            for (final BasicChartObject c : this.chartObjectSources) {
                final List<BasicTimer> timersList = new ArrayList<BasicTimer>(c.getTimersList().size());

                // Fill the timers list with original basic timers
                for (final TimerTreeNodeObject t : c.getTimersList()) {
                    if ((t.getCurrentTimer() != null) && (t.getCurrentTimer().getTotalTime() != 0L)) {
                        timersList.add(t.getCurrentTimer());
                    }
                }

                // Add current bag to the
                results.add(new ResultBag(c.getAoObject().getName(), c.getAoObject().getUniqueID()
                        .shortString(), timersList, c.getAoObject().getName() + " on " +
                    c.getAoObject().getParent().getName()));
            }

            // Add results to the output writer
            for (final ResultBag resultBag : results) {
                resultBag.addResultsTo(finalWriter);
            }
        }

        finalWriter.writeToFile();

        // Log a message to the user console
        Console.getInstance(Activator.CONSOLE_NAME).log(
                "XML output generated successfully [" + (System.currentTimeMillis() - startTime) +
                    " ms] ! See : " + filename);

    }

    /**
     * Fills a timer element with attributes specified by a timer object.
     * @param newTimerElement The element to fill
     * @param currentTimer The timer object
     */
    public static final void fillTimerElement(final Element newTimerElement,
            final TimerTreeNodeObject currentTimer) {
        // Set the name as an attribute
        newTimerElement.setAttribute(new Attribute("name", currentTimer.getCurrentTimer().getName()));
        // Set the totalTime in millis of the timer as an attribute
        newTimerElement.setAttribute(new Attribute("totalTimeInMillis", "" +
            (currentTimer.getCurrentTimer().getTotalTime() / 1000000)));
        // Set the percentage from total of the timer as an attribute
        newTimerElement.setAttribute(new Attribute("percentageFromTotal", currentTimer
                .getFormatedPercentageFromTotal()));
        // Set the invocations of the timer as an attribute
        newTimerElement.setAttribute(new Attribute("invocations", "" +
            currentTimer.getCurrentTimer().getStartStopCoupleCount()));
        // Set the percentage from parent of the timer as an attribute
        newTimerElement.setAttribute(new Attribute("percentageFromParent", currentTimer
                .getFormatedPercentageFromParent()));
        // Set the parent name
        newTimerElement.setAttribute(new Attribute("parentName",
            ((currentTimer.getCurrentTimer().getParent() == null) ? "" : ("" + currentTimer.getCurrentTimer()
                    .getParent().getName()))));
        // Set the parentId of the timer as an attribute
        newTimerElement.setAttribute(new Attribute("id", "" + currentTimer.getCurrentTimer().getId()));
    }

}
