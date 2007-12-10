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
package org.objectweb.proactive.extensions.scheduler.gui.views;

import java.awt.Frame;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extensions.scheduler.common.task.util.ResultDescriptorTool.SimpleTextPanel;


/**
 * @author FRADJ Johann
 */
public class ResultPreview extends ViewPart {

    /** an id */
    public static final String ID = "org.objectweb.proactive.extensions.scheduler.gui.views.ResultPreview";

    // The shared instance
    private static Composite parent = null;
    private static ResultPreview instance = null;
    private static boolean isDisposed = true;
    private static Composite root;
    private static Frame container;
    private static JPanel toBeDisplayed;
    private static JScrollPane scrollableContainer;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * The default constructor
     *
     */
    public ResultPreview() {
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
    public static ResultPreview getInstance() {
        if (isDisposed) {
            return null;
        }
        return instance;
    }

    public void update(JPanel tbd) {
        if (tbd != toBeDisplayed) {
            toBeDisplayed = tbd;
            container.removeAll();
            scrollableContainer = new JScrollPane(toBeDisplayed);
            container.add(scrollableContainer);
            toBeDisplayed.revalidate();
        }
        container.repaint();
        root.pack();
        root.redraw();
        root.update();
        root.getParent().layout();
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
        root.pack();
        parent.pack();
        this.update(new SimpleTextPanel("No selected task"));
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // TODO petit problème, Eclipse envoi 3 fois d'afiler le mm event
        // setFocus quand la fenêtre a une fenetre "onglet" voisine...
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        isDisposed = true;
        super.dispose();
    }
}
