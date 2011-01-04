/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.gui.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.ow2.proactive.resourcemanager.gui.data.model.Describable;
import org.ow2.proactive.resourcemanager.gui.data.model.Selectable;


/**
 * The class responsible for handling a "describe node" commande event
 */
public class DescribeCommandHandler extends AbstractHandler implements IHandler {

    /** the previous state of the command*/
    private boolean previousState = false;
    /** the static instance */
    private static DescribeCommandHandler instance;
    /** the list of the selected nodes */
    private List<Describable> selectedNodes = null;

    public DescribeCommandHandler() {
        super();
        instance = this;
    }

    /**
     * @return returns the describecommandhandler instance
     */
    public static DescribeCommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean isEnabled() {
        boolean enabled = false;
        if (selectedNodes != null && selectedNodes.size() == 1) {
            enabled = true;
        }
        //hack for toolbar menu (bug?), force event throwing if state changed.
        // Otherwise command stills disabled in toolbar menu.
        //No mood to implement callbacks to static field of my handler
        //to RMStore, just do business code
        //and let RCP API manages buttons...
        if (previousState != enabled) {
            previousState = enabled;
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
        return enabled;
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (selectedNodes.size() == 1) {
            Describable pn = selectedNodes.get(0);
            Clipboard clipboard = new Clipboard(Display.getDefault());
            TextTransfer tt = TextTransfer.getInstance();
            clipboard.setContents(new Object[] { pn.getDescription() }, new Transfer[] { tt });
            MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Node Information", pn
                    .getDescription());
        }
        return null;
    }

    public void setSelectedNodes(List<Selectable> selectedNodes) {
        List<Describable> toCopy = new ArrayList<Describable>();
        for (Selectable selected : selectedNodes) {
            if (selected instanceof Describable) {
                toCopy.add((Describable) selected);
            }
        }
        this.selectedNodes = toCopy;
        if (!previousState && selectedNodes.size() == 1) {
            fireHandlerChanged(new HandlerEvent(this, true, false));
        } else if (previousState && selectedNodes.size() != 1) {
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
    }
}
