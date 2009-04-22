/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.examples;

import javax.swing.JPanel;

import org.ow2.proactive.scheduler.common.task.ResultPreview;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool.SimpleImagePanel;


/**
 * @author The ProActive Team
 * @since 2.2
 */
public class DenoisePreview extends ResultPreview {
    /**
     *
     */
    private static final long serialVersionUID = 10L;
    private static final String MATCH_PATTERN = "Saving output image '";

    /**
     * @see org.ow2.proactive.scheduler.common.task.ResultPreview#getGraphicalDescription(org.ow2.proactive.scheduler.common.task.TaskResult)
     */
    @Override
    public JPanel getGraphicalDescription(TaskResult r) {
        String path = this.getPathToImage(r.getOutput().getStderrLogs(false));
        System.out.println("[RESULT_DESCRIPTOR] Displaying " + path);
        return new SimpleImagePanel(path);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.ResultPreview#getTextualDescription(org.ow2.proactive.scheduler.common.task.TaskResult)
     */
    @Override
    public String getTextualDescription(TaskResult r) {
        return this.getPathToImage(r.getOutput().getStderrLogs(false));
    }

    private String getPathToImage(String output) {
        int pos = output.indexOf(MATCH_PATTERN, 0);
        pos += MATCH_PATTERN.length();
        String extracted = output.substring(pos, output.indexOf('\'', pos));
        extracted = ResultPreviewTool.getSystemCompliantPath(extracted);
        return extracted;
    }
}
