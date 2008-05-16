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
package org.objectweb.proactive.extensions.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.objectweb.proactive.extensions.scheduler.gui.views.SeparatedJobView;


/**
 * @author The ProActive Team
 */
public class ChangeViewModeAction extends Action {
    public static final boolean ENABLED_AT_CONSTRUCTION = false;
    private static ChangeViewModeAction instance = null;

    private ChangeViewModeAction() {
        this.setText("Switch view mode");
        this.setToolTipText("Switch view to horizontal mode");
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "icons/horizontal.png"));
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    @Override
    public void run() {
        switch (SeparatedJobView.getSashForm().getOrientation()) {
            case SWT.HORIZONTAL:
                SeparatedJobView.getSashForm().setOrientation(SWT.VERTICAL);
                this.setToolTipText("Switch view to vertical mode");
                this
                        .setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(),
                                "icons/vertical.png"));
                break;
            case SWT.VERTICAL:
                SeparatedJobView.getSashForm().setOrientation(SWT.HORIZONTAL);
                this.setToolTipText("Switch view to horizontal mode");
                this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(),
                        "icons/horizontal.png"));
                break;
        }
    }

    public static ChangeViewModeAction newInstance() {
        instance = new ChangeViewModeAction();
        return instance;
    }

    public static ChangeViewModeAction getInstance() {
        return instance;
    }
}
