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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import org.ow2.proactive.resourcemanager.gui.dialog.CreateSourceDialog;
import org.ow2.proactive.resourcemanager.nodesource.policy.Configurable;
import org.ow2.proactive.utils.FileToBytesConverter;


public class ConfigurablePanel extends Group {

    class Property extends Composite {

        private Label nameLabel;
        private Text text;
        private Label descriptionLabel;
        boolean isFile = false;

        public Property(Composite parent, Field f, Object instance) throws Exception {
            super(parent, SWT.LEFT);
            String name = f.getName();
            f.setAccessible(true);
            Object valueObj = f.get(instance);
            String value = valueObj == null ? "" : valueObj.toString();

            Configurable configurable = f.getAnnotation(Configurable.class);
            String description = configurable.description();
            boolean isPasswd = configurable.password();
            isFile = configurable.fileBrowser();

            setLayout(new FormLayout());

            nameLabel = new Label(this, SWT.LEFT);
            nameLabel.setText(CreateSourceDialog.beautifyName(name));

            int passwdMask = isPasswd ? SWT.PASSWORD : 0;
            text = new Text(this, SWT.LEFT | SWT.BORDER | passwdMask);
            text.setText(value);

            FormData fd = new FormData();
            fd.top = new FormAttachment(1, 5);
            fd.left = new FormAttachment(1, 5);
            fd.width = 100;
            nameLabel.setLayoutData(fd);

            fd = new FormData();
            fd.left = new FormAttachment(nameLabel, 5);
            fd.width = 200;
            text.setLayoutData(fd);

            if (isFile) {
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

        public String getValue() {
            return text.getText();
        }
    }

    private Combo combo;
    private List<Property> properties = new LinkedList<Property>();
    private Label description;
    private Class<?> selectedClass = null;
    private HashMap<String, Class<?>> comboStates = new HashMap<String, Class<?>>();
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

        combo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                for (Property l : properties) {
                    l.dispose();
                }
                properties.clear();
                parent.pack();

                Class<?> cls = comboStates.get(combo.getText());
                setClass(cls);
                parent.pack();
            }
        });
    }

    public void addComboValue(String name, Class<?> value) {
        combo.add(name);
        comboStates.put(name, value);
    }

    private void setClass(Class<?> cls) {
        selectedClass = cls;
        if (cls == null) {
            description.setText("");
            return;
        }

        try {
            Object instance = cls.newInstance();

            List<Field> fields = new LinkedList<Field>();
            findFileds(cls, fields);
            for (Field f : fields) {
                Property property = new Property(this, f, instance);
                Control lowest = properties.size() > 0 ? properties.get(properties.size() - 1)
                        : this.description;
                FormData fd = new FormData();
                fd.top = new FormAttachment(lowest, 10);
                property.setLayoutData(fd);

                properties.add(property);
            }

            Method getDescription = cls.getMethod("getDescription");
            if (getDescription != null) {
                description.setText((String) getDescription.invoke(instance));
                description.pack();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findFileds(Class<?> cls, List<Field> fields) {
        if (cls.getSuperclass() != null && cls.getSuperclass() != Object.class) {
            findFileds(cls.getSuperclass(), fields);
        }

        for (Field f : cls.getDeclaredFields()) {
            if (f.getAnnotation(Configurable.class) != null) {
                fields.add(f);
            }
        }
    }

    protected void checkSubclass() {
    }

    public Object[] getParameters() {
        List<Object> params = new ArrayList<Object>();
        for (Property p : properties) {
            if (p.isFile) {
                try {
                    params.add(FileToBytesConverter.convertFileToByteArray(new File(p.getValue())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                params.add(p.getValue());
            }
        }
        return params.toArray();
    }

    public Class<?> getSelectedClass() {
        return selectedClass;
    }
}
