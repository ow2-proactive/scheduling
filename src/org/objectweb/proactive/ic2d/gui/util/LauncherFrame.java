package org.objectweb.proactive.ic2d.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.objectweb.fractal.gui.menu.control.SimpleFileFilter;
import org.objectweb.proactive.core.descriptor.Launcher;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.ic2d.gui.IC2DFrame;
import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorPanel;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredJob;
import org.objectweb.proactive.ic2d.util.MonitorThread;


public class LauncherFrame extends JFrame {
    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JTextField pathTextField = new JTextField();
    JButton browseButton = new JButton("Browse Files");
    TitledBorder titledBorder1 = new TitledBorder("");
    Border border1 = BorderFactory.createEtchedBorder(Color.white,
            new Color(148, 145, 140));
    Border border2 = new TitledBorder(border1, "Deployment Descriptor path");
    JPanel jPanel3 = new JPanel();
    JButton oKButton = new JButton();
    JCheckBox jmCheckBox = new JCheckBox();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel jPanel4 = new JPanel();
    JPanel jPanel5 = new JPanel();
    JCheckBox gmCheckBox = new JCheckBox();
    private boolean doGraphicalMonitoring;
    private boolean doJobMonitoring;

    public LauncherFrame() {
        super("Application launcher");
        // for monitoring
        doGraphicalMonitoring = true;
        doJobMonitoring = true;
        try {
            jbInit();
        } catch (Exception e) {
        }
    }

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(borderLayout1);
        browseButton.setPreferredSize(new Dimension(110, 25));
        browseButton.setToolTipText("Browse file system");
        //browseButton.setText("Browse Files");
        browseButton.addActionListener(new MyLaunchDialog_browseButton_actionAdapter(
                this));
        pathTextField.setMinimumSize(new Dimension(150, 21));
        pathTextField.setPreferredSize(new Dimension(300, 21));
        jPanel2.setBorder(border2);
        oKButton.setToolTipText("Launch the application");
        oKButton.setText("OK");
        oKButton.addActionListener(new MyDialog_oKButton_actionAdapter(this));
        jmCheckBox.setText("Job monitoring");
        jmCheckBox.addActionListener(new MyLaunchDialog_jmCheckBox_actionAdapter(
                this));
        jmCheckBox.setSelected(doJobMonitoring);
        jPanel1.setLayout(borderLayout2);
        gmCheckBox.setText("Graphical monitoring");
        gmCheckBox.addActionListener(new MyLaunchDialog_gmCheckBox_actionAdapter(
                this));
        gmCheckBox.setSelected(doGraphicalMonitoring);
        jPanel2.add(pathTextField);
        jPanel2.add(browseButton);
        this.getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);
        jPanel3.add(oKButton);

        jPanel1.add(jPanel4, java.awt.BorderLayout.SOUTH);
        jPanel4.add(jmCheckBox);
        jPanel1.add(jPanel5, java.awt.BorderLayout.NORTH);
        jPanel5.add(gmCheckBox);
        this.getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        this.getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);
        pack();
        setVisible(false);
    }

    public void oKButton_actionPerformed(ActionEvent e) {
        try {
            if (pathTextField.getText().equals("")) {
                return;
            }

            // launch the application
            Launcher launcher = new Launcher(pathTextField.getText());
            launcher.activate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setVisible(false);
    }

    public void setVisible(boolean b) {
        if (b) {
            // display the window at the middle of the screen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int largeurEcran = screenSize.width;
            int hauteurEcran = screenSize.height;

            int largeurFrame = getSize().width;
            int hauteurFrame = getSize().height;
            int posX = (largeurEcran - largeurFrame) / 2;
            int posY = (hauteurEcran - hauteurFrame) / 2;

            setLocation(posX, posY);
        }
        super.setVisible(b);
    }

    public void browseButton_actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser(".");

        fileChooser.addChoosableFileFilter(new SimpleFileFilter("xml",
                "XML Descriptor files"));
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File f = fileChooser.getSelectedFile();

        String path = f.getPath();
        pathTextField.setText(path);
    }

    public void jmCheckBox_actionPerformed(ActionEvent e) {
        doJobMonitoring = jmCheckBox.isSelected();
    }

    public void gmCheckBox_actionPerformed(ActionEvent e) {
        doGraphicalMonitoring = gmCheckBox.isSelected();
    }

    public boolean isGraphicalMonitoring() {
        return doGraphicalMonitoring;
    }

    public boolean isJobMonitoring() {
        return doJobMonitoring;
    }

    public void setIsJobMonitoring(boolean isJobMonitoring) {
        doJobMonitoring = isJobMonitoring;
    }

    public void setIsGraphicalMonitoring(boolean isGraphicalMonitoring) {
        doGraphicalMonitoring = isGraphicalMonitoring;
    }
}


class MyLaunchDialog_gmCheckBox_actionAdapter implements ActionListener {
    private LauncherFrame adaptee;

    MyLaunchDialog_gmCheckBox_actionAdapter(LauncherFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.gmCheckBox_actionPerformed(e);
    }
}


class MyLaunchDialog_jmCheckBox_actionAdapter implements ActionListener {
    private LauncherFrame adaptee;

    MyLaunchDialog_jmCheckBox_actionAdapter(LauncherFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jmCheckBox_actionPerformed(e);
    }
}


class MyLaunchDialog_browseButton_actionAdapter implements ActionListener {
    private LauncherFrame adaptee;

    MyLaunchDialog_browseButton_actionAdapter(LauncherFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.browseButton_actionPerformed(e);
    }
}


class MyDialog_oKButton_actionAdapter implements ActionListener {
    private LauncherFrame adaptee;

    MyDialog_oKButton_actionAdapter(LauncherFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.oKButton_actionPerformed(e);
    }
}
