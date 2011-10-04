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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ow2.proactive.scheduler.gui.actions.JMXActionsManager;
import org.ow2.proactive.scheduler.gui.actions.ShowRuntimeDataAction;

public abstract class JMXAbstractHandler extends AbstractHandler {
	
	private Action action; 

	public JMXAbstractHandler()
	{
		JMXActionsManager.getInstance().addHandler(this);
		this.setBaseEnabled(false);
	}
	
	
	@Override
	public void setEnabled(Object context)
	{
		if (context instanceof Boolean)
			this.setBaseEnabled(((Boolean)context).booleanValue());
	}
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (action == null)
		{
			try {
				action = createAction();
			} catch (Exception e) {
				e.printStackTrace();
	            MessageDialog.openError(Display.getDefault().getActiveShell(), "Unable to create the action " +
              ShowRuntimeDataAction.NAME, e.getMessage());
	          return null;
			}
		}
		action.run();
		return null;
	}
	
	
	protected abstract Action createAction() throws Exception;
}
