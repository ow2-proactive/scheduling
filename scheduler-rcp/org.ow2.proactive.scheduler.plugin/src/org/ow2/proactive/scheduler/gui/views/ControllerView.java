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
package org.ow2.proactive.scheduler.gui.views;

import java.awt.Color;
import java.awt.Frame;

import javax.swing.JScrollPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.util.userconsole.UserSchedulerModel;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.util.adminconsole.AdminSchedulerModel;
import org.ow2.proactive.utils.console.VisualConsole;


/**
 * Controller console view for this RCP
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class ControllerView extends ViewPart {

    /** an id */
    public static final String ID = "org.ow2.proactive.scheduler.gui.views.ControllerView";

    private static ControllerView instance = null;
    private Frame container;
    private VisualConsole console;
    private JScrollPane scrollableContainer;
    private UserSchedulerInterface scheduler;

    public ControllerView() {
        instance = this;
        console = new VisualConsole(50, Color.GREEN, Color.WHITE, Color.BLACK, 3, false);
    }

    public static ControllerView getInstance() {
        if (instance == null) {
            instance = new ControllerView();
        }
        return instance;
    }

    private void clean() {
        if (container != null) {
            console.stop();
            container.removeAll();
            container.pack();
        }
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite cparent) {
        if (container == null) {
            container = SWT_AWT.new_Frame(new Composite(cparent, SWT.EMBEDDED));
        }

        if (SchedulerProxy.getInstance().isProxyConnected()) {
            //scheduler is connected

            scheduler = SchedulerProxy.getInstance();

            UserSchedulerModel model;
            if (SchedulerProxy.getInstance().isAnAdmin()) {
                model = AdminSchedulerModel.getModel(false);
            } else {
                model = UserSchedulerModel.getModel(false);
            }

            startConsole(model);

            scrollableContainer = new JScrollPane(console.getJContentPane());
            container.add(scrollableContainer);
        }
    }

    private void startConsole(final UserSchedulerModel model) {
        model.connectConsole(console);
        model.connectScheduler(scheduler);
        try {
            new Thread() {
                @Override
                public void run() {
                    try {
                        model.startModel();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectedEvent(boolean isAdmin) {
        scheduler = SchedulerProxy.getInstance();

        UserSchedulerModel model;
        if (isAdmin) {
            model = AdminSchedulerModel.getNewModel(false);
        } else {
            model = UserSchedulerModel.getNewModel(false);
        }

        startConsole(model);

        if (container != null) {
            scrollableContainer = new JScrollPane(console.getJContentPane());
            container.add(scrollableContainer);
            container.repaint();
            container.pack();
        }
    }

    @Override
    public void setFocus() {
    }

    public static void clearInstance() {
        instance.clean();
        //instance = null;
    }

}
