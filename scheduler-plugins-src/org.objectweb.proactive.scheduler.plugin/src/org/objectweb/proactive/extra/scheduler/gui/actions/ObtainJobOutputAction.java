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
package org.objectweb.proactive.extra.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.TableItem;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.gui.data.JobsOutputController;
import org.objectweb.proactive.extra.scheduler.gui.data.TableManager;


public class ObtainJobOutputAction extends Action {
    public static final boolean ENABLED_AT_CONSTRUCTION = false;
    private static ObtainJobOutputAction instance = null;

    private ObtainJobOutputAction() {
        this.setText("Get job output");
        this.setToolTipText("To get the job output");
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "icons/output.png"));
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    @Override
    public void run() {
        TableItem item = TableManager.getInstance().getLastSelectedItem();
        if ((item != null) && (!item.isDisposed())) {
            JobId jobId = (JobId) item.getData();
            JobsOutputController.getInstance().createJobOutput(jobId);
        }
    }

    public static ObtainJobOutputAction newInstance() {
        instance = new ObtainJobOutputAction();
        return instance;
    }

    public static ObtainJobOutputAction getInstance() {
        return instance;
    }
}
