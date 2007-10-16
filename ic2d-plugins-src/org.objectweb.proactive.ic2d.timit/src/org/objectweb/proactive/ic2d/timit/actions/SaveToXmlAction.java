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
package org.objectweb.proactive.ic2d.timit.actions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.benchmarks.timit.result.BasicResultWriter;
import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;
import org.objectweb.proactive.benchmarks.timit.util.basic.ResultBag;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.data.ChartObject;
import org.objectweb.proactive.ic2d.timit.data.TimerObject;
import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;
import org.objectweb.proactive.ic2d.timit.editparts.SafeSaveDialog;


public class SaveToXmlAction extends Action {
    public static final String SAVE_TO_XML_ACTION = "Save All to XML";
    private TimerTreeHolder timerTreeHolder;

    public SaveToXmlAction(final TimerTreeHolder t) {
        this.timerTreeHolder = t;
        super.setId(SAVE_TO_XML_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "save_edit.gif"));
        super.setToolTipText(SAVE_TO_XML_ACTION);
        super.setEnabled(true);
    }

    @Override
    public final void run() {
        if ((this.timerTreeHolder == null) ||
                (this.timerTreeHolder.getChartObjectSources() == null) ||
                (this.timerTreeHolder.getChartObjectSources().size() == 0)) {
            Console.getInstance(Activator.CONSOLE_NAME)
                   .log("Cannot generate XML output file. Nothing to save.");
            return;
        }

        SafeSaveDialog safeSaveDialog = new SafeSaveDialog(Display.getDefault()
                                                                  .getActiveShell());

        String path = safeSaveDialog.open();

        // Bad path
        if ((path == null) || "".equals(path)) {
            Console.getInstance(Activator.CONSOLE_NAME)
                   .log("Cannot generate XML output file. Please provide a correct output file path.");
            return;
        }

        // Create the global list of result bags
        List<ResultBag> results = new java.util.ArrayList<ResultBag>(this.timerTreeHolder.getChartObjectSources()
                                                                                         .size());

        for (ChartObject c : this.timerTreeHolder.getChartObjectSources()) {
            List<BasicTimer> timersList = new ArrayList<BasicTimer>(c.getTimersList()
                                                                     .size());

            // Fill the timers list with original basic timers
            for (TimerObject t : c.getTimersList()) {
                if ((t.getCurrentTimer() != null) && t.isViewed()) {
                    timersList.add(t.getCurrentTimer());
                }
            }

            // Add current bag to the
            results.add(new ResultBag(c.getAoObject().getName(),
                    c.getAoObject().getUniqueID().shortString(), timersList,
                    c.getAoObject().getName() + " on " +
                    c.getAoObject().getParent().getName()));
        }

        BasicResultWriter finalWriter = new BasicResultWriter(path);

        // Load date formatter
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.MEDIUM);

        // Get Current date
        Date now = new Date();
        now.setTime(System.currentTimeMillis());

        // Can possibly add the current JVM version
        finalWriter.addGlobalInformationElement(
            "This XML file was generated : " + df.format(now));

        // Add results to the output writer
        for (ResultBag resultBag : results) {
            resultBag.addResultsTo(finalWriter);
        }

        finalWriter.writeToFile();

        Console.getInstance(Activator.CONSOLE_NAME)
               .log("Successful XML output file generation. See : " + path);
    }
}
