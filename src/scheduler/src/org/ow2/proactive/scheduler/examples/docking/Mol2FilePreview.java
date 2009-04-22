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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.examples.docking;

import java.io.IOException;

import javax.swing.JPanel;

import org.ow2.proactive.scheduler.common.task.ResultPreview;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool.SimpleTextPanel;


/**
 * @author The ProActive Team
 *
 */
public class Mol2FilePreview extends ResultPreview {

    /**
     *
     */
    private static final long serialVersionUID = 10L;
    private static final String MATCH_PATTERN = "Produced output file : ";

    /**
     * @see org.ow2.proactive.scheduler.common.task.ResultPreview#getGraphicalDescription(org.ow2.proactive.scheduler.common.task.TaskResult)
     */
    @Override
    public JPanel getGraphicalDescription(TaskResult r) {
        try {
            String pathToResult = this.getPathToFile(r.getOutput().getStdoutLogs(false));
            String molEditor = System.getenv("MOL_EDITOR");
            if (molEditor == null) {
                // mercury is default (supposed to be in the path)
                molEditor = "mercury";
            }
            String command = molEditor + " " + pathToResult;
            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                return new SimpleTextPanel("Unable to open external display : " + e.getMessage());
            }
            return new SimpleTextPanel("External display : " + command);
        } catch (RuntimeException e) {
            return new SimpleTextPanel("Unable to open external display : " + e.getMessage());
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.ResultPreview#getTextualDescription(org.ow2.proactive.scheduler.common.task.TaskResult)
     */
    @Override
    public String getTextualDescription(TaskResult r) {
        return "Output file : " + this.getPathToFile(r.getOutput().getStdoutLogs(false));
    }

    private String getPathToFile(String output) {
        int pos = output.indexOf(MATCH_PATTERN, 0);
        pos += MATCH_PATTERN.length();
        String extracted = output.substring(pos, output.indexOf(".mol2", pos));
        //extracted = ResultPreviewTool.getSystemCompliantPath(extracted);
        return extracted + ".mol2";
    }

}
