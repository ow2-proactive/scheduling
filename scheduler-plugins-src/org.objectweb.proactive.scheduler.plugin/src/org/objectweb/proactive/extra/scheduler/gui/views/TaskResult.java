/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://proactive.inria.fr/team_members.htm Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.scheduler.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalTask;


public class TaskResult extends ViewPart {

    /** an id */
    public static final String ID = "org.objectweb.proactive.extra.scheduler.gui.views.TaskResult";

    // The shared instance
    private static Composite parent = null;
    private static TaskResult instance = null;
    private static boolean isDisposed = true;
    private static Canvas canvas = null;
    private static Image image = null;

    // FIXME à suppr
    private static int nb = 0;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * The default constructor
     *
     */
    public TaskResult() {
        instance = this;
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- public ------------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static TaskResult getInstance() {
        if (isDisposed) {
            return null;
        }
        return instance;
    }

    public void update(InternalTask task) {
        System.out.println("Update de l'image ==> ");
        image = new Image(parent.getDisplay(), nb + ".jpg");
        nb++;
        nb %= 3;
        canvas.redraw();
    }

    // -------------------------------------------------------------------- //
    // ------------------------- extends viewPart ------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite theParent) {
        parent = theParent;
        isDisposed = false;

        canvas = new Canvas(parent, SWT.BORDER);

        canvas.addPaintListener(new PaintListener() {
                public void paintControl(final PaintEvent event) {
                    if (image != null) {
                        event.gc.drawImage(image, 0, 0);
                    }
                }
            });
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // TODO petit problème, Eclipse envoi 3 fois d'afiler le mm event
        // setFocus quand la fenêtre a une fenetre "onglet" voisine...

        // TableManager tableManager = TableManager.getInstance();
        // if (tableManager != null) {
        // TableItem item = tableManager.getLastSelectedItem();
        // if (item != null)
        // updateInfos(JobsController.getInstance().getJobById((IntWrapper)
        // item.getData()));
        // }
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        isDisposed = true;
        if (image != null) {
            image.dispose();
            image = null;
        }
        super.dispose();
    }
}
