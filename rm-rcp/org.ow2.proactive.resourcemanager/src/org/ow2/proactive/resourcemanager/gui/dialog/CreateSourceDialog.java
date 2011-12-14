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
package org.ow2.proactive.resourcemanager.gui.dialog;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.ResourceManagerProxy;
import org.ow2.proactive.resourcemanager.gui.data.SupportedPluginDescriptors;
import org.ow2.proactive.resourcemanager.gui.dialog.nodesources.ConfigurablePanel;
import org.ow2.proactive.resourcemanager.gui.dialog.nodesources.NodeSourceName;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;


/**
 * This class allow to pop up a dialogue to remove a source node.
 *
 * @author The ProActive Team
 */
public class CreateSourceDialog {

    private Shell dialog;
    private NodeSourceName name;
    private ConfigurablePanel infrastructure;
    private ConfigurablePanel policy;
    private ScrolledComposite scroll = null;
    private Composite view = null;

    private CreateSourceDialog(Shell parent, 
    		SupportedPluginDescriptors infrastructuresAndPolicies) {

        ProActiveConfiguration.load();

        Display display = parent.getDisplay();

        dialog = new Shell(parent.getDisplay(), SWT.DIALOG_TRIM | SWT.RESIZE);
        dialog.setText("Create a node source");
        GridLayout shellLayout = new GridLayout(1, false);
        shellLayout.marginLeft = 0;
        shellLayout.marginRight = 0;
        shellLayout.horizontalSpacing = 0;
        shellLayout.marginWidth = 0;
        dialog.setLayout(shellLayout);

        scroll = new ScrolledComposite(dialog, SWT.H_SCROLL | SWT.V_SCROLL);
        scroll.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_BOTH));

        view = new Composite(scroll, SWT.NONE);
        GridLayout l = new GridLayout(1, false);
        view.setLayout(l);

        scroll.setExpandHorizontal(true);
        scroll.setExpandVertical(true);
        scroll.setContent(view);

        // creation
        name = new NodeSourceName(view, SWT.NONE);
        name.setLayoutData(new GridData(GridData.BEGINNING));

        infrastructure = new ConfigurablePanel(view, "Node source infrastructure", this);
        infrastructure.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_BOTH));

        policy = new ConfigurablePanel(view, "Node source policy", this);
        policy.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_BOTH));


        for (PluginDescriptor descriptor : infrastructuresAndPolicies.getSupportedNodeSourceInfrastructures()) {
            try {
                infrastructure.addComboValue(descriptor);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        Collection<PluginDescriptor> policies = infrastructuresAndPolicies.getSupportedNodeSourcePolicies();

        for (PluginDescriptor descriptor : policies) {
            policy.addComboValue(descriptor);
        }

        final Label sep = new Label(dialog, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gs = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
        gs.horizontalSpan = 2;
        sep.setLayoutData(gs);

        Composite buttons = new Composite(dialog, 0);
        GridData gb = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END);
        buttons.setLayoutData(gb);
        RowLayout rl = new RowLayout(SWT.HORIZONTAL);
        rl.marginRight = 20;
        rl.spacing = 20;
        buttons.setLayout(rl);

        final Button cancelButton = new Button(buttons, SWT.PUSH);
        cancelButton.setText("   Cancel   ");
        cancelButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                dialog.close();
            }
        });

        final Button okButton = new Button(buttons, SWT.PUSH);
        okButton.setText("     OK     ");
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
            	validateForm();
                ResourceManagerProxy rm = RMStore.getInstance().getResourceManager();
                try {
                    Object[] policyParams = policy.getParameters();
                    boolean result = rm.syncCreateNodeSource(name.getNodeSourceName(),
                            infrastructure.getSelectedPlugin().getPluginName(),
                            infrastructure.getParameters(), policy.getSelectedPlugin().getPluginName(),
                            policyParams);
                    if (result) {
                        dialog.close();
                    } else {
                    	rm.displayError("Unknown reason", "Cannot create nodesource");
                    }
                } catch (Exception e) {
                	rm.logAndDisplayError(e, "Cannot create nodesource");
                }
            }
        });

        scroll.setMinSize(view.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        dialog.pack();
        dialog.open();
        dialog.setMinimumSize(100, 100);

        while (!dialog.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    public void repack() {
        scroll.setMinSize(view.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        view.pack();

        Point pref = dialog.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        int h = Display.getCurrent().getPrimaryMonitor().getBounds().height;
        int rh = Math.min(pref.y, h * 4 / 5);
        dialog.setSize(pref.x, rh);
    }

    /**
     * This method pop up a dialog for trying to connect a resource manager.
     *
     * @param parent the parent
     */
    public static void showDialog(final Shell parent) {
        Job job = new Job("Initializing dialog.") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
		        ResourceManagerProxy rm = RMStore.getInstance().getResourceManager();
		        try {
		            final SupportedPluginDescriptors infrastructuresAndPolicies = rm.syncGetSupportedPluginDescriptors(); 
		        	parent.getDisplay().asyncExec(new Runnable() {
		        		public void run() {
			            	new CreateSourceDialog(parent, infrastructuresAndPolicies);
		        		}
		        	});
					return Status.OK_STATUS;
		        } catch (Exception e) {
		            rm.logAndDisplayError(e, "Cannot create nodesource");
		            return Status.OK_STATUS;
		        }
			}
        };
        job.setUser(true);
        job.schedule();
    }

    private void validateForm() {
        if (name.getNodeSourceName().length() == 0) {
            throw new RuntimeException("Node source name cannot be empty");
        }

        if (infrastructure.getSelectedPlugin() == null) {
            throw new RuntimeException("Select node source infrastructure type");
        }

        if (policy.getSelectedPlugin() == null) {
            throw new RuntimeException("Select node source policy type");
        }
    }
}
