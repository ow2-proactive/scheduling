package org.objectweb.proactive.ic2d.gui.jobmonitor;

import org.objectweb.proactive.ic2d.gui.jobmonitor.data.BasicMonitoredObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeNode;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;


class StatusCell extends JPanel implements JobMonitorConstants {
    private JLabel name;
    private JLabel state;
    private int key;
    private String clearedNameLabel;

    public StatusCell(int key) {
        this.key = key;
        this.clearedNameLabel = "No " + NAMES[KEY2INDEX[key]] +
            " in the hierarchy";
        setLayout(new GridLayout(3, 1));
        setBackground(Color.WHITE);

        JLabel title = new JLabel(NAMES[KEY2INDEX[key]],
                Icons.getIconForKey(key), SwingConstants.LEFT);

        add(title);

        name = prepareLabel();
        add(name);

        state = prepareLabel();
        add(state);
    }

    private static JLabel prepareLabel() {
        JLabel label = new JLabel();
        label.setFont(label.getFont().deriveFont(Font.PLAIN));

        return label;
    }

    public void clear() {
        setNameLabel(clearedNameLabel);
        state.setText("No state");
    }

    public void setNameLabel(String name) {
        this.name.setText(name);
    }

    private static String timeDiff(Calendar d1, Calendar d2) {
        long d1Sec = d1.getTimeInMillis() / 1000;
        long d2Sec = d2.getTimeInMillis() / 1000;

        long diff = d1Sec - d2Sec;
        if (diff < 120) {
            return diff + " seconds";
        }

        diff /= 60;
        if (diff < 120) {
            return diff + " minutes";
        }

        diff /= 60;
        if (diff < 120) {
            return diff + " hours";
        }

        diff /= 24;
        return diff + " days";
    }

    public void updateDeleted(GregorianCalendar deletedSince) {
        if (deletedSince == null) {
            state.setText("Alive");
        } else {
            GregorianCalendar now = new GregorianCalendar();
            String diff = timeDiff(now, deletedSince);
            state.setText("Unresponding for " + diff);
        }
    }

    public void repaint() {
        if (name != null) {
            name.repaint();
        }

        if (state != null) {
            state.repaint();
        }
    }
}


public class JobMonitorStatus extends JPanel implements JobMonitorConstants,
    TreeSelectionListener {
    private JTree tree;
    private StatusCell[] cells;

    public JobMonitorStatus(JTree tree) {
        this.tree = tree;
        this.cells = new StatusCell[NB_KEYS];

        tree.addTreeSelectionListener(this);
        setLayout(new GridLayout(cells.length, 1, 0, 20));
        setBackground(Color.WHITE);

        for (int i = 0; i < cells.length; i++) {
            StatusCell cell = new StatusCell(KEYS[i]);
            cells[i] = cell;
            cell.clear();
            add(cell);
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        DataTreeNode node = (DataTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        for (int i = 0; i < cells.length; i++)
            cells[i].clear();

        while (!node.isRoot()) {
            int i = KEY2INDEX[node.getKey()];
            StatusCell cell = cells[i];
            BasicMonitoredObject object = node.getObject();
            cell.setNameLabel(object.getFullName());
            cell.updateDeleted(object.getDeletedSince());
            node = (DataTreeNode) node.getParent();
        }

        for (int i = 0; i < cells.length; i++)
            cells[i].repaint();
    }
}
