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
package org.objectweb.proactive.ic2d.timit.actions.tree;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.data.XMLExporter;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeHolder;
import org.objectweb.proactive.ic2d.timit.editparts.SafeSaveDialog;


/**
 * This action is used when the user wants to save all data in the tree view in a xml file. 
 * @author The ProActive Team
 *
 */
public class SaveToXmlAction extends Action {
    public static final String SAVE_TO_XML_ACTION = "Save All to XML";
    private TimerTreeHolder timerTreeHolder;

    public SaveToXmlAction(final TimerTreeHolder t) {
        this.timerTreeHolder = t;
        super.setId(SAVE_TO_XML_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/save_edit.gif"), null)));
        super.setToolTipText(SAVE_TO_XML_ACTION);
        super.setEnabled(true);
    }

    @Override
    public final void run() {
        if ((this.timerTreeHolder == null) || (this.timerTreeHolder.getChartObjectSources() == null) ||
            (this.timerTreeHolder.getChartObjectSources().size() == 0)) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Cannot generate XML output file. Nothing to save.");
            return;
        }

        final SafeSaveDialog safeSaveDialog = new SafeSaveDialog(Display.getDefault().getActiveShell());

        final String path = safeSaveDialog.open();

        // Bad path
        if ((path == null) || "".equals(path)) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Cannot generate XML output file. Please provide a correct output file path.");
            return;
        }

        XMLExporter xmlExporter = new XMLExporter(this.timerTreeHolder.getChartObjectSources());
        xmlExporter.exportTo(path);
    }

}
