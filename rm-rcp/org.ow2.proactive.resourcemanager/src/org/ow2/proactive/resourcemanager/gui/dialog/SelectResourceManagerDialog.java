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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.URIBuilder;
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 * This class allow to pop up a dialogue to connect a resource manager.
 *
 * @author The ProActive Team
 */
public class SelectResourceManagerDialog {

    public static final String RM_CONFIG_DIR = ".ProActive_ResourceManager";
    public static final String USER_DIR = System.getProperty("user.home");

    /** Name of the file which store the good url */
    public static final String URL_FILE = "urls";
    public static final String URL_FILE_PATH = USER_DIR + File.separator + RM_CONFIG_DIR + File.separator +
        URL_FILE;

    /** Name of the file which store the good login */
    public static final String LOGIN_FILE = ".proActive_rm_logins";
    public static final String LOGIN_FILE_PATH = USER_DIR + File.separator + RM_CONFIG_DIR + File.separator +
        LOGIN_FILE;
    private static final String SERVER_URL_PROPERTY_NAME = "pa.rm.serverURL";

    private static List<String> urls = null;
    private static List<String> logins = null;
    private static String url = null;
    private static String serverURLProperty = null;
    private static String login = null;
    private static String pwd = null;
    private static String credPath = null;
    private static boolean useCred = false;
    private static Boolean hasBeenCanceled = null;
    private static Combo urlCombo = null;
    private static Combo loginCombo = null;
    private Shell shell = null;
    private Button okButton = null;
    private Button cancelButton = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    private SelectResourceManagerDialog(Shell parent) {
        // Load the proactive default configuration
        ProActiveConfiguration.load();
        useCred = false;

        // Init the display
        Display display = parent.getDisplay();

        // Init the shell
        shell = new Shell(parent.getDisplay(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Connect to a resource manager");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        // creation
        Label urlLabel = new Label(shell, SWT.NONE);
        urlCombo = new Combo(shell, SWT.BORDER);
        final Label loginLabel = new Label(shell, SWT.NONE);
        loginCombo = new Combo(shell, SWT.BORDER);
        final Label pwdLabel = new Label(shell, SWT.NONE);
        final Text pwdText = new Text(shell, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
        okButton = new Button(shell, SWT.NONE);
        cancelButton = new Button(shell, SWT.NONE);
        final Label authLabel = new Label(shell, SWT.NONE);
        final Combo authType = new Combo(shell, SWT.BORDER | SWT.READ_ONLY);
        final Label credLabel = new Label(shell, SWT.NONE);
        final Text credText = new Text(shell, SWT.SINGLE | SWT.BORDER);
        final Button credButton = new Button(shell, SWT.NONE);

        // label url
        urlLabel.setText("Url");
        FormData urlLabelFormData = new FormData();
        urlLabelFormData.top = new FormAttachment(urlCombo, 0, SWT.CENTER);
        urlLabelFormData.right = new FormAttachment(urlCombo, -5);
        urlLabel.setLayoutData(urlLabelFormData);

        // combo url
        FormData urlFormData = new FormData();
        urlFormData.top = new FormAttachment(0, -1);
        urlFormData.left = new FormAttachment(credLabel, 5);
        urlFormData.right = new FormAttachment(100, -5);
        urlFormData.width = 320;
        urlCombo.setLayoutData(urlFormData);

        serverURLProperty = System.getProperty(SERVER_URL_PROPERTY_NAME);
        if (serverURLProperty == null) {
            loadUrls();
        } else {
            urlCombo.setText(serverURLProperty);
            urlCombo.setEnabled(false);
        }

        // auth type : credentials or plain login/pwd
        authLabel.setText("Authentication");
        FormData authLFD = new FormData();
        authLFD.top = new FormAttachment(authType, 5, SWT.CENTER);
        authLFD.left = new FormAttachment(0, 5);
        authLabel.setLayoutData(authLFD);

        authType.setItems(new String[] { "Basic", "Credentials" });
        authType.setText("Basic");
        FormData authFD = new FormData();
        authFD.top = new FormAttachment(urlCombo, 5);
        authFD.left = new FormAttachment(authLabel, 10);
        authType.setLayoutData(authFD);
        authType.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                useCred = authType.getText().equals("Credentials");

                credLabel.setVisible(useCred);
                credText.setVisible(useCred);
                credButton.setVisible(useCred);
                pwdLabel.setVisible(!useCred);
                pwdText.setVisible(!useCred);
                loginLabel.setVisible(!useCred);
                loginCombo.setVisible(!useCred);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // label login
        loginLabel.setText("Login");
        FormData loginLabelFormData = new FormData();
        loginLabelFormData.top = new FormAttachment(loginCombo, 0, SWT.CENTER);
        loginLabelFormData.right = new FormAttachment(loginCombo, -5);
        loginLabel.setLayoutData(loginLabelFormData);

        // text login
        FormData loginFormData = new FormData();
        loginFormData.top = new FormAttachment(authType, 5);
        loginFormData.left = new FormAttachment(credLabel, 5);
        loginFormData.right = new FormAttachment(100, -60);
        loginCombo.setLayoutData(loginFormData);
        loadLogins();

        // label password
        pwdLabel.setText("Password");
        FormData pwdLabelFormData = new FormData();
        pwdLabelFormData.top = new FormAttachment(pwdText, 0, SWT.CENTER);
        pwdLabelFormData.right = new FormAttachment(pwdText, -5);
        pwdLabel.setLayoutData(pwdLabelFormData);

        // text password
        FormData pwdFormData = new FormData();
        pwdFormData.top = new FormAttachment(loginCombo, 5);
        pwdFormData.left = new FormAttachment(credLabel, 5);
        pwdFormData.right = new FormAttachment(100, -60);
        pwdText.setLayoutData(pwdFormData);

        // credendials file selection
        FormData credLD = new FormData();
        credLabel.setText("Credentials");
        credLD.top = new FormAttachment(credText, 0, SWT.CENTER);
        credLD.right = new FormAttachment(credText, -5);
        credLabel.setLayoutData(credLD);

        FormData credTD = new FormData();
        credTD.top = new FormAttachment(loginCombo, 5);
        credTD.left = new FormAttachment(credLabel, 5);
        credTD.right = new FormAttachment(80, -5);
        credText.setLayoutData(credTD);

        credButton.setText("Browse");
        FormData credBD = new FormData();
        credBD.top = new FormAttachment(loginCombo, 5);
        credBD.left = new FormAttachment(credText, 5);
        credBD.right = new FormAttachment(100, -5);
        credButton.setLayoutData(credBD);
        credButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
                if (credText.getText() != null && credText.getText().length() > 0) {
                    fileDialog.setFileName(credText.getText());
                }
                String fileName = fileDialog.open();
                if (fileName != null) {
                    credText.setText(fileName);
                }
            }
        });

        credLabel.setVisible(false);
        credText.setVisible(false);
        credButton.setVisible(false);

        // button "OK"
        okButton.setText("OK");
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                hasBeenCanceled = false;
                url = urlCombo.getText();
                login = loginCombo.getText();
                pwd = pwdText.getText();
                credPath = credText.getText();
                shell.close();
            }
        });

        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(pwdText, 15);
        okFormData.left = new FormAttachment(25, 20);
        okFormData.right = new FormAttachment(50, -10);
        okButton.setLayoutData(okFormData);
        shell.setDefaultButton(okButton);

        // button "CANCEL"
        cancelButton.setText("Cancel");
        cancelButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                hasBeenCanceled = true;
                shell.close();
            }
        });

        FormData cancelFormData = new FormData();
        cancelFormData.top = new FormAttachment(pwdText, 15);
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
    private static void setInitialHostName() {
        String initialHostValue = "";
        String port = "";
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
            BufferedReader reader = new BufferedReader(new FileReader(URL_FILE_PATH));
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
            bw = new BufferedWriter(new FileWriter(URL_FILE_PATH));
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
            BufferedReader reader = new BufferedReader(new FileReader(LOGIN_FILE_PATH));
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
            File file = new File(USER_DIR + File.separator + RM_CONFIG_DIR);
            if (!file.exists())
                file.mkdir();
            bw = new BufferedWriter(new FileWriter(LOGIN_FILE_PATH, false));
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
            if (login != null && login.trim().length() > 0) {
                pw.println(login);
            }
        } catch (IOException e) {
            Activator.log(IStatus.ERROR, "Error when recording logins. ", e);
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
                Activator.log(IStatus.ERROR, "Error when recording logins. ", e);
                e.printStackTrace();
                /* Do-Nothing */
            }
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * This method pop up a dialog for trying to connect a resource manager.
     *
     * @param parent the parent
     * @return a SelectResourceManagerDialogResult which contain all needed
     *         informations.
     */
    public static SelectResourceManagerDialogResult showDialog(Shell parent) {
        new SelectResourceManagerDialog(parent);
        if (url == null) {
            return null;
        }

        byte[] cred = null;
        if (useCred) {
            try {
                cred = FileToBytesConverter.convertFileToByteArray(new File(credPath));
            } catch (IOException e) {
                MessageDialog.openError(parent, "Error", "Credentials file not found : " + credPath);
                return null;
            }
        }

        url = url.trim();
        SelectResourceManagerDialogResult res = new SelectResourceManagerDialogResult(hasBeenCanceled, url,
            login, pwd, cred);
        pwd = null;
        return res;
    }

    /**
     * For saving login and url in files.
     */
    public static void saveInformations() {
        recordLogins();
        if (serverURLProperty == null) {
            recordUrls();
        }
    }
}
