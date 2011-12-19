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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
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
package org.ow2.proactive.scheduler.gui.perspective;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.gui.handlers.SchedulerStatusSourceProvider;


/**
 * Listens for perspective change Event and Updates the SchedulerPerspectiveSourceProvider 
 * @author esalagea
 *
 */
public class SchedulerPerspectiveAdapter extends PerspectiveAdapter {

    @Override
    public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspectiveDescriptor) {
        super.perspectiveActivated(page, perspectiveDescriptor);
        if (isSchedulerPerspective(perspectiveDescriptor)) {
            updatePerspectiveState(true);
        }
    }

    @Override
    public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspectiveDescriptor) {
        if (isSchedulerPerspective(perspectiveDescriptor)) {
            updatePerspectiveState(false);
        }
    }

    private void updatePerspectiveState(boolean isActive) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        ISourceProviderService service = (ISourceProviderService) window
                .getService(ISourceProviderService.class);

        SchedulerPerspectiveSourceProvider sourceProvider = (SchedulerPerspectiveSourceProvider) service
                .getSourceProvider(SchedulerPerspectiveSourceProvider.schedulerPerspectiveKey);
        sourceProvider.perspectiveChanged(isActive);

        SchedulerStatusSourceProvider sourceProvider1 = (SchedulerStatusSourceProvider) service
                .getSourceProvider(SchedulerStatusSourceProvider.CAN_START_SCHEDULER);
        sourceProvider1.perspectiveChanged(isActive);

        SchedulerProxy.getInstance().setActivityIndicatorState(isActive);
    }

    private boolean isSchedulerPerspective(IPerspectiveDescriptor perspectiveDescriptor) {
        return perspectiveDescriptor.getId().equals(
                "org.ow2.proactive.scheduler.gui.perspective.SchedulerPerspective");
    }

}
