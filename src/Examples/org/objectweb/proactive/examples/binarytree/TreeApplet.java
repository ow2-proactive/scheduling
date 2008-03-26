/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.binarytree;

import java.io.IOException;
import java.text.CharacterIterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * The binary tree is an a recursive data structure. A tree is
 * composed of a root node, and each node has two potential child
 * nodes. Here, each node is an active object, allowing large data
 * structures to be distributed aver the network.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class TreeApplet extends org.objectweb.proactive.examples.StandardFrame {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private javax.swing.JPanel rootPanel;
    private javax.swing.Box pCmd;
    private javax.swing.JPanel searchResult;
    private javax.swing.JButton bAdd;
    private javax.swing.JButton bSearch;
    private javax.swing.JButton bDump;
    private javax.swing.JTextField tNode;
    private javax.swing.JTextField tKey;
    private javax.swing.JTextField tValue;
    private javax.swing.JTextField tKeysNb;
    private javax.swing.JScrollPane scrollTree;
    private javax.swing.JRadioButton continuation;
    private TreeDisplay display;
    private TreePanel treePanel;
    private SearchPane searchPane;

    // Flag who indicates if Automataic Continuations
    // are enabled
    private boolean AC = false;

    // ArrayList who contains keys to do a multi-research
    private java.util.ArrayList keys;
    private java.util.ArrayList futurs;

    public TreeApplet() {
        super();
    }

    public TreeApplet(String name, Integer width, Integer height) {
        super(name, width.intValue(), height.intValue());
        verticalSplitPane.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                refresh();
            }
        });

        // Create the DisplayManager
        try {
            display = new TreeDisplay(this);
            display = (TreeDisplay) org.objectweb.proactive.api.PAActiveObject.turnActive(display);
            treePanel.setDisplay(display);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] arg) {
        try {
            org.objectweb.proactive.api.PAActiveObject.newActive(TreeApplet.class.getName(), new Object[] {
                    "Binary Tree", new Integer(900), new Integer(600) });
        } catch (ActiveObjectCreationException e) {
        } catch (NodeException e) {
        }
    }

    public void displayTree() {
        if (treePanel != null) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    treePanel.repaint();
                }
            });
        }
    }

    @Override
    protected void start() {
    }

    @Override
    protected javax.swing.JPanel createRootPanel() {
        rootPanel = new javax.swing.JPanel(new java.awt.BorderLayout());

        javax.swing.JPanel westPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        pCmd = javax.swing.Box.createVerticalBox();

        // Create Area
        javax.swing.JPanel panel = new javax.swing.JPanel();
        tNode = new javax.swing.JTextField("", 3);
        javax.swing.JButton create = new javax.swing.JButton("Create Binary Tree");
        create.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String nodeNb = tNode.getText();
                int nb = 0;
                try {
                    // We re-initialize automatic continuations
                    continuation.setSelected(true);
                    AC = true;
                    nb = (new Integer(nodeNb)).intValue();
                    display.createTree(nb, AC);
                } catch (NumberFormatException ex) {
                    receiveMessage("You must enter an integer size of the tree!", java.awt.Color.red);
                }
            }
        });
        panel.add(create);
        panel.add(new javax.swing.JLabel("Size:"));
        panel.add(tNode);
        pCmd.add(panel);

        // Text Area
        panel = new javax.swing.JPanel();
        tKey = new javax.swing.JTextField("", 15);
        panel.add(new javax.swing.JLabel("Key"));
        panel.add(tKey);
        pCmd.add(panel);

        panel = new javax.swing.JPanel();
        tValue = new javax.swing.JTextField("", 15);
        panel.add(new javax.swing.JLabel("Value"));
        panel.add(tValue);
        pCmd.add(panel);

        // Button placement
        panel = new javax.swing.JPanel();
        javax.swing.JButton bAdd = new javax.swing.JButton("Add");
        bAdd.setToolTipText("Add a node to the tree");
        bAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String key = tKey.getText();
                String value = tValue.getText();
                if ((key == null) || (value == null)) {
                    receiveMessage("You must specify a Key/Value couple!!!", java.awt.Color.red);
                } else {
                    display.add(key, value, AC);
                }
            }
        });
        panel.add(bAdd);

        javax.swing.JButton bSearch = new javax.swing.JButton("Search");
        bSearch.setToolTipText("Research the value of the key");
        bSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String key = tKey.getText();
                if (key == null) {
                    return;
                }
                tValue.setText((display.search(key)).toString());
            }
        });
        panel.add(bSearch);

        javax.swing.JButton bDump = new javax.swing.JButton("Dump tree");
        bDump.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                displayTree();
            }
        });
        panel.add(bDump);

        pCmd.add(panel);

        pCmd.add(javax.swing.Box.createVerticalStrut(30));

        // Generate Keys to search
        panel = new javax.swing.JPanel();
        tKeysNb = new javax.swing.JTextField("", 3);
        javax.swing.JButton generateK = new javax.swing.JButton("Generate Keys");
        generateK.setToolTipText("Generate keys to research values");
        generateK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String keyNb = tKeysNb.getText();
                try {
                    int nb = (new Integer(keyNb)).intValue();
                    keys = display.getRandomKeys(nb);
                    searchPane.clear();
                    java.util.Iterator it = keys.iterator();
                    while (it.hasNext()) {
                        java.util.Vector curLine = new java.util.Vector();
                        String[] kv = new String[3];
                        kv[0] = (String) it.next();
                        kv[1] = "Unknown";
                        kv[2] = "";
                        searchPane.updateKeyValue(kv);
                    }
                } catch (NumberFormatException ex) {
                    receiveMessage("You must enter an integer number of keys!", java.awt.Color.red);
                }
            }
        });
        panel.add(generateK);
        panel.add(new javax.swing.JLabel("Number:"));
        panel.add(tKeysNb);
        pCmd.add(panel);

        // Research Panel
        searchPane = new SearchPane();
        pCmd.add(searchPane);

        // Automatic Continuations Button
        panel = new javax.swing.JPanel();
        continuation = new javax.swing.JRadioButton("Automatic Continuations");
        continuation.setToolTipText("Enabled/Disabled Automatic Continuations");
        continuation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (AC) {
                    try {
                        PAActiveObject.disableAC(display);
                        display.disableAC();
                        receiveMessage("Automatic continuation disabled...", new java.awt.Color(200, 100, 0));
                        AC = false;
                    } catch (IOException e1) {
                    }
                } else {
                    try {
                        PAActiveObject.enableAC(display);
                        display.enableAC();
                        receiveMessage("Automatic continuation enabled...", new java.awt.Color(200, 100, 0));
                        AC = true;
                    } catch (IOException e1) {
                    }
                }
            }
        });
        panel.add(continuation);
        pCmd.add(panel);

        panel = new javax.swing.JPanel();
        javax.swing.JButton search = new javax.swing.JButton("Search Values");
        search.setToolTipText("Research values of the keys");
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    java.util.Iterator it = keys.iterator();
                    java.util.Vector<ObjectWrapper> tab1 = new java.util.Vector<ObjectWrapper>();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        tab1.add(display.search(key));
                    }

                    int lng = keys.size();
                    java.util.ArrayList<Integer> vKeys = new java.util.ArrayList<Integer>();
                    java.util.ArrayList<String[]> res = new java.util.ArrayList<String[]>();
                    for (int i = 0; i < lng; i++) {
                        vKeys.add(new Integer(i));
                    }

                    for (int i = 0; i < lng; i++) {
                        int key = 0;
                        key = PAFuture.waitForAny(tab1);
                        res.add(new String[] { (String) (keys.get(vKeys.remove(key).intValue())),
                                tab1.get(key).toString(), "" + i });
                        tab1.remove(key);
                    }

                    Thread t = new RefreshThread(res);
                    t.start();

                    receiveMessage("Search in progress...", new java.awt.Color(200, 100, 100));
                } catch (NullPointerException ex) {
                    receiveMessage("You must have keys to search!", java.awt.Color.red);
                }
            }
        });
        panel.add(search);
        pCmd.add(panel);

        westPanel.add(pCmd, java.awt.BorderLayout.NORTH);
        rootPanel.add(westPanel, java.awt.BorderLayout.WEST);

        treePanel = new TreePanel(display);
        scrollTree = new javax.swing.JScrollPane();
        scrollTree.getViewport().add(treePanel);
        rootPanel.add(scrollTree, java.awt.BorderLayout.CENTER);

        return rootPanel;
    }

    public void refresh() {
        verticalSplitPane.validate();
        verticalSplitPane.repaint();
    }

    public class TreePanel extends javax.swing.JPanel {
        private TreeDisplay display;

        public TreePanel(TreeDisplay display) {
            this.display = display;
            setVisible(true);
            setBackground(java.awt.Color.white);
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    refresh();
                }
            });
        }

        @Override
        public void repaint() {
            logger.info(Thread.currentThread());
            super.repaint();
        }

        public void setDisplay(TreeDisplay display) {
            this.display = display;
        }

        @Override
        public void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            try {
                if (display != null) {
                    Tree tree = display.getTree();
                    if (tree != null) {
                        try {
                            // Dimension depending to the depth of tree
                            int width = 300;

                            // Dimension depending to the depth of tree
                            int height = 600;
                            switch (tree.depth()) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                    break;
                                case 5:
                                    height = 1200;
                                    break;
                                case 6:
                                    height = 2400;
                                    break;
                                case 7:
                                case 8:
                                case 9:
                                    height = 5400;
                                    break;
                                default:
                                    width = tree.depth() * 60;
                                    height = 5400;
                                    break;
                            }
                            java.awt.Dimension newSize = new java.awt.Dimension(height, width);
                            if (!newSize.equals(getPreferredSize())) {
                                setPreferredSize(newSize);
                                scrollTree.getViewport().doLayout();
                            }
                            paintTree(g, tree, (int) height / 2, 30, tree.depth());
                        } catch (NullPointerException e) {
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        public void paintTree(java.awt.Graphics g, Tree tree, int x, int y, int depth) {
            if ((tree != null) && (tree.getKey() != null)) {
                // Analyse of the key size to paint the node
                int keySize = 5;
                java.text.StringCharacterIterator it1 = new java.text.StringCharacterIterator(tree.getKey());
                while (it1.current() != CharacterIterator.DONE) {
                    char currentChar = it1.next();
                    if ((currentChar == 'i') || (currentChar == 'l')) {
                        keySize = keySize + 3;
                    } else if ((currentChar == 'j') || (currentChar == 'f')) {
                        keySize = keySize + 6;
                    } else if ((currentChar == 'm') || (currentChar == 'w')) {
                        keySize = keySize + 10;
                    } else {
                        keySize = keySize + 9;
                    }
                }

                // Analyse of the value size to paint the node
                int valueSize = 5;
                java.text.StringCharacterIterator it2 = new java.text.StringCharacterIterator(tree.getValue());
                while (it2.current() != CharacterIterator.DONE) {
                    char currentChar = it2.next();
                    if ((currentChar == 'i') || (currentChar == 'l')) {
                        valueSize = valueSize + 3;
                    } else if ((currentChar == 'j') || (currentChar == 'f')) {
                        valueSize = valueSize + 6;
                    } else if ((currentChar == 'm') || (currentChar == 'w')) {
                        valueSize = valueSize + 10;
                    } else {
                        valueSize = valueSize + 9;
                    }
                }

                int size;
                if (valueSize > keySize) {
                    size = valueSize;
                } else {
                    size = keySize;
                }

                g.setColor(new java.awt.Color(240, 240, 240));
                g.fillRoundRect(x - (size / 2), y - 10, size, 40, 35, 35);

                g.setColor(java.awt.Color.black);
                g.drawRoundRect(x - (size / 2), y - 10, size, 40, 35, 35);
                g.drawLine(x - (size / 2), y + 10, x - (size / 2) + size, y + 10);
                g.drawString(tree.getKey(), x - (size / 2) + 5, y + 5);

                g.setColor(java.awt.Color.blue);
                g.drawString(tree.getValue(), x - (size / 2) + 5, y + 25);

                //Angle value
                int angle = 65;
                if (depth > 0) {
                    switch (depth) {
                        case 1:
                            break;
                        case 2:
                            angle = 50;
                            break;
                        case 3:
                            angle = 35;
                            break;
                        case 4:
                            angle = 20;
                            break;
                        case 5:
                            angle = 10;
                            break;
                        case 6:
                            angle = 5;
                            break;
                        case 7:
                            angle = 2;
                            break;
                        default:
                            depth = 7;
                            angle = 2;
                    }
                }

                try {
                    int rightX = (int) (x + (50 / Math.tan(Math.toRadians(angle))));
                    int rightY = y + 60;

                    paintTree(g, tree.getRight(), rightX, rightY, depth - 1);

                    g.setColor(java.awt.Color.lightGray);
                    g.drawLine(x + 4, y + 30, rightX + 1, rightY - 10);
                    g.drawLine(x + 2, y + 30, rightX - 1, rightY - 10);

                    g.setColor(java.awt.Color.black);
                    g.drawLine(x + 3, y + 30, rightX, rightY - 10);
                } catch (NullPointerException e) {
                    //We stop the right iteration in tree, if a
                    //NullPointerException is detected
                }

                try {
                    int leftX = (int) (x + (50 / Math.tan(Math.toRadians(180 - angle))));
                    int leftY = y + 60;

                    paintTree(g, tree.getLeft(), leftX, leftY, depth - 1);

                    g.setColor(java.awt.Color.lightGray);
                    g.drawLine(x - 4, y + 30, leftX - 1, leftY - 10);
                    g.drawLine(x - 2, y + 30, leftX + 1, leftY - 10);

                    g.setColor(java.awt.Color.black);
                    g.drawLine(x - 3, y + 30, leftX, leftY - 10);
                } catch (NullPointerException e) {
                    //We stop the left iteration in tree, if a
                    //NullPointerException is detected
                }
            }
        }
    }

    public class RefreshThread extends Thread {
        java.util.ArrayList<String[]> list;

        public RefreshThread(java.util.ArrayList<String[]> list) {
            this.list = list;
        }

        @Override
        public void run() {
            java.util.Iterator<String[]> it = list.iterator();
            while (it.hasNext()) {
                try {
                    Thread.sleep(500);
                    searchPane.updateKeyValue((it.next()));
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
