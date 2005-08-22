package org.objectweb.proactive.ic2d.gui.jobmonitor;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.objectweb.proactive.ic2d.gui.jobmonitor.data.BasicMonitoredObject;


public class MonitoredObjectsList extends JDialog {
    private DefaultListModel monitoredListModel;
    private DefaultListModel skippedListModel;
    private JobMonitorPanel jobPanel;

    public MonitoredObjectsList(JobMonitorFrame owner, JobMonitorPanel jobPanel) {
        super(owner, true);

        this.jobPanel = jobPanel;
        monitoredListModel = copyList(null, jobPanel.getMonitoredHosts());
        skippedListModel = copyList(null, jobPanel.getSkippedObjects());

        JTabbedPane tabbedPane = init();

        addPane(tabbedPane, "Monitored Hosts", "Remove host", monitoredListModel);
        addPane(tabbedPane, "Skipped objects", "Remove object", skippedListModel);

        pack();
    }

    private JButton createRemoveButton(String name, final JList list) {
        JButton button = new JButton(new AbstractAction(name) {
                    public void actionPerformed(ActionEvent e) {
                        int[] selected = list.getSelectedIndices();
                        DefaultListModel model = (DefaultListModel) list.getModel();
                        for (int i = selected.length - 1; i >= 0; i--)
                            model.remove(selected[i]);
                    }
                });

        return button;
    }

    private static DefaultListModel copyList(DefaultListModel dest,
        DefaultListModel src) {
        if (dest == null) {
            dest = new DefaultListModel();
        } else {
            dest.clear();
        }

        for (int i = 0; i < src.getSize(); i++) {
            dest.addElement(src.get(i));
        }

        return dest;
    }

    private JTabbedPane init() {
        setTitle("Monitored objects");
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(),
                javax.swing.BoxLayout.Y_AXIS));

        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    cancel();
                }
            });

        JTabbedPane tabbedPane = new javax.swing.JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        getContentPane().add(tabbedPane);

        JPanel buttons = new JPanel();
        buttons.setLayout(new javax.swing.BoxLayout(buttons,
                javax.swing.BoxLayout.X_AXIS));

        JButton ok = new JButton(new AbstractAction("OK") {
                    public void actionPerformed(ActionEvent e) {
                        ok();
                    }
                });

        JButton cancel = new JButton(new AbstractAction("Cancel") {
                    public void actionPerformed(ActionEvent e) {
                        cancel();
                    }
                });

        buttons.add(Box.createHorizontalGlue());
        buttons.add(cancel);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(ok);
        buttons.add(Box.createHorizontalGlue());

        getContentPane().add(buttons);

        return tabbedPane;
    }

    private static Box horizontalCenter(JComponent component) {
        Box centeredContainer = new Box(BoxLayout.X_AXIS);
        centeredContainer.add(Box.createHorizontalGlue());
        centeredContainer.add(component);
        centeredContainer.add(Box.createHorizontalGlue());

        return centeredContainer;
    }

    private void addPane(JTabbedPane tabbedPane, String name,
        String removeButtonName, DefaultListModel listModel) {
        Box container = new Box(BoxLayout.Y_AXIS);
        JList list = new JList(listModel);
        JButton removeButton = createRemoveButton(removeButtonName, list);

        list.setCellRenderer(new MonitoredObjectsListCellRenderer());

        container.add(Box.createVerticalGlue());
        container.add(horizontalCenter(new JScrollPane(list)));

        container.add(Box.createVerticalGlue());
        container.add(horizontalCenter(removeButton));

        container.add(Box.createVerticalGlue());

        tabbedPane.addTab(name, container);
    }

    private void cancel() {
        setVisible(false);
    }

    private void ok() {
        copyList(jobPanel.getMonitoredHosts(), monitoredListModel);
        copyList(jobPanel.getSkippedObjects(), skippedListModel);
        setVisible(false);
    }
}


class MonitoredObjectsListCellRenderer extends DefaultListCellRenderer
    implements JobMonitorConstants {
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected,
            cellHasFocus);

        BasicMonitoredObject object = (BasicMonitoredObject) value;
        int key = object.getKey();
        Icon icon = Icons.getIconForKey(key);
        if (icon != null) {
            setIcon(icon);
        }

        setText(object.getPrettyName());
        return this;
    }
}
