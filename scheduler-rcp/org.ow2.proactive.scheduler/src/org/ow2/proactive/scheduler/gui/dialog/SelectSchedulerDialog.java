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
package org.ow2.proactive.scheduler.gui.dialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.URIBuilder;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 * This class allow to pop up a dialogue to connect a scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SelectSchedulerDialog extends Dialog {

    /** Name of the file which store good urls */
    public static final String URL_FILE = "urls";

    /** Name of the file which store good logins */
    public static final String LOGIN_FILE = "logins";

    /** Name of the file which store good logins */
    public static final String SSHKEY_FILE = "sshpath";

    private static List<String> urls = null;
    private static List<String> logins = null;
    private static boolean validate = false;
    private static String url = null;
    private static String defaultUrl = null;
    private static String login = null;
    private static String pwd = null;
    private static boolean useSSH = false;
    private static String SSHkeyPath = null;
    private static Combo urlCombo = null;
    private static Combo loginCombo = null;
    private static Text sshText = null;
    private Shell shell = null;
    private Button okButton = null;
    private Button cancelButton = null;
    private Button useSSHKey = null;
    private Label sshLabel = null;
    private Button chooseButton = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    private SelectSchedulerDialog(Shell parent) {
        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        // Load the proactive default configuration
        ProActiveConfiguration.load();

        validate = false;

        // Init the display
        Display display = parent.getDisplay();

        // Init the shell
        shell = new Shell(parent, SWT.BORDER | SWT.CLOSE);
        shell.setText("Connect to scheduler");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        // creation
        Label urlLabel = new Label(shell, SWT.NONE);
        urlCombo = new Combo(shell, SWT.BORDER);
        Label loginLabel = new Label(shell, SWT.NONE);
        loginCombo = new Combo(shell, SWT.BORDER);
        Label pwdLabel = new Label(shell, SWT.NONE);
        final Text pwdText = new Text(shell, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
        useSSHKey = new Button(shell, SWT.CHECK);
        sshLabel = new Label(shell, SWT.NONE);
        sshText = new Text(shell, SWT.SINGLE | SWT.BORDER);
        chooseButton = new Button(shell, SWT.NONE);
        okButton = new Button(shell, SWT.NONE);
        cancelButton = new Button(shell, SWT.NONE);

        // label url
        urlLabel.setText("Url :");
        FormData urlLabelFormData = new FormData();
        urlLabelFormData.top = new FormAttachment(urlCombo, 0, SWT.CENTER);
        urlLabelFormData.left = new FormAttachment(0, 5);
        urlLabel.setLayoutData(urlLabelFormData);

        // combo url
        FormData urlFormData = new FormData();
        urlFormData.top = new FormAttachment(0, -1);
        urlFormData.left = new FormAttachment(loginLabel, 5);
        urlFormData.right = new FormAttachment(100, -5);
        urlFormData.width = 320;
        urlCombo.setLayoutData(urlFormData);

        defaultUrl = System.getProperty("pa.scheduler.serverURL");
        if (defaultUrl == null) {
            loadUrls();
        } else {
            urlCombo.setText(defaultUrl);
            urlCombo.setEnabled(false);
        }

        // label login
        loginLabel.setText("login :");
        FormData loginLabelFormData = new FormData();
        loginLabelFormData.top = new FormAttachment(loginCombo, 0, SWT.CENTER);
        loginLabelFormData.left = new FormAttachment(0, 5);
        loginLabel.setLayoutData(loginLabelFormData);

        // text login
        FormData loginFormData = new FormData();
        loginFormData.top = new FormAttachment(urlCombo, 5);
        loginFormData.left = new FormAttachment(loginLabel, 5);
        loginFormData.right = new FormAttachment(40, 5);
        loginCombo.setLayoutData(loginFormData);
        loadLogins();

        // label password
        pwdLabel.setText("password :");
        FormData pwdLabelFormData = new FormData();
        pwdLabelFormData.top = new FormAttachment(pwdText, 0, SWT.CENTER);
        pwdLabelFormData.left = new FormAttachment(loginCombo, 5);
        pwdLabel.setLayoutData(pwdLabelFormData);

        // text password
        FormData pwdFormData = new FormData();
        pwdFormData.top = new FormAttachment(urlCombo, 5);
        pwdFormData.left = new FormAttachment(pwdLabel, 5);
        pwdFormData.right = new FormAttachment(100, -5);
        pwdText.setLayoutData(pwdFormData);

        //ssh checkbox
        useSSHKey.setText("Provide private SSH key");
        FormData sshFormData = new FormData();
        sshFormData.top = new FormAttachment(pwdText, 16, SWT.BOTTOM);
        sshFormData.left = new FormAttachment(0, 1);
        useSSHKey.setLayoutData(sshFormData);
        useSSHKey.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                setSSHformEnabled(useSSHKey.getSelection());
                loadSSHPath();
            }
        });

        //ssh label / text field / browse
        sshLabel.setText("SSH private key path :");
        FormData sshLabelFormData = new FormData();
        sshLabelFormData.top = new FormAttachment(useSSHKey, 6, SWT.BOTTOM);
        sshLabelFormData.left = new FormAttachment(0, 5);
        sshLabel.setLayoutData(sshLabelFormData);

        FormData sshTextFormData = new FormData();
        sshTextFormData.top = new FormAttachment(useSSHKey, 3);
        sshTextFormData.left = new FormAttachment(sshLabel, 5);
        sshTextFormData.right = new FormAttachment(80, -5);
        sshText.setLayoutData(sshTextFormData);

        chooseButton.setText("Choose file");
        FormData chooseButtonTextFormData = new FormData();
        chooseButtonTextFormData.top = new FormAttachment(useSSHKey, 2);
        chooseButtonTextFormData.left = new FormAttachment(sshText, 5);
        chooseButtonTextFormData.right = new FormAttachment(100, -5);
        chooseButton.setLayoutData(chooseButtonTextFormData);
        final Text sshTextf = sshText;
        chooseButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
                if (sshTextf.getText() != null && sshTextf.getText().length() > 0) {
                    fileDialog.setFileName(sshTextf.getText());
                }
                String fileName = fileDialog.open();
                if (fileName != null) {
                    sshTextf.setText(fileName);
                }
            }
        });
        setSSHformEnabled(false);

        // button "OK"
        okButton.setText("OK");
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                validate = true;
                url = urlCombo.getText();
                login = loginCombo.getText();
                pwd = pwdText.getText();
                useSSH = useSSHKey.getSelection();
                if (useSSH) {
                    SSHkeyPath = sshText.getText();
                } else {
                    SSHkeyPath = null;
                }
                shell.close();
            }
        });

        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(chooseButton, 6);
        okFormData.left = new FormAttachment(25, 20);
        okFormData.right = new FormAttachment(50, -10);
        okButton.setLayoutData(okFormData);
        shell.setDefaultButton(okButton);

        // button "CANCEL"
        cancelButton.setText("Cancel");
        cancelButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                validate = false;
                shell.close();
            }
        });

        FormData cancelFormData = new FormData();
        cancelFormData.top = new FormAttachment(chooseButton, 6);
        cancelFormData.left = new FormAttachment(50, 10);
        cancelFormData.right = new FormAttachment(75, -20);
        cancelButton.setLayoutData(cancelFormData);

        shell.pack();
        shell.open();

        pwdText.setFocus();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- private ------------------------------ //
    // -------------------------------------------------------------------- //
    private void setSSHformEnabled(boolean enabled) {
        sshLabel.setEnabled(enabled);
        chooseButton.setEnabled(enabled);
        sshText.setEnabled(enabled);
    }

    private static void setInitialHostName() {
        String initialHostValue = "";
        String port = null;
        try {
            /* Get the machine's name */
            initialHostValue = URIBuilder.getHostNameorIP(InetAddress.getLocalHost());
            /* Get the machine's port */
            port = System.getProperty("proactive.rmi.port");
        } catch (UnknownHostException e) {
            initialHostValue = "localhost";
        }
        if (port == null) {
            port = "1099";
        }
        urlCombo.add("rmi://" + initialHostValue + ":" + port + "/");
        urlCombo.setText("rmi://" + initialHostValue + ":" + port + "/");
    }

    private static void setInitialLogin() {

        /* Get the user name */
        String initialLogin = System.getProperty("user.name");
        loginCombo.add(initialLogin);
        loginCombo.setText(initialLogin);
    }

    /**
     * Load Urls
     */
    private static void loadUrls() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.home") +
                "/.ProActive_Scheduler/" + URL_FILE));
            try {
                urls = new ArrayList<String>();
                String url = null;
                String lastUrl = null;
                while ((url = reader.readLine()) != null) {
                    urls.add(url);
                    lastUrl = url;
                }
                int size = urls.size();
                if (size > 0) {
                    String[] hosts = new String[size];
                    urls.toArray(hosts);
                    Arrays.sort(hosts);
                    urlCombo.setItems(hosts);
                    urlCombo.setText(lastUrl);
                } else {
                    setInitialHostName();
                }
            } catch (IOException e) {
                /* Do-nothing */
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    /* Do-Nothing */
                }
            }
        } catch (FileNotFoundException e) {
            setInitialHostName();
        }
    }

    /**
     * Record urls
     */
    private static void recordUrls() {
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            File file = new File(System.getProperty("user.home") + "/.ProActive_Scheduler/");
            if (!file.exists())
                file.mkdir();
            bw = new BufferedWriter(new FileWriter(System.getProperty("user.home") +
                "/.ProActive_Scheduler/" + URL_FILE, false));
            pw = new PrintWriter(bw, true);

            // Record urls
            if (urls != null) {
                for (String s : urls) {
                    if (!s.equals(url)) {
                        pw.println(s);
                    }
                }
            }
            // Record the last URL used at the end of the file
            // in order to find it easily for the next time
            pw.println(url);
        } catch (IOException e) {

            /* Do-Nothing */
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (pw != null) {
                    pw.close();
                }
            } catch (IOException e) {

                /* Do-Nothing */
            }
        }
    }

    /**
     * Load Logins
     */
    private static void loadLogins() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.home") +
                "/.ProActive_Scheduler/" + LOGIN_FILE));
            try {
                logins = new ArrayList<String>();
                String login = null;
                String lastUrl = null;
                while ((login = reader.readLine()) != null) {
                    logins.add(login);
                    lastUrl = login;
                }
                int size = logins.size();
                if (size > 0) {
                    String[] hosts = new String[size];
                    logins.toArray(hosts);
                    Arrays.sort(hosts);
                    loginCombo.setItems(hosts);
                    loginCombo.setText(lastUrl);
                } else {
                    setInitialLogin();
                }
            } catch (IOException e) {

                /* Do-nothing */
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {

                    /* Do-Nothing */
                }
            }
        } catch (FileNotFoundException e) {
            setInitialLogin();
        }
    }

    /**
     * Record an login
     */
    private static void recordLogins() {
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            File file = new File(System.getProperty("user.home") + "/.ProActive_Scheduler/");
            if (!file.exists())
                file.mkdir();
            bw = new BufferedWriter(new FileWriter(System.getProperty("user.home") +
                "/.ProActive_Scheduler/" + LOGIN_FILE, false));
            pw = new PrintWriter(bw, true);

            // Record logins
            if (logins != null) {
                for (String s : logins) {
                    if (!s.equals(login)) {
                        pw.println(s);
                    }
                }
            }
            // Record the last Login used at the end of the file
            // in order to find it easily for the next time
            pw.println(login);
        } catch (IOException e) {
            Activator.log(IStatus.ERROR, "An Exception occured when recording logins", e);
            e.printStackTrace();
            /* Do-Nothing */
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (pw != null) {
                    pw.close();
                }
            } catch (IOException e) {
                Activator.log(IStatus.ERROR, "An Exception occured when recording logins", e);
                /* Do-Nothing */
            }
        }
    }

    private static void recordSSHPath() {
        if (useSSH) {
            BufferedWriter bw = null;
            PrintWriter pw = null;
            try {
                File file = new File(System.getProperty("user.home") + "/.ProActive_Scheduler/");
                if (!file.exists()) {
                    file.mkdir();
                }
                bw = new BufferedWriter(new FileWriter(System.getProperty("user.home") +
                    "/.ProActive_Scheduler/" + SSHKEY_FILE, false));
                pw = new PrintWriter(bw, true);

                // Record path
                pw.println(SSHkeyPath);
            } catch (IOException e) {
                Activator.log(IStatus.ERROR, "An Exception occured when recording sshkey", e);
                e.printStackTrace();
                /* Do-Nothing */
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                    if (pw != null) {
                        pw.close();
                    }
                } catch (IOException e) {
                    Activator.log(IStatus.ERROR, "An Exception occured when recording sshkey", e);
                    /* Do-Nothing */
                }
            }
        }
    }

    private static void loadSSHPath() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.home") +
                "/.ProActive_Scheduler/" + SSHKEY_FILE));
            try {
                sshText.setText(reader.readLine());
            } catch (IOException e) {
                /* Do-nothing */
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    /* Do-Nothing */
                }
            }
        } catch (FileNotFoundException e) {
            /* Do-Nothing */
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * This method pop up a dialog for trying to connect a scheduler.
     *
     * @param parent the parent
     *
     * @return a SelectSchedulerDialogResult which contain all needed informations.
     */
    public static SelectSchedulerDialogResult showDialog(Shell parent) {
        new SelectSchedulerDialog(parent);
        if (validate) {
            if ((url == null) || url.trim().equals("")) {
                MessageDialog.openError(parent, "Error", "The url is empty !");
                return null;
            }
            if ((login == null) || login.trim().equals("")) {
                MessageDialog.openError(parent, "Error", "The login is empty !");
                return null;
            }
            url = url.trim();
            if (useSSH) {
                try {
                    byte[] keyfileContent = FileToBytesConverter.convertFileToByteArray(new File(SSHkeyPath));
                    return new SelectSchedulerDialogResult(url, login, pwd, keyfileContent);
                } catch (Exception e) {
                    MessageDialog.openError(parent, "Error", "SSh key file not found : " + SSHkeyPath);
                    return null;
                }
            } else {
                return new SelectSchedulerDialogResult(url, login, pwd);
            }
        }
        return null;
    }

    /**
     * For saving login and url in files.
     */
    public static void saveInformations() {
        recordLogins();
        if (defaultUrl == null) {
            recordUrls();
        }
        recordSSHPath();
    }
}
