/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.actions;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.job.factories.XMLAttributes;
import org.ow2.proactive.scheduler.common.job.factories.XMLTags;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.data.DataServers;
import org.ow2.proactive.scheduler.gui.data.DataServers.Server;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author The ProActive Team
 */
public class SubmitJobAction extends SchedulerGUIAction {
    private Composite parent = null;
    private String lastDirectory = null;

    /** true if this Action should propose variable edition upon submission */
    private boolean editVariables = false;

    /** internal flag to keep track of 'Edit Variables' dialog status:
     * should be true when visible, false when hidden */
    private boolean editingVariables = false;
    /** internal flag to keep track of the 'Edit Variables' return status :
     * true for OK button, false for Cancel / close */
    private boolean dialogReturn = false;

    public SubmitJobAction(Composite parent, boolean editVariables) {
        this.editVariables = editVariables;
        this.parent = parent;
        if (editVariables) {
            this.setText("Submit and edit variables");
            this.setToolTipText("Submit job from an XML file and edit the variables definitions");
        } else {
            this.setText("Submit an XML job file");
            this.setToolTipText("Submit job from an XML file containing a job description");
        }
        this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_JOBSUBMIT));
        this.setEnabled(false);
    }

    /*
     * Data Structure that holds info throughout the variable edition process of a descriptor 
     *
     */
    private static class VarMap {
        /** file path to the original job descriptor */
        public String originalFile = null;
        /** variable name -> value bindings */
        public Map<String, String> vars = null;
        /** DOM : keep the same object for reading/writing */
        public Document doc = null;
        /** temporary file with edited variables for submission */
        public File out = null;
        /** true if variables were actually edited */
        public boolean changed = false;

        public VarMap() {
            // LinkedHashMap : preserve insertion order 
            this.vars = new LinkedHashMap<String, String>();
        }
    }

    /*
     * for each descriptor to submit :
     * open it, search for variables, for each variable
     * propose edition in a modal dialog, then write the
     * new edited descriptor in a temp file and submit
     * this descriptor.
     * 
     */
    private List<VarMap> editDescriptorVariables(final String[] files) {

        final List<VarMap> variables = new ArrayList<VarMap>();

        for (String file : files) {
            Document doc = null;
            // Build the document
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                doc = docBuilder.parse(file);
            } catch (Exception e) {
                String msg = "Failed to parse descriptor " + file + ":\n" + e.getMessage();
                ErrorDialog.openError(parent.getShell(), "Edit Variables", msg, null);
                Activator.log(IStatus.ERROR, msg, e);
                return null;
            }
            // extract the variables
            try {
                VarMap varMap = new VarMap();
                varMap.doc = doc;
                varMap.originalFile = file;
                variables.add(varMap);

                Node vars = doc.getElementsByTagName(XMLTags.VARIABLES.toString()).item(0);

                doc.getElementsByTagName(XMLTags.TASK.toString());

                if (vars != null) {
                    NodeList varChildren = vars.getChildNodes();
                    for (int i = 0; i < varChildren.getLength(); i++) {
                        Node var = varChildren.item(i);
                        if (var != null) {
                            if (var.getAttributes() != null) {

                                String name = null, value = null;
                                for (int j = 0; j < var.getAttributes().getLength(); j++) {
                                    Node attr = var.getAttributes().item(j);

                                    if (attr.getNodeName().equals(XMLAttributes.COMMON_NAME.toString())) {
                                        name = attr.getNodeValue();
                                    }
                                    if (attr.getNodeName().equals(XMLAttributes.COMMON_VALUE.toString())) {
                                        value = attr.getNodeValue();
                                    }
                                }
                                varMap.vars.put(name, value);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                String msg = "Error while reading variables in '" + file + "':\n" + e.getMessage();
                ErrorDialog.openError(parent.getShell(), "Edit Variables", msg, null);
                Activator.log(IStatus.ERROR, msg, e);
                return null;
            }
        }

        popupVariablesDialog(variables, files);

        if (!dialogReturn) {
            return null;
        }

        for (VarMap varMap : variables) {
            if (!varMap.changed)
                continue;

            // write the variables
            try {
                Node vars = varMap.doc.getElementsByTagName(XMLTags.VARIABLES.toString()).item(0);
                NodeList varChildren = vars.getChildNodes();
                for (int i = 0; i < varChildren.getLength(); i++) {
                    Node var = varChildren.item(i);
                    if (var != null) {
                        if (var.getAttributes() != null) {

                            String name = null;
                            Node value = null;
                            for (int j = 0; j < var.getAttributes().getLength(); j++) {
                                Node attr = var.getAttributes().item(j);

                                if (attr.getNodeName().equals(XMLAttributes.COMMON_NAME.toString())) {
                                    name = attr.getNodeValue();
                                }
                                if (attr.getNodeName().equals(XMLAttributes.COMMON_VALUE.toString())) {
                                    value = attr;
                                }
                            }

                            String match = varMap.vars.get(name);
                            if (match != null) {
                                value.setNodeValue(match);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                String msg = "Error while writing variables for '" + varMap.originalFile + "':\n" +
                    e.getMessage();
                ErrorDialog.openError(parent.getShell(), "Edit Variables", msg, null);
                Activator.log(IStatus.ERROR, msg, e);
                return null;
            }
            // write the document
            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                File out = File.createTempFile("descriptor_", ".xml");
                out.deleteOnExit();
                varMap.out = out;
                StreamResult result = new StreamResult(new FileWriter(out));
                DOMSource source = new DOMSource(varMap.doc);
                transformer.transform(source, result);

            } catch (Exception e) {
                String msg = "Error while writing descriptor to '" + varMap.out.getAbsolutePath() + "':\n" +
                    e.getMessage();
                ErrorDialog.openError(parent.getShell(), "Edit Variables", msg, null);
                Activator.log(IStatus.ERROR, msg, e);
                return null;
            }
        }

        return variables;
    }

    /*
     * pops a modal dialog for editing the variables of a list of descriptors
     * 
     */
    private void popupVariablesDialog(final List<VarMap> variables, final String[] files) {
        // true as long as the dialog is shown
        editingVariables = true;
        // false unless the 'OK' button is clicked
        dialogReturn = false;

        Display.getDefault().asyncExec(new Runnable() {
            // create the dialog in SWT's gui thread
            public void run() {
                final Shell dialog = new Shell(parent.getDisplay(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL |
                    SWT.RESIZE);
                dialog.setText("Edit variables");

                // Grid Layout : 2 columns
                GridLayout l = new GridLayout(2, false);
                l.marginLeft = 0;
                l.marginRight = 0;
                l.horizontalSpacing = 0;
                l.marginWidth = 0;
                dialog.setLayout(l);

                // little textual description
                Label desc = new Label(dialog, SWT.WRAP);
                desc.setText("For each submitted job, edit the definition of every variable "
                    + "found in the descriptor, or leave the default value.\n");
                GridData dgd = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
                dgd.horizontalSpan = 2;
                desc.setLayoutData(dgd);

                Label comboLabel = new Label(dialog, SWT.NULL);
                comboLabel.setLayoutData(new GridData(GridData.BEGINNING));
                comboLabel.setText("Descriptor: ");

                // Combo : choose the descriptor to edit
                final Combo combo = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
                GridData comboLayout = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
                combo.setLayoutData(comboLayout);
                combo.setItems(files);
                combo.select(0);

                // ScrollPane : will contain the list of variables with 
                // scrolling if there are too many to display
                final ScrolledComposite scroll = new ScrolledComposite(dialog, SWT.H_SCROLL | SWT.V_SCROLL);
                GridData scrolld = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
                scrolld.horizontalSpan = 2;
                scroll.setLayoutData(scrolld);
                scroll.setExpandHorizontal(true);
                scroll.setExpandVertical(true);

                // contains an editor Composite for each file
                final Map<String, Composite> editors = new HashMap<String, Composite>();

                // for each descriptor, create a hidden composite that will be shown when
                // selected in the Combo
                for (VarMap varMap : variables) {
                    Composite comp = new Composite(scroll, SWT.NULL);

                    // 2 columns : label | combo
                    GridLayout gl = new GridLayout(2, false);
                    comp.setLayout(gl);

                    // there are variables : create the content
                    if (varMap.vars.size() > 0) {
                        for (Entry<String, String> vars : varMap.vars.entrySet()) {
                            Label key = new Label(comp, SWT.NULL);
                            key.setLayoutData(new GridData(GridData.BEGINNING));
                            key.setText(vars.getKey());

                            final Combo val = new Combo(comp, SWT.DROP_DOWN);
                            val.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL));

                            // put the DataServers names in the suggestions
                            Map<String, Server> servers = DataServers.getInstance().getServers();
                            String[] items = new String[1 + servers.size()];
                            items[0] = vars.getValue();
                            int i = 1;
                            for (Server srv : servers.values()) {
                                items[i] = srv.getUrl();
                                i++;
                            }
                            val.setItems(items);
                            val.select(0);
                            val.setData(vars.getKey());

                            // update the content of the varMaps when a ComboBox is edited
                            val.addModifyListener(new ModifyListener() {
                                public void modifyText(ModifyEvent e) {
                                    Combo c = (Combo) e.getSource();
                                    String selDesc = combo.getItem(combo.getSelectionIndex());
                                    for (VarMap varMap : variables) {
                                        if (varMap.originalFile.equals(selDesc)) {
                                            varMap.vars.put((String) val.getData(), c.getText());
                                            varMap.changed = true;
                                        }
                                    }
                                }

                            });

                        }
                    }
                    // no variables ; display a little message
                    else {
                        Label msg = new Label(comp, SWT.NULL);
                        msg.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
                        msg.setText("No variable definition");
                    }

                    // setup default content
                    if (combo.getItem(0).equals(varMap.originalFile)) {
                        scroll.setContent(comp);
                        scroll.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                    }

                    editors.put(varMap.originalFile, comp);
                }

                // called when the user selects another descriptor in the combobox
                combo.addSelectionListener(new SelectionListener() {
                    public void widgetSelected(SelectionEvent e) {
                        // find the Composite associated with the descriptor,
                        // show it in the scroll pane
                        Composite comp = editors.get(combo.getItem(combo.getSelectionIndex()));
                        if (comp != null) {
                            scroll.setContent(comp);
                            scroll.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));

                            // pack but restore the same size
                            int x = dialog.getSize().x;
                            int y = dialog.getSize().y;
                            dialog.pack();
                            dialog.setSize(x, y);
                        }
                    }

                    public void widgetDefaultSelected(SelectionEvent e) {
                    }
                });

                // Buttons
                final Label sep = new Label(dialog, SWT.SEPARATOR | SWT.HORIZONTAL);
                GridData gs = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
                gs.horizontalSpan = 2;
                sep.setLayoutData(gs);

                Composite buttons = new Composite(dialog, 0);
                GridData g5 = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END);
                g5.horizontalSpan = 2;
                buttons.setLayoutData(g5);
                buttons.setLayout(new RowLayout(SWT.HORIZONTAL));

                final Button cancelButton = new Button(buttons, SWT.PUSH);
                cancelButton.setText("  Cancel  ");
                cancelButton.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        dialog.close();
                    }
                });

                final Button okButton = new Button(buttons, SWT.PUSH);
                okButton.setText("   OK   ");
                okButton.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        dialogReturn = true;
                        dialog.close();
                    }
                });

                dialog.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        editingVariables = false;
                    }
                });

                dialog.pack();
                dialog.setSize(400, 300);
                dialog.open();
            }
        });

        // sleeping : this is not SWT's thread and we can block here as long as we want
        // closing the dialog will switch this boolean
        while (editingVariables) {
            Thread.yield();
        }
    }

    @Override
    public void run() {
        FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.OPEN | SWT.MULTI);
        fileDialog.setFilterExtensions(new String[] { "*.xml" });
        if (lastDirectory != null) {
            fileDialog.setFilterPath(lastDirectory);
        }
        if (fileDialog.open() == null) {
            return;
        }

        final String[] fileNames = fileDialog.getFileNames();
        final String directoryPath = fileDialog.getFilterPath();
        lastDirectory = directoryPath;

        for (int i = 0; i < fileNames.length; i++) {
            fileNames[i] = directoryPath + File.separator + fileNames[i];
        }

        //create jobs in a worker thread : not GUI.
        Thread worker = new Thread(new Runnable() {
            public void run() {
                if (editVariables) {
                    List<VarMap> varMap = editDescriptorVariables(fileNames);
                    if (varMap == null) {
                        // this means the editoDescriptorVariables() dialog was cancelled
                        return;
                    } else {
                        // replace filename if variables were edited
                        for (VarMap vars : varMap) {
                            for (int i = 0; i < fileNames.length; i++) {
                                if (fileNames[i].equals(vars.originalFile) && vars.changed) {
                                    fileNames[i] = vars.out.getAbsolutePath();
                                }
                            }
                        }
                    }
                }

                //map of submitted jobs, for submission summary
                final HashMap<JobId, String> submittedJobs = new HashMap<JobId, String>();
                //which creation or submission has failed
                final HashMap<String, String> failedJobs = new HashMap<String, String>();

                try {
                    for (String fileName : fileNames) {
                        String filePath = fileName;
                        try {
                            Job job = JobFactory.getFactory().createJob(filePath);
                            JobId id = SchedulerProxy.getInstance().submit(job);
                            submittedJobs.put(id, fileName);
                        } catch (JobCreationException e) {
                            failedJobs.put(fileName, "Job creation error : " + e.getMessage());
                        } catch (SchedulerException e) {
                            failedJobs.put(fileName, "Job submission error : " + e.getMessage());
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                if (failedJobs.size() != 0) {
                    //one error for one job to submit : display a simple dialog box
                    if (fileNames.length == 1) {
                        parent.getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                MessageDialog.openError(parent.getShell(), "Job submission error", failedJobs
                                        .get(fileNames[0]));
                            }
                        });

                    } else {
                        //display a dialog box with details for each job
                        final String text = "Submission summary : \n\n" + submittedJobs.size() +
                            " submitted. \n" + failedJobs.size() + " failed to submit.";

                        final String pluginId = Activator.PLUGIN_ID;

                        final MultiStatus ms = new MultiStatus(pluginId, 0,
                            "Creation or submission from some xml files has failed, see details.", null);

                        for (Entry<JobId, String> entry : submittedJobs.entrySet()) {
                            String ErrorText = "file name : " + entry.getValue() + " submitted, job ID : " +
                                entry.getKey().toString();
                            ms.add(new Status(IStatus.INFO, pluginId, ErrorText));
                        }
                        for (Entry<String, String> entry : failedJobs.entrySet()) {
                            String ErrorText = "file name : " + entry.getKey() + "\n" + entry.getValue();
                            ms.add(new Status(IStatus.ERROR, pluginId, ErrorText));
                        }
                        parent.getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                ErrorDialog.openError(parent.getShell(), "Job submission error", text, ms,
                                        IStatus.ERROR | IStatus.INFO);
                            }
                        });
                    }
                }
            }
        });

        worker.start();
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus schedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        if (connected && (schedulerStatus != SchedulerStatus.KILLED) &&
            (schedulerStatus != SchedulerStatus.SHUTTING_DOWN) &&
            (schedulerStatus != SchedulerStatus.STOPPED))
            setEnabled(true);
        else
            setEnabled(false);
    }
}
