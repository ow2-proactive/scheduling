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
package org.ow2.proactive.resourcemanager.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.ow2.proactive.resourcemanager.gui.dialog.CreateSourceDialog;


/**
 * @author The ProActive Team
 * 
 */
public class CreateSourceAction extends Action {
    public static final boolean ENABLED_AT_CONSTRUCTION = false;
    private static CreateSourceAction instance = null;
    private Shell shell = null;

    private CreateSourceAction(Composite parent) {
        this.shell = parent.getShell();
        this.setText("Create node source");
        this.setToolTipText("To create a static or dynamic source");
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "icons/create_source.gif"));
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    @Override
    public void run() {
        CreateSourceDialog.showDialog(shell);
    }

    public static CreateSourceAction newInstance(Composite parent) {
        instance = new CreateSourceAction(parent);
        return instance;
    }

    public static CreateSourceAction getInstance() {
        return instance;
    }
}
