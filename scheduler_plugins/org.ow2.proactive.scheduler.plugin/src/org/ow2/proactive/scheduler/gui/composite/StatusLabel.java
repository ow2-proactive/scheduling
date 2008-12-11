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
package org.ow2.proactive.scheduler.gui.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.ow2.proactive.scheduler.gui.Colors;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.listeners.EventSchedulerListener;


/**
 * @author The ProActive Team
 */
public class StatusLabel implements EventSchedulerListener {
    public static final int FONT_SIZE = 10;
    public static final int FONT_STYLE = SWT.BOLD;
    public static final String INITIAL_TEXT = "right click -> connect to scheduler...";
    public static final Color INITIAL_COLOR = Colors.BLACK;
    public static final String STARTED_TEXT = "STARTED";
    public static final Color STARTED_COLOR = Colors.BLACK;
    public static final String STOPPED_TEXT = "STOPPED";
    public static final Color STOPPED_COLOR = Colors.BLACK;
    public static final String FREEZED_TEXT = "FREEZED";
    public static final Color FREEZED_COLOR = Colors.BLACK;
    public static final String PAUSED_TEXT = "PAUSED";
    public static final Color PAUSED_COLOR = Colors.BLACK;
    public static final String RESUMED_TEXT = "RESUMED";
    public static final Color RESUMED_COLOR = Colors.BLACK;
    public static final String SHUTTING_DOWN_TEXT = "SHUTTING DOWN";
    public static final Color SHUTTING_DOWN_COLOR = Colors.BLACK;
    public static final String SHUTDOWN_TEXT = "SHUTDOWN";
    public static final Color SHUTDOWN_COLOR = Colors.BLACK;
    public static final String KILLED_TEXT = "KILLED";
    public static final Color KILLED_COLOR = Colors.BLACK;
    public static final String DISCONNECTED_TEXT = "DISCONNECTED";
    public static final Color DISCONNECTED_COLOR = Colors.BLACK;
    private static StatusLabel instance = null;
    private Label label = null;

    private StatusLabel(Composite parent, GridData gridData, JobsController jobsController) {
        label = new Label(parent, SWT.CENTER | SWT.BORDER);
        label.setText(INITIAL_TEXT);
        label.setForeground(INITIAL_COLOR);
        label.setFont(new Font(Display.getDefault(), "", FONT_SIZE, FONT_STYLE));
        label.setLayoutData(gridData);
        jobsController.addEventSchedulerListener(this);
    }

    public static void newInstance(Composite parent, GridData gridData, JobsController jobsController) {
        instance = new StatusLabel(parent, gridData, jobsController);
    }

    public static StatusLabel getInstance() {
        return instance;
    }

    public void freezeEvent() {
        setText(FREEZED_TEXT, FREEZED_COLOR);
    }

    public void killedEvent() {
        setText(KILLED_TEXT, KILLED_COLOR);
    }

    public void pausedEvent() {
        setText(PAUSED_TEXT, PAUSED_COLOR);
    }

    public void resumedEvent() {
        setText(RESUMED_TEXT, RESUMED_COLOR);
    }

    public void shutDownEvent() {
        setText(SHUTDOWN_TEXT, SHUTDOWN_COLOR);
    }

    public void shuttingDownEvent() {
        setText(SHUTTING_DOWN_TEXT, SHUTTING_DOWN_COLOR);
    }

    public void startedEvent() {
        setText(STARTED_TEXT, STARTED_COLOR);
    }

    public void stoppedEvent() {
        setText(STOPPED_TEXT, STOPPED_COLOR);
    }

    public void disconnect() {
        setText(DISCONNECTED_TEXT, DISCONNECTED_COLOR);
    }

    private void setText(String aText, Color aColor) {
        final String text = aText;
        final Color color = aColor;
        if (!label.isDisposed()) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    label.setForeground(color);
                    label.setText(text);
                }
            });
        }
    }
}
