package org.objectweb.proactive.ic2d.gui.components.menu.control;

import org.objectweb.fractal.gui.UserData;
import org.objectweb.fractal.gui.menu.control.OpenAction;
import org.objectweb.fractal.gui.menu.control.SimpleFileFilter;
import org.objectweb.fractal.gui.model.Component;
import org.objectweb.fractal.swing.WaitGlassPane;

import org.objectweb.proactive.core.component.adl.vnexportation.ExportedVirtualNodesList;
import org.objectweb.proactive.ic2d.gui.components.util.Verifier;

import java.awt.event.ActionEvent;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveOpenAction extends OpenAction {

    /**
     *
     */
    public ProActiveOpenAction() {
        super();
    }

    public void actionPerformed(final ActionEvent e) {
        try {
            File storage = null;
            if (configuration.getStorage() != null) {
                storage = new File(configuration.getStorage());
                if (!storage.exists() || !storage.isDirectory()) {
                    storage = null;
                }
            }
            if (storage == null) {
                JOptionPane.showMessageDialog(null,
                    "A storage directory must be selected before files can be opened",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (configuration.getChangeCount() > 0) {
                Object[] options = { "Yes", "No", "Cancel" };
                int n = JOptionPane.showOptionDialog(null,
                        "Do you want to save the current configuration " +
                        "before opening a new one ?", "Warning",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (n == 0) {
                    save.actionPerformed(e);
                } else if (n == 2) {
                    return;
                }
            }
            JFileChooser fileChooser = new JFileChooser();
            String dir = null;
            if (userData != null) {
                dir = userData.getStringData(UserData.LAST_OPEN_DIR);
            }
            fileChooser.setCurrentDirectory((dir == null) ? storage
                                                          : new File(dir));
            String file = userData.getStringData(UserData.LAST_OPEN_FILE);
            if (file != null) {
                fileChooser.setSelectedFile(new File(file));
            }
            fileChooser.addChoosableFileFilter(new SimpleFileFilter("fractal",
                    "Fractal ADL files"));
            if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File f = fileChooser.getSelectedFile();

            File p = f;
            String name = f.getName().substring(0, f.getName().indexOf('.'));
            while ((p.getParentFile() != null) &&
                    !p.getParentFile().equals(storage)) {
                name = p.getParentFile().getName() + "." + name;
                p = p.getParentFile();
            }
            if (!storage.equals(p.getParentFile())) {
                JOptionPane.showMessageDialog(null,
                    "Cannot open a file which is not in the storage directory. " +
                    "Change the storage directory first.", "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (userData != null) {
                userData.setStringData(UserData.LAST_OPEN_FILE,
                    f.getAbsolutePath());
                userData.setStringData(UserData.LAST_OPEN_DIR, f.getParent());
                userData.setStringData(UserData.LAST_OPEN_CONF, name);
                userData.save();
            }

            new Thread(new ProActiveOpen(e, storage, name)).start();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    protected class ProActiveOpen extends Open {

        /**
         * @param arg1
         * @param arg2
         * @param arg3
         */
        public ProActiveOpen(ActionEvent arg1, File arg2, String arg3) {
            super(arg1, arg2, arg3);
        }

        /**
         *
         */
        public void run() {
            java.awt.Component glassPane = rootPane.getGlassPane();
            rootPane.setGlassPane(new WaitGlassPane());
            rootPane.getGlassPane().setVisible(true);

            try {
                ProActiveOpenAction.this.storage.open(storage.getAbsolutePath());
                try {
                    // first : empty static list of exported virtual nodes
                    ExportedVirtualNodesList.instance().empty();
                    Component c = repository.loadComponent(name, graph);
                    configuration.setRootComponent(c);
                    selection.selectComponent(c);
                    Verifier.checkConsistencyOfExportedVirtualNodes(false);
                } finally {
                    ProActiveOpenAction.this.storage.close();
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }

            rootPane.getGlassPane().setVisible(false);
            rootPane.setGlassPane(glassPane);
        }
    }
}
