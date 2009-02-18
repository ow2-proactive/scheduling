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
package org.ow2.proactive.scheduler.gui.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.gui.views.SeparatedJobView;


/**
 * @author The ProActive Team
 */
public class ChangeViewModeAction extends SchedulerGUIAction {

    private static final String HORIZONTAL_ICON_URL = "icons/horizontal.gif";
    private static final String VERTICAL_ICON_URL = "icons/vertical.gif";

    public ChangeViewModeAction() {
        this.setText("Switch view mode");
        this.setToolTipText("Switch view to horizontal mode");
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), HORIZONTAL_ICON_URL));
        this.setEnabled(false);
    }

    @Override
    public void run() {
        switch (SeparatedJobView.getSashForm().getOrientation()) {
            case SWT.HORIZONTAL:
                SeparatedJobView.getSashForm().setOrientation(SWT.VERTICAL);
                this.setToolTipText("Switch view to vertical mode");
                this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), VERTICAL_ICON_URL));
                break;
            case SWT.VERTICAL:
                SeparatedJobView.getSashForm().setOrientation(SWT.HORIZONTAL);
                this.setToolTipText("Switch view to horizontal mode");
                this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), HORIZONTAL_ICON_URL));
                break;
        }
    }

    @Override
    public void setEnabled(boolean connected, SchedulerState schedulerState, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        if (connected)
            setEnabled(true);
        else
            setEnabled(false);
    }
}
