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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Control;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.gui.views.SeparatedJobView;


/**
 * @author The ProActive Team
 */
public class MaximizeListAction extends SchedulerGUIAction {
    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int FINISHED = 2;
    public static final int NONE = 3;
    public static final boolean ENABLED_AT_CONSTRUCTION = false;

    private static Map<Integer, MaximizeListAction> instances = new HashMap<Integer, MaximizeListAction>();
    private Control control;
    private int mode;

    private MaximizeListAction(Control control, int mode) {
        this.control = control;
        this.mode = mode;
        this.setText(modeToString());
        this.setToolTipText("To maximize the " + modeToString() + " jobs list");
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    @Override
    public void run() {
        SeparatedJobView.getSashForm().setMaximizedControl(control);
    }

    public static MaximizeListAction newInstance(Control control, int mode) {
        MaximizeListAction instance = new MaximizeListAction(control, mode);
        instances.put(mode, instance);
        return instance;
    }

    public static MaximizeListAction getInstance(int mode) {
        return instances.get(mode);
    }

    private String modeToString() {
        switch (mode) {
            case PENDING:
                return "pending";
            case RUNNING:
                return "running";
            case FINISHED:
                return "finished";
            case NONE:
                return "none";
            default:
                return "Unexpected Mode !!!";
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
