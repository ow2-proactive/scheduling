/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.composite;

import javax.swing.JPanel;

import org.ow2.proactive.gui.common.SWTGuiThreadResultHandler;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool.SimpleTextPanel;
import org.ow2.proactive.scheduler.gui.views.ResultPreview;


/**
 * GUI callback action which is called after completion of the asynchronous
 * action 'Scheduler.getTaskResult'.
 *    
 * @author The ProActive Team
 *
 */
class GetTaskResultHandler extends SWTGuiThreadResultHandler<TaskResult> {

    private final boolean grapchicalPreview;

    GetTaskResultHandler(boolean grapchicalPreview) {
        this.grapchicalPreview = grapchicalPreview;
    }

    @Override
    protected void handleResultInGuiThread(TaskResult result) {
        ResultPreview resultPreview = ResultPreview.getInstance();
        if (resultPreview != null) {
            if (grapchicalPreview) {
                displayGraphicalPreview(resultPreview, result);
                resultPreview.putOnTop();
            } else {
                displayTextualPreview(resultPreview, result);
                resultPreview.putOnTop();
            }
        }
    }

    /**
     * Display in Result Preview view graphical description of a task result if
     * any graphical description is available for this task
     * @param resultPreview Result preview SWT view object
     * @param result TaskResult that has eventually a graphical description
     */
    private void displayGraphicalPreview(ResultPreview resultPreview, TaskResult result) {
        JPanel previewPanel;
        try {
            previewPanel = result.getGraphicalDescription();
            resultPreview.update(previewPanel);
        } catch (Throwable t) {
            // root exception can be wrapped into ProActive level exception
            // try to display also cause exception.
            // TODO cdelbe : recursive display ?
            String cause = t.getCause() != null ? System.getProperty("line.separator") + "caused by " +
                t.getCause() : "";
            resultPreview.update(new SimpleTextPanel("[ERROR] Cannot create graphical previewer: " +
                System.getProperty("line.separator") + t + cause));
        }
    }

    /**
     * Display in Result Preview view textual description of a task result.
     * @param resultPreview Result preview SWT view object
     * @param result TaskResult for which a textual description is to display
     */
    private void displayTextualPreview(ResultPreview resultPreview, TaskResult result) {
        JPanel previewPanel;
        try {
            previewPanel = new SimpleTextPanel(result.getTextualDescription());
            resultPreview.update(previewPanel);
        } catch (Throwable t) {
            // root exception can be wrapped into ProActive level exception
            // try to display also cause exception.
            String cause = t.getCause() != null ? System.getProperty("line.separator") + "caused by " +
                t.getCause() : "";
            resultPreview.update(new SimpleTextPanel("[ERROR] Cannot create textual previewer: " +
                System.getProperty("line.separator") + t + cause));
        }
    }

}
