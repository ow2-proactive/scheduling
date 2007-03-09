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
package org.objectweb.proactive.ic2d.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.objectweb.proactive.ic2d.gui.data.ActiveObjectPanel;


public class Legend extends JFrame {
    private static Legend uniqueInstance;

    public static Legend uniqueInstance() {
        return (uniqueInstance == null) ? (uniqueInstance = new Legend())
                                        : uniqueInstance;
    }

    private Legend() {
        super("World Panel Legend");
        setSize(500, 500);
        {
            GridBagLayout gridBagLayout;
            getContentPane().setLayout(gridBagLayout = new GridBagLayout());

            JPanel activeObjectsPanel = new JPanel(new GridLayout(-1, 2, 5, 5));
            getContentPane().add(activeObjectsPanel);
            activeObjectsPanel.setBorder(new TitledBorder("Active objects"));
            gridBagLayout.setConstraints(activeObjectsPanel,
                new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;
                            g.setColor(ActiveObjectPanel.COLOR_WHEN_ACTIVE);
                            g.fillOval(w / 4, 0, w / 2, h);
                        }
                    };
                activeObjectsPanel.add(comp);
                activeObjectsPanel.add(new JLabel("Active by itself"));
            }

            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;

                            g.setColor(ActiveObjectPanel.COLOR_WHEN_SERVING_REQUEST);
                            g.fillOval(w / 4, 0, w / 2, h);
                        }
                    };
                activeObjectsPanel.add(comp);
                activeObjectsPanel.add(new JLabel("Serving request"));
            }

            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;

                            g.setColor(ActiveObjectPanel.COLOR_WHEN_WAITING_REQUEST);
                            g.fillOval(w / 4, 0, w / 2, h);
                        }
                    };
                activeObjectsPanel.add(comp);
                activeObjectsPanel.add(new JLabel("Waiting for request"));
            }

            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;
                            g.setColor(ActiveObjectPanel.COLOR_WHEN_WAITING_BY_NECESSITY);
                            g.fillOval(w / 4, 0, w / 2, h);
                        }
                    };
                activeObjectsPanel.add(comp);
                activeObjectsPanel.add(new JLabel(
                        "Waiting for result (wait by necessity)"));
            }

            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;

                            g.setColor(ActiveObjectPanel.COLOR_WHEN_MIGRATING);
                            g.fillOval(w / 4, 0, w / 2, h);
                        }
                    };
                activeObjectsPanel.add(comp);
                activeObjectsPanel.add(new JLabel("Migrating"));
            }

            JPanel requestQueuePanel = new JPanel(new GridLayout(2, 2, 0, 0));
            getContentPane().add(requestQueuePanel);
            requestQueuePanel.setBorder(new TitledBorder("Pending Requests"));
            gridBagLayout.setConstraints(requestQueuePanel,
                new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;

                            g.setColor(ActiveObjectPanel.COLOR_WHEN_SERVING_REQUEST);
                            g.fillOval(w / 4, 0, w / 2, h);

                            g.setColor(ActiveObjectPanel.COLOR_REQUEST_SINGLE);
                            for (int i = 0; i < 5; i++)
                                g.fillRect((w / 2) - 15 + (i * 6), 2, 4, 4);

                            g.setColor(ActiveObjectPanel.COLOR_REQUEST_SEVERAL);
                            for (int i = 0; i < 3; i++)
                                g.fillRect((w / 2) - 9 + (i * 6), h - 6, 4, 4);
                        }
                    };
                requestQueuePanel.add(comp);
                requestQueuePanel.add(new JLabel("Pending requests:"));
                requestQueuePanel.add(new JLabel());
                JPanel requestQueuePanel2 = new JPanel(new GridLayout(1, 3));
                requestQueuePanel.add(requestQueuePanel2);
                requestQueuePanel2.add(new JLabel("1  ",
                        new Icon() {
                        public int getIconHeight() {
                            return 4;
                        }

                        public int getIconWidth() {
                            return 4;
                        }

                        public void paintIcon(Component c, Graphics g, int x,
                                int y) {
                            g.setColor(ActiveObjectPanel.COLOR_REQUEST_SINGLE);
                            g.fillRect(x, y, 4, 4);
                        }
                    }, JLabel.LEFT));
                requestQueuePanel2.add(new JLabel(ActiveObjectPanel.NUMBER_OF_REQUESTS_FOR_SEVERAL +
                        "  ",
                        new Icon() {
                        public int getIconHeight() {
                            return 4;
                        }

                        public int getIconWidth() {
                            return 4;
                        }

                        public void paintIcon(Component c, Graphics g, int x,
                                int y) {
                            g.setColor(ActiveObjectPanel.COLOR_REQUEST_SEVERAL);
                            g.fillRect(x, y, 4, 4);
                        }
                    }, JLabel.LEFT));
                requestQueuePanel2.add(new JLabel(ActiveObjectPanel.NUMBER_OF_REQUESTS_FOR_MANY +
                        "  ",
                        new Icon() {
                        public int getIconHeight() {
                            return 4;
                        }

                        public int getIconWidth() {
                            return 4;
                        }

                        public void paintIcon(Component c, Graphics g, int x,
                                int y) {
                            g.setColor(ActiveObjectPanel.COLOR_REQUEST_MANY);
                            g.fillRect(x, y, 4, 4);
                        }
                    }, JLabel.LEFT));
            }

            JPanel nodePanel = new JPanel(new GridLayout(-1, 2, 5, 5));
            getContentPane().add(nodePanel);
            nodePanel.setBorder(new TitledBorder("Nodes"));
            gridBagLayout.setConstraints(nodePanel,
                new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;
                            g.setColor(new Color(0xd0, 0xd0, 0xe0));
                            g.fillRect(w / 4, 0, w / 2, h);
                        }
                    };
                nodePanel.add(comp);
                nodePanel.add(new JLabel("RMI Node"));
            }

            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;

                            g.setColor(java.awt.Color.orange);
                            g.fillRect(w / 4, 0, w / 2, h);
                        }
                    };
                nodePanel.add(comp);
                nodePanel.add(new JLabel("HTTP Node"));
            }
            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;

                            g.setColor(java.awt.Color.white);
                            g.fillRect(w / 4, 0, w / 2, h);
                        }
                    };
                nodePanel.add(comp);
                nodePanel.add(new JLabel("RMI/SSH Node"));
            }

            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;

                            g.setColor(java.awt.Color.cyan);
                            g.fillRect(w / 4, 0, w / 2, h);
                        }
                    };
                nodePanel.add(comp);
                nodePanel.add(new JLabel("JINI Node"));
            }

            JPanel jvmPanel = new JPanel(new GridLayout(-1, 2, 5, 5));
            getContentPane().add(jvmPanel);
            jvmPanel.setBorder(new TitledBorder("JVMs"));
            gridBagLayout.setConstraints(jvmPanel,
                new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

            {
                JComponent comp = new JPanel() {
                        private int w = 100;
                        private int h = 50;

                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;
                            g.setColor(new Color(0xd0, 0xd0, 0xd0));
                            g.fillRect(w / 4, 0, w / 2, h);
                        }
                    };
                jvmPanel.add(comp);
                jvmPanel.add(new JLabel("Standard Jvm"));
            }

            {
                JComponent comp = new JPanel() {
                        private int w = 100;
                        private int h = 50;

                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;
                            g.setColor(new Color(0xff, 0xd0, 0xd0));
                            g.fillRect(w / 4, 0, w / 2, h);
                        }
                    };
                jvmPanel.add(comp);
                jvmPanel.add(new JLabel("Jvm started with Globus"));
            }

            /*
               {
                   JComponent comp = new JPanel() {
                           public void paintComponent(Graphics g) {
                               Dimension dim = getSize();
                               int w = dim.width;
                               int h = dim.height;
                               g.setColor(ActiveObjectPanel.COLOR_WHEN_NOT_RESPONDING);
                                                           g.fillRect(w / 4, 0, w / 2, h);
                           }
                       };
                                   jvmPanel.add(comp);
                                   jvmPanel.add(new JLabel("Jvm not responding"));
               }
             */
            JPanel hostPanel = new JPanel(new GridLayout(-1, 2, 5, 5));
            getContentPane().add(hostPanel);
            hostPanel.setBorder(new TitledBorder("Hosts"));
            gridBagLayout.setConstraints(hostPanel,
                new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

            {
                JComponent comp = new JPanel() {
                        private int w = 100;
                        private int h = 50;

                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;
                            g.setColor(new Color(0xd0, 0xd0, 0xd0));
                            g.fillRect(w / 4, 0, w / 2, h);
                        }
                    };
                hostPanel.add(comp);
                hostPanel.add(new JLabel("Standard Host"));
            }

            JPanel notRespondingPanel = new JPanel(new GridLayout(-1, 2, 5, 5));
            getContentPane().add(notRespondingPanel);
            notRespondingPanel.setBorder(new TitledBorder("Not Responding"));
            gridBagLayout.setConstraints(notRespondingPanel,
                new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;
                            g.setColor(ActiveObjectPanel.COLOR_WHEN_NOT_RESPONDING);
                            g.fillOval(w / 4, 0, w / 2, h);
                        }
                    };
                notRespondingPanel.add(comp);
                notRespondingPanel.add(new JLabel("Active Object"));
            }
            {
                JComponent comp = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            Dimension dim = getSize();
                            int w = dim.width;
                            int h = dim.height;
                            g.setColor(ActiveObjectPanel.COLOR_WHEN_NOT_RESPONDING);
                            g.fillRect(w / 4, 0, w / 2, h);
                        }
                    };
                notRespondingPanel.add(comp);
                notRespondingPanel.add(new JLabel("JVM"));
            }

            getContentPane().validate();
        }

        addWindowListener(new WindowAdapter() {
                public void windowClosing() {
                    setVisible(false);
                }
            });
    }

    private void add(String name, JComponent comp) {
        JPanel pan = new JPanel(new FlowLayout());
        pan.add(new JLabel(name));
        pan.add(comp);
        getContentPane().add(pan);
    }

    public static void main(String[] argv) {
        Legend.uniqueInstance().setVisible(true);
    }
}
