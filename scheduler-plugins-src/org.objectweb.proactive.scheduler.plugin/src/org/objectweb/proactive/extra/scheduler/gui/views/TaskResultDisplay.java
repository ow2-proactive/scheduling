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

import java.awt.Frame;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class TaskResultDisplay extends ViewPart {

    /** an id */
    public static final String ID = "org.objectweb.proactive.extra.scheduler.gui.views.TaskResult";

    // The shared instance
    private static Composite parent = null;
    private static TaskResultDisplay instance = null;
    private static boolean isDisposed = true;
    private static Composite root;
    private static Frame container;
    private static JPanel toBeDisplayed;
    private static JScrollPane scrollableContainer;
    private static boolean hasBeenModified;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * The default constructor
     *
     */
    public TaskResultDisplay() {
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
    public static TaskResultDisplay getInstance() {
        if (isDisposed) {
            return null;
        }
        return instance;
    }

    public void update(JPanel tbd) {
        System.out.println("TaskResultDisplay.update()");
        try {
            toBeDisplayed = tbd;
            if (toBeDisplayed != null) { //test if TBD has changed...
                container.removeAll();
                container.repaint();
                scrollableContainer = new JScrollPane(toBeDisplayed);
                container.add(scrollableContainer);
                container.pack();
                container.repaint();
                scrollableContainer.repaint();
                root.redraw();
            } else {
                //SHOULD DISPLAY SOMETHING...
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
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

        root = new Composite(parent, SWT.EMBEDDED);
        root.setVisible(true);
        container = SWT_AWT.new_Frame(root);
        container.pack();
        container.setVisible(true);
        parent.pack();

        //SimpleImagePanel img = new SimpleImagePanel("/user/cdelbe/home/mkrisJeune.jpg");

        //        SimpleTextPanel img = new SimpleTextPanel("TEST : This is a test message");
        //        this.update(img);
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
        try {
            //container.repaint();
            //scrollableContainer.repaint();
            root.redraw();
        } catch (RuntimeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        isDisposed = true;
        //        if (image != null) {
        //            image.dispose();
        //            image = null;
        //        }
        super.dispose();
    }
}
