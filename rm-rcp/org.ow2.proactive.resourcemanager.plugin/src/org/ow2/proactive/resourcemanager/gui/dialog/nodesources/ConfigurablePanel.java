/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.dialog.nodesources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.gui.dialog.CreateCredentialDialog;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableField;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;


public class ConfigurablePanel extends Group {

    class Property extends Composite {

        private Label nameLabel;
        private Text text;
        private Label descriptionLabel;
        private Object value;

        public Property(Composite parent, ConfigurableField configurableField) {
            super(parent, SWT.LEFT);

            String name = configurableField.getName();
            Configurable configurable = configurableField.getMeta();
            String description = configurable.description();

            setLayout(new FormLayout());

            nameLabel = new Label(this, SWT.LEFT);
            nameLabel.setText(PluginDescriptor.beautifyName(name));

            int passwdMask = configurableField.getMeta().password() ? SWT.PASSWORD : 0;
            text = new Text(this, SWT.LEFT | SWT.BORDER | passwdMask);
            text.setText(configurableField.getValue());

            FormData fd = new FormData();
            fd.top = new FormAttachment(1, 5);
            fd.left = new FormAttachment(1, 5);
            fd.width = 140;
            nameLabel.setLayoutData(fd);

            fd = new FormData();
            fd.left = new FormAttachment(nameLabel, 5);
            fd.width = 200;
            text.setLayoutData(fd);

            if (configurableField.getMeta().fileBrowser() || configurableField.getMeta().credential()) {
                Button chooseButton = new Button(this, SWT.NONE);
                chooseButton.setText("Choose file");
                chooseButton.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        FileDialog fileDialog = new FileDialog(ConfigurablePanel.this.parent, SWT.OPEN);
                        String fileName = fileDialog.open();
                        if (fileName != null)
                            text.setText(fileName);
                    }
                });

                FormData chooseFormData = new FormData();
                chooseFormData.left = new FormAttachment(text, 5);
                chooseButton.setLayoutData(chooseFormData);

                if (configurableField.getMeta().credential()) {
                    Button createCredentialButton = new Button(this, SWT.NONE);
                    createCredentialButton.setText("Create");
                    createCredentialButton.addListener(SWT.Selection, new Listener() {
                        public void handleEvent(Event event) {
                            CreateCredentialDialog dialog = new CreateCredentialDialog(
                                ConfigurablePanel.this.parent, null);
                            value = dialog.getCredentials();
                            text.setText("<generated>");
                        }
                    });

                    FormData createCredentialFormData = new FormData();
                    createCredentialFormData.left = new FormAttachment(chooseButton, 5);
                    createCredentialButton.setLayoutData(createCredentialFormData);
                }

            } else if (description != null && description.length() > 0) {
                descriptionLabel = new Label(this, SWT.LEFT);
                descriptionLabel.setText(description);

                fd = new FormData();
                fd.top = new FormAttachment(1, 5);
                fd.left = new FormAttachment(text, 5);
                descriptionLabel.setLayoutData(fd);
            }

            pack();
        }

        public Object getValue() {
            return value == null ? text.getText() : value;
        }
    }

    private Combo combo;
    private List<Property> properties = new LinkedList<Property>();
    private Label description;
    private PluginDescriptor selectedDescriptor = null;
    private HashMap<String, PluginDescriptor> comboStates = new HashMap<String, PluginDescriptor>();
    private Shell parent;

    public ConfigurablePanel(final Shell parent, String labelText) {
        super(parent, SWT.NONE);

        this.parent = parent;
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        this.setLayout(layout);

        setText(labelText);
        Label typeLabel = new Label(this, SWT.NONE);
        typeLabel.setText("Type : ");

        combo = new Combo(this, SWT.READ_ONLY);
        description = new Label(this, SWT.NONE);

        FormData fd = new FormData();
        fd.top = new FormAttachment(1, 3);
        typeLabel.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(typeLabel, 5);
        fd.width = 200;
        combo.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(typeLabel, 10);
        description.setLayoutData(fd);

        combo.add("");
        comboStates.put("", null);

        combo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                for (Property l : properties) {
                    l.dispose();
                }
                properties.clear();
                parent.pack();

                designGui(comboStates.get(combo.getText()));
                parent.pack();
            }
        });
    }

    public void addComboValue(PluginDescriptor descriptor) {
        String pluginName = PluginDescriptor.beautifyName(descriptor.getPluginName());
        combo.add(pluginName);
        comboStates.put(pluginName, descriptor);
    }

    private void designGui(PluginDescriptor descriptor) {
        selectedDescriptor = descriptor;
        if (descriptor == null) {
            description.setText("");
            return;
        }

        for (ConfigurableField configurableField : descriptor.getConfigurableFields()) {
            Property property = new Property(this, configurableField);
            Control lowest = properties.size() > 0 ? properties.get(properties.size() - 1) : this.description;
            FormData fd = new FormData();
            fd.top = new FormAttachment(lowest, 10);
            property.setLayoutData(fd);

            properties.add(property);
        }

        if (descriptor.getPluginDescription() != null) {
            description.setText(descriptor.getPluginDescription());
            description.pack();
        }

    }

    protected void checkSubclass() {
    }

    public Object[] getParameters() throws RMException {
        List<Object> params = new ArrayList<Object>();

        for (Property p : properties) {
            params.add(p.getValue());
        }

        if (selectedDescriptor == null) {
            throw new RMException("Incorrect plugin selection");
        }
        return selectedDescriptor.packParameters(params.toArray());
    }

    public PluginDescriptor getSelectedPlugin() {
        return selectedDescriptor;
    }
}
