/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.gui.process;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.rsh.RSHProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.MessageLogger;
import org.objectweb.proactive.ic2d.gui.util.MessagePanel;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class ProcessControlPanel extends javax.swing.JPanel {
    private static final String[] DEFAULT_ENVIRONMENT = {
            "DISPLAY=" + RSHProcess.DEFAULT_HOSTNAME + ":0"
        };
    private static final java.awt.Color FINISHED_PROCESS_COLOR = new java.awt.Color(211,
            32, 47);
    private ProcessesListModel processesListModel;
    private ProcessListPanel processListPanel;
    private ProcessDefinitionPanel processDefinitionPanel;
    private ProcessOutputPanel processOutputPanel;
    private javax.swing.JSplitPane mainSplitPanel;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public ProcessControlPanel() {
        processesListModel = new ProcessesListModel();

        setLayout(new java.awt.GridLayout(1, 1));
        // create the top split panel
        processListPanel = new ProcessListPanel();
        processDefinitionPanel = new ProcessDefinitionPanel();
        processOutputPanel = new ProcessOutputPanel();
        javax.swing.JSplitPane topSplitPanel = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT,
                false, processListPanel, processDefinitionPanel);
        topSplitPanel.setDividerLocation(200);
        topSplitPanel.setOneTouchExpandable(true);

        //Create the full split pane
        mainSplitPanel = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT,
                false, topSplitPanel, processOutputPanel);
        //mainSplitPanel.setDividerLocation(getHeight() - 200);
        mainSplitPanel.setOneTouchExpandable(true);
        add(mainSplitPanel);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void processChanged(JVMProcessWrapper process) {
        processListPanel.processChanged(process);
        processDefinitionPanel.processChanged(process);
        processOutputPanel.processChanged(process);
    }

    private void currentProcessChanged() {
        processChanged(processListPanel.getCurrentProcess());
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    private interface MonitoredRSHProcessObserver {
        public void processChanged(JVMProcessWrapper process);
    }

    private interface MonitoredRSHProcessManager {
        public JVMProcessWrapper getCurrentProcess();
    }

    /**
     *
     *
     *
     * ProcessList
     *
     *
     *
     */
    private class ProcessesListModel extends javax.swing.AbstractListModel {
        private java.util.ArrayList processesList;

        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public ProcessesListModel() {
            processesList = new java.util.ArrayList();
        }

        //
        // -- Public methods -----------------------------------------------
        //
        //
        // -- implements ListModel -----------------------------------------------
        //
        public int getSize() {
            return processesList.size();
        }

        public Object getElementAt(int index) {
            return processesList.get(index);
        }

        public void addProcess(JVMProcessWrapper wrapper) {
            int n = processesList.size();
            processesList.add(wrapper);
            fireIntervalAdded(this, n, n);
        }

        public void removeProcess(int index) {
            JVMProcessWrapper wrapper = (JVMProcessWrapper) processesList.remove(index);
            wrapper.stopProcess();
            fireIntervalRemoved(this, index, index);
        }
    } // end inner class ProcessListModel

    /**
     * Cell Renderer
     */
    private class ProcessListCellRenderer
        extends javax.swing.DefaultListCellRenderer {
        public ProcessListCellRenderer() {
        }

        public java.awt.Component getListCellRendererComponent(
            javax.swing.JList list, Object o, int index, boolean isSelected,
            boolean cellHasFocus) {
            java.awt.Component c = super.getListCellRendererComponent(list, o,
                    index, isSelected, cellHasFocus);
            if (!(o instanceof JVMProcessWrapper)) {
                return c;
            }
            JVMProcessWrapper wrapper = (JVMProcessWrapper) o;
            if (wrapper.getProcess().isFinished()) {
                c.setForeground(FINISHED_PROCESS_COLOR);
            }
            return c;
        }
    }

    /**
     *
     *
     *
     * ProcessListPanel
     *
     *
     *
     */
    private class ProcessListPanel extends javax.swing.JPanel
        implements MonitoredRSHProcessObserver, MonitoredRSHProcessManager {
        private javax.swing.JButton stopProcessButton;
        private javax.swing.JList processesJList;

        public ProcessListPanel() {
            setBorder(javax.swing.BorderFactory.createTitledBorder(
                    "List of current processes"));
            setLayout(new java.awt.BorderLayout());
            // list
            processesJList = new javax.swing.JList(processesListModel);
            processesJList.setCellRenderer(new ProcessListCellRenderer());
            processesJList.setVisibleRowCount(10);
            processesJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

                    /**
                     * When an item is selected, this method is fired and updates the display area
                     */
                    public void valueChanged(
                        javax.swing.event.ListSelectionEvent e) {
                        if (e.getValueIsAdjusting()) {
                            return;
                        }
                        currentProcessChanged();
                    }
                });

            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(processesJList);
            scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(scrollPane, java.awt.BorderLayout.CENTER);
            // stop button
            stopProcessButton = new javax.swing.JButton("Stop selected process");
            stopProcessButton.setEnabled(false);
            stopProcessButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        int n = processesJList.getSelectedIndex();
                        if (n > -1) {
                            processesListModel.removeProcess(n);
                            currentProcessChanged();
                        }
                    }
                });
            stopProcessButton.setToolTipText(
                "Stop the selected process in the list");
            add(stopProcessButton, java.awt.BorderLayout.SOUTH);
        }

        public JVMProcessWrapper getCurrentProcess() {
            return (JVMProcessWrapper) processesJList.getSelectedValue();
        }

        public void processChanged(JVMProcessWrapper process) {
            stopProcessButton.setEnabled(process != null);
        }
    } // end inner class ProcessListPanel

    /**
     *
     *
     *
     * ProcessDefinitionPanel
     *
     *
     *
     */
    private class ProcessDefinitionPanel extends javax.swing.JPanel
        implements MonitoredRSHProcessObserver {
        //ProActiveRuntimeImpl part = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
        String localRuntimeUrl;
        private javax.swing.JTextField hostnameField = new javax.swing.JTextField(RSHProcess.DEFAULT_HOSTNAME);
        private javax.swing.JTextField usernameField = new javax.swing.JTextField(RSHProcess.DEFAULT_USERNAME);
        private javax.swing.JTextField javaPathField = new javax.swing.JTextField(JVMProcessImpl.DEFAULT_JAVAPATH);
        private javax.swing.JTextField policyFileField = new javax.swing.JTextField(JVMProcessImpl.DEFAULT_POLICY_FILE);
        private javax.swing.JTextField classnameField = new javax.swing.JTextField(
                "org.objectweb.proactive.core.runtime.StartRuntime");
        private javax.swing.JTextField parametersField;
        private javax.swing.JTextArea classpathField = new javax.swing.JTextArea(JVMProcessImpl.DEFAULT_CLASSPATH,
                4, 0);
        private javax.swing.JTextArea environmentField = new javax.swing.JTextArea(stringArrayToString(
                    DEFAULT_ENVIRONMENT), 4, 0);

        public ProcessDefinitionPanel() {
            setBorder(javax.swing.BorderFactory.createTitledBorder(
                    "Create new process"));
            setLayout(new java.awt.BorderLayout());
            ProActiveRuntime part;
            try {
                part = RuntimeFactory.getDefaultRuntime();
                localRuntimeUrl = part.getURL();
            } catch (ProActiveException e1) {
                e1.printStackTrace();
            }
            parametersField = new javax.swing.JTextField("ic2d " +
                    localRuntimeUrl + " rmi: 1 jvm");
            // defines fields in the north panel 
            {
                javax.swing.JPanel northPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
                javax.swing.JPanel labelPanel = new javax.swing.JPanel(new java.awt.GridLayout(
                            0, 1));
                javax.swing.JPanel fieldPanel = new javax.swing.JPanel(new java.awt.GridLayout(
                            0, 1));
                labelPanel.add(new javax.swing.JLabel("hostname ",
                        javax.swing.JLabel.RIGHT));
                fieldPanel.add(hostnameField);
                labelPanel.add(new javax.swing.JLabel("username ",
                        javax.swing.JLabel.RIGHT));
                fieldPanel.add(usernameField);
                labelPanel.add(new javax.swing.JLabel("java command path ",
                        javax.swing.JLabel.RIGHT));
                fieldPanel.add(javaPathField);
                labelPanel.add(new javax.swing.JLabel("policy file path ",
                        javax.swing.JLabel.RIGHT));
                fieldPanel.add(policyFileField);
                labelPanel.add(new javax.swing.JLabel("classname to start ",
                        javax.swing.JLabel.RIGHT));
                fieldPanel.add(classnameField);
                labelPanel.add(new javax.swing.JLabel(
                        "parameters of the class ", javax.swing.JLabel.RIGHT));
                fieldPanel.add(parametersField);
                northPanel.add(labelPanel, java.awt.BorderLayout.WEST);
                northPanel.add(fieldPanel, java.awt.BorderLayout.CENTER);
                add(northPanel, java.awt.BorderLayout.NORTH);
            }

            {
                // defines text areas in the center panel
                javax.swing.JPanel labelPanel = new javax.swing.JPanel(new java.awt.GridLayout(
                            0, 1));
                javax.swing.JPanel fieldPanel = new javax.swing.JPanel(new java.awt.GridLayout(
                            0, 1));
                labelPanel.add(new javax.swing.JLabel("classpath ",
                        javax.swing.JLabel.RIGHT));
                fieldPanel.add(createScrollWrapper(classpathField));
                classpathField.setLineWrap(true);
                labelPanel.add(new javax.swing.JLabel("environment ",
                        javax.swing.JLabel.RIGHT));
                fieldPanel.add(createScrollWrapper(environmentField));
                environmentField.setLineWrap(false);
                add(labelPanel, java.awt.BorderLayout.WEST);
                add(fieldPanel, java.awt.BorderLayout.CENTER);
            }
            // defines start button in the south panel 
            {
                javax.swing.JButton b = new javax.swing.JButton(
                        "Start new process");
                b.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                            java.awt.event.ActionEvent e) {
                            String hostname = filterEmptyString(hostnameField.getText());
                            String username = filterEmptyString(usernameField.getText());
                            String javaPath = filterEmptyString(javaPathField.getText());
                            String policyFile = filterEmptyString(policyFileField.getText());
                            String classname = filterEmptyString(classnameField.getText());
                            String classpath = filterEmptyString(classpathField.getText());
                            String parameters = filterEmptyString(parametersField.getText());
                            String[] environment = stringToStringArray(environmentField.getText());
                            JVMProcessWrapper w = createJVMProcess(environment,
                                    javaPath, policyFile, classpath, classname,
                                    parameters, hostname, username);
                            processesListModel.addProcess(w);
                            w.startProcess();
                        }
                    });
                b.setToolTipText("Start a process based on the above specs");
                add(b, java.awt.BorderLayout.SOUTH);
            }
        }

        public void processChanged(JVMProcessWrapper wrapper) {
            if (wrapper == null) {
                return;
            }
            hostnameField.setText(filterNull(wrapper.getHostname()));
            usernameField.setText(filterNull(wrapper.getUsername()));
            javaPathField.setText(filterNull(wrapper.getJavaPath()));
            policyFileField.setText(filterNull(wrapper.getPolicyFile()));
            classnameField.setText(filterNull(wrapper.getClassname()));
            parametersField.setText(filterNull(wrapper.getParameters()));
            classpathField.setText(filterNull(wrapper.getClasspath()));
            environmentField.setText(stringArrayToString(
                    wrapper.getProcess().getEnvironment()));
        }

        private JVMProcessWrapper createJVMProcess(String[] environment,
            String javaPath, String policyFile, String classpath,
            String classname, String parameters, String hostname,
            String username) {
            MessagePanel messagePanel = new MessagePanel(
                    "Messages for process running " + classname);
            JVMProcessImpl process = new JVMProcessImpl(new SynchronizedMessageLogger(
                        messagePanel.getMessageLogger()));
            process.setEnvironment(environment);
            process.setJavaPath(javaPath);
            process.setPolicyFile(policyFile);
            process.setClasspath(classpath);
            process.setClassname(classname);
            process.setParameters(parameters);
            try {
                if (!hostname.equals(InetAddress.getLocalHost().getHostName())) {
                    RSHProcess rshProcess = new RSHProcess(process);
                    rshProcess.setHostname(hostname);
                    rshProcess.setUsername(username);
                    process.setParameters("ic2d " + localRuntimeUrl +
                        " rmi: 1 rsh-jvm");
                    return new JVMProcessWrapper(rshProcess, messagePanel,
                        javaPath, policyFile, classpath, classname, parameters,
                        hostname, username);
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                messagePanel.getMessageLogger().log("Unknown Host", e);
            }
            return new JVMProcessWrapper(process, messagePanel, javaPath,
                policyFile, classpath, classname, parameters);
        }

        private String filterNull(String s) {
            if (s == null) {
                return "";
            }
            return s;
        }

        private String filterEmptyString(String s) {
            if (s.length() == 0) {
                return null;
            }
            return s;
        }

        private String stringArrayToString(String[] stringArray) {
            if (stringArray == null) {
                return "";
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < stringArray.length; i++) {
                sb.append(stringArray[i]);
                sb.append("\n");
            }
            return sb.toString();
        }

        private String[] stringToStringArray(String string) {
            java.util.StringTokenizer st = new java.util.StringTokenizer(string,
                    "\n");
            java.util.ArrayList result = new java.util.ArrayList();
            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                if (s.length() > 0) {
                    result.add(s);
                }
            }
            if (result.size() == 0) {
                return null;
            }
            String[] stringArray = new String[result.size()];
            result.toArray(stringArray);
            return stringArray;
        }

        private javax.swing.JScrollPane createScrollWrapper(
            javax.swing.JTextArea textArea) {
            javax.swing.JScrollPane areaScrollPane = new javax.swing.JScrollPane(textArea);
            areaScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            areaScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(
                    java.awt.Color.black));
            return areaScrollPane;
        }
    } // end inner class ProcessDefinitionPanel

    public class SynchronizedMessageLogger implements MessageLogger {
        private MessageLogger logger;

        public SynchronizedMessageLogger(MessageLogger logger) {
            this.logger = logger;
        }

        public synchronized void log(String message) {
            logger.log(message);
        }

        public synchronized void log(String message, Throwable e) {
            logger.log(message, e);
        }

        public synchronized void log(Throwable e) {
            logger.log(e);
        }
    }

    /**
     *
     *
     *
     * ProcessOutputPanel
     *
     *
     *
     */
    private class ProcessOutputPanel extends javax.swing.JPanel
        implements MonitoredRSHProcessObserver {
        private javax.swing.JPanel emptyProcessPanel;

        public ProcessOutputPanel() {
            setLayout(new java.awt.GridLayout(1, 1));
            // create the empty Process Panel
            emptyProcessPanel = new javax.swing.JPanel();
            emptyProcessPanel.add(new javax.swing.JLabel("no process selected"));
            add(emptyProcessPanel);
        }

        public void processChanged(JVMProcessWrapper process) {
            removeAll();
            if (process == null) {
                add(emptyProcessPanel);
            } else {
                add(process.getPanel());
            }
            revalidate();
            repaint();
        }
    } // end inner class ProcessOutputPanel
}
