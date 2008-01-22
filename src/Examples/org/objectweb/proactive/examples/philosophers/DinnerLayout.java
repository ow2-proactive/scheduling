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
package org.objectweb.proactive.examples.philosophers;

import java.awt.GridBagConstraints;

import org.objectweb.proactive.ObjectForSynchronousCall;
import org.objectweb.proactive.api.PAActiveObject;


/**
 * DinnerLayout
 * This class acts as a wrapper for the ui.
 * It is there also that the active objects are initialized.
 */
public class DinnerLayout {

    /**
     * Reference to the philosopher's stub in order to dispatch messages from the UI
     */
    transient private Philosopher[] phils;

    /**
     * Reference to the actual LOCAL awt frame
     */
    transient private UserFrame display;

    /**
     * Reference to the Table manager, for bootstrap use.
     * In fact, the applet creates the layout
     * then the layout creates the active objects and
     * sends them a reference to its own stub
     */
    transient private Table manager;
    transient private String url;

    /**
     * The empty no args constructor commanded by papdc
     * it performs <B>ABSOLUTELY NOTHING</B> because it is called by papdc
     * before an explicit call. Any operation in this could cause memory leaks..
     */
    public DinnerLayout() {
    }

    /**
     * The real constructor
     * @param the array holding the Philosopher/Forks's images
     */
    public DinnerLayout(javax.swing.Icon[] images) {
        display = new UserFrame(images);
    }

    public void setNode(String url) {
        this.url = url;
    }

    public javax.swing.JPanel getDisplay() {
        return display;
    }

    /**
     * init
     * this method instanciates the remote objects,
     * as this <b>MUST NOT<b> be done in the constructor
     * [the getProxyOnThis method would fail, and we don't want that]
     */
    public ObjectForSynchronousCall init() {

        /**
         * This method instanciates the remote Table manager
         * The parameter passed to the new Table is the reference to the layout's stub
         */
        Object[] params; // the papdc arg holder
        params = new Object[1];
        params[0] = org.objectweb.proactive.api.PAActiveObject.getStubOnThis();

        // Creates the Table 
        try {
            manager = (Table) org.objectweb.proactive.api.PAActiveObject.newActive(Table.class.getName(),
                    params, url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // and the philosophers:
        phils = new Philosopher[5];

        // Creates the philosophers    
        params = new Object[3];
        params[1] = manager;
        params[2] = PAActiveObject.getStubOnThis();

        for (int n = 0; n < 5; n++) {
            params[0] = new Integer(n);
            try {
                phils[n] = (Philosopher) org.objectweb.proactive.api.PAActiveObject.newActive(
                        Philosopher.class.getName(), params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ObjectForSynchronousCall();
    }

    public void exit() {
        try {
            display.setVisible(false);
            display = null;
            System.exit(0);
        } catch (Throwable t) {
        }
    }

    /**
     * This method is called by the manager
     * after it has created the active Philosophers.
     */
    public void activateButtons() {
        display.activate();
    }

    /**
     * update
     * Updates the state's image of a philosopher [ie: when he eats]
     * @param index the philosopher's ID
     * @param the new state [0=think, 1=wait 2=eating]
     */
    public void update(int index, int state) {
        this.display.philButtons[index].changeState(state);
        this.display.philButtons[index].setEnabled(true);
    }

    /*
     * update fork state: @param index the fork's ID @param the new state [3=ontable, 4=inhand]
     */
    public void updateFork(int index, int state) {
        this.display.forkButtons[index].changeState(state);
    }

    /**
     * UserFrame
     * This is the <b>real</b> AWT Frame
     */
    protected class UserFrame extends javax.swing.JPanel implements java.awt.event.ActionListener,
            java.awt.event.MouseListener {
        protected javax.swing.JButton bQuit;
        protected javax.swing.JButton bAuto;

        /**
         * This array links a philosopher to its button
         */
        protected PhilPanel[] philButtons;
        protected ForkPanel[] forkButtons;
        protected boolean autopilot;

        public UserFrame(javax.swing.Icon[] images) {
            // Frame size and position
            // Autopilot initialization
            autopilot = false;

            /*
             * setBackground(java.awt.Color.lightGray); setFont(new java.awt.Font("SansSerif",
             * java.awt.Font.PLAIN, 12));
             */
            setSize(350, 300);
            java.awt.GridBagLayout grid = new java.awt.GridBagLayout();
            java.awt.GridBagConstraints cs = new java.awt.GridBagConstraints();
            this.setLayout(grid);
            cs.gridy = 0;

            // Philosophers' panels
            javax.swing.JPanel pPhil = new javax.swing.JPanel();
            java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
            java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();

            pPhil.setLayout(gridbag);

            // Philosophers
            philButtons = new PhilPanel[5];
            for (int i = 0; i < 5; i++) {
                philButtons[i] = new PhilPanel(images);
            }

            // Forks
            forkButtons = new ForkPanel[5];
            for (int i = 0; i < 5; i++) {
                forkButtons[i] = new ForkPanel(images);
            }

            c.gridx = 2;
            c.gridy = 1;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = c.weighty = 1.0;
            gridbag.setConstraints(philButtons[0], c);
            pPhil.add(philButtons[0]);

            c.gridx = 3;
            c.gridy = 1;
            gridbag.setConstraints(forkButtons[1], c);
            pPhil.add(forkButtons[1]);

            c.gridx = 4;
            c.gridy = 2;
            gridbag.setConstraints(philButtons[1], c);
            pPhil.add(philButtons[1]);

            c.gridx = 4;
            c.gridy = 3;
            gridbag.setConstraints(forkButtons[2], c);
            pPhil.add(forkButtons[2]);

            c.gridx = 3;
            c.gridy = 4;
            gridbag.setConstraints(philButtons[2], c);
            pPhil.add(philButtons[2]);

            c.gridx = 2;
            c.gridy = 4;
            gridbag.setConstraints(forkButtons[3], c);
            pPhil.add(forkButtons[3]);

            c.gridx = 1;
            c.gridy = 4;
            gridbag.setConstraints(philButtons[3], c);
            pPhil.add(philButtons[3]);

            c.gridx = 0;
            c.gridy = 3;
            gridbag.setConstraints(forkButtons[4], c);
            pPhil.add(forkButtons[4]);

            c.gridx = 0;
            c.gridy = 2;
            gridbag.setConstraints(philButtons[4], c);
            pPhil.add(philButtons[4]);

            c.gridx = 1;
            c.gridy = 1;
            gridbag.setConstraints(forkButtons[0], c);
            pPhil.add(forkButtons[0]);

            grid.setConstraints(pPhil, cs);
            this.add(pPhil);

            cs.gridy = 1;
            // Panel de commandes
            javax.swing.JPanel pCmd = new javax.swing.JPanel();

            // Quit
            bQuit = new javax.swing.JButton("Quit");
            bQuit.addActionListener(this);
            pCmd.add(bQuit);

            // Autopilot
            bAuto = new javax.swing.JButton("Autopilot");
            pCmd.add(bAuto);

            grid.setConstraints(pCmd, cs);
            this.add(pCmd);

            setVisible(true);
        }

        /*
         * Don't activate before the Philosophers are built
         */
        void activate() {
            bAuto.addActionListener(this);
            for (int i = 0; i < 5; i++) {
                philButtons[i].addMouseListener(this);
            }
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            Object source = e.getSource();
            if (source == bAuto) {
                if (!autopilot) {
                    bAuto.setText("Manual");
                    autopilot = true;
                } else {
                    bAuto.setText("Autopilot");
                    autopilot = false;
                }

                // Toggle the philosophers' state
                for (int i = 0; i < 5; i++)
                    phils[i].toggle();
            } else if (source == bQuit) {
                autopilot = false;
                exit();
            }
        }

        public void mousePressed(java.awt.event.MouseEvent e) {
        };

        public void mouseReleased(java.awt.event.MouseEvent e) {
        };

        public void mouseEntered(java.awt.event.MouseEvent e) {
        };

        public void mouseExited(java.awt.event.MouseEvent e) {
        };

        /**
         * mouseClicked
         * Called when the user clicks on the panel
         * used to toggle the state of the philosopher
         * @param e the event
         */
        public void mouseClicked(java.awt.event.MouseEvent e) {
            PhilPanel source = (PhilPanel) e.getSource();

            if (autopilot) { // We don't want no interferences when in autopilot mode
                return;
            }

            // Find the philosopher who has just been clicked upon
            int index = 0;
            while ((index < 5) && (philButtons[index] != source)) {
                index++;
            }

            // checks if the philosopher is eating
            switch (philButtons[index].state) {
                case 0:
                    // He's not eating
                    // (le philosophe fait un appel synchrone sur Table.getForks)
                    phils[index].getForks();
                    philButtons[index].changeState(1);
                    philButtons[index].setEnabled(false);
                    break;
                case 2:
                    phils[index].putForks();
                    philButtons[index].changeState(0);
                    philButtons[index].setEnabled(true);
                    break;
            }
        }
    }

    private class PhilPanel extends javax.swing.JLabel {

        /**
         * The array holding the images
         */
        private javax.swing.Icon[] imgPhil;

        /**
         * Current state
         */
        public int state;

        /**
         * PhilPanel
         * the constructor
         * @param imgPhil The array holding the images
         */
        public PhilPanel(javax.swing.Icon[] imgPhil) {
            super(imgPhil[0]);
            setPreferredSize(new java.awt.Dimension(70, 70));
            this.imgPhil = imgPhil;
            state = 0;
        }

        /**
         * changeState
         * Call this function when changing the picture diplayed
         * @param state the new state
         */

        /* Assert: state in [0-2] */
        public void changeState(int state) {
            this.state = state;
            setIcon(imgPhil[state]);
        }
    }

    private class ForkPanel extends javax.swing.JLabel {

        /**
         * The array holding the images (shared with the phils)
         * Forks images are indexed 3 (on the table) and 4 (in hand)
         */
        private javax.swing.Icon[] imgPhil;

        /**
         * Current state
         */
        public int state;

        /**
         * ForkPanel
         * the constructor
         * @param imgPhil The array holding the images
         */
        public ForkPanel(javax.swing.Icon[] imgPhil) {
            super(imgPhil[3]);
            setPreferredSize(new java.awt.Dimension(40, 40));
            this.imgPhil = imgPhil;
            state = 3;
        }

        /**
         * changeState
         * Call this function when changing the picture diplayed
         * @param state the new state
         */

        /* Assert: state in [3-4] */
        public void changeState(int state) {
            this.state = state;
            setIcon(imgPhil[state]);
        }
    }
}
