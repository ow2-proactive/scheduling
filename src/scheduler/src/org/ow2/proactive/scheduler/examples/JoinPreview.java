/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.examples;

import java.io.IOException;

import javax.swing.JPanel;

import org.ow2.proactive.scheduler.common.task.ResultPreview;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool.SimpleImagePanel;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool.SimpleTextPanel;


/**
 * JoinPreview...
 *
 * @author The ProActive Team
 *
 */
public class JoinPreview extends ResultPreview {
    /**  */
    private static final long serialVersionUID = 200;
    private static final String MATCH_PATTERN = "Merged picture parts in ";

    /**
     * @see org.ow2.proactive.scheduler.common.task.ResultPreview#getGraphicalDescription(org.ow2.proactive.scheduler.common.task.TaskResult)
     */
    @Override
    public JPanel getGraphicalDescription(TaskResult r) {
        String pathToResult = this.getPathToImage(r.getOutput().getStdoutLogs(false));
        String jpgEditor = System.getenv("JPG_EDITOR");
        if (jpgEditor != null) {
            String command = jpgEditor + " " + pathToResult;
            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                return new SimpleTextPanel("Unable to open external display : " + e.getMessage());
            }
            return new SimpleTextPanel("External display : " + command);
        } else {
            return new SimpleImagePanel(pathToResult);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.ResultPreview#getTextualDescription(org.ow2.proactive.scheduler.common.task.TaskResult)
     */
    @Override
    public String getTextualDescription(TaskResult r) {
        return this.getPathToImage(r.getOutput().getStdoutLogs(false));
    }

    private String getPathToImage(String output) {
        int pos = output.indexOf(MATCH_PATTERN, 0);
        pos += MATCH_PATTERN.length();
        String extracted = output.substring(pos, output.indexOf(".jpg", pos));
        extracted = ResultPreviewTool.getSystemCompliantPath(extracted);
        return extracted + ".jpg";
    }
}
