/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * Static util methods for result preview definition
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class ResultPreviewTool {

    /**
     * Name of the environment variable for windows home directory
     * on the common file system.
     */
    public static final String WINDOWS_HOME_ENV_VAR = PASchedulerProperties.WINDOWS_HOME_ENV_VAR
            .getValueAsString();

    /**
     * Name of the environment variable for unix home directory
     * on the common file system.
     */
    public static final String UNIX_HOME_ENV_VAR = PASchedulerProperties.UNIX_HOME_ENV_VAR.getValueAsString();

    /**
     * Convert path parameter into system compliant path on a common file system
     * @param path the path to convert
     * @return converted path
     */
    public static String getSystemCompliantPath(String path) {
        //Check home dir env variable
        String winHome = System.getenv(WINDOWS_HOME_ENV_VAR);
        String unixHome = System.getenv(UNIX_HOME_ENV_VAR);

        System.out.println("[RESULT_PREVIEW] WINHOME: [" + winHome + "]");
        System.out.println("[RESULT_PREVIEW] UNIXHOME: [" + unixHome + "]");

        if ((winHome == null) || (unixHome == null)) {
            System.err.println("[RESULT_PREVIEW] Warning : home directories variables are not set !");
            return path;
        }

        if (System.getProperty("os.name").contains("Windows")) {
            // on windows
            if (path.contains("/") && !path.contains("\\")) {
                // convert unix path to windows path
                path = path.replace(unixHome, winHome);
                path = path.replace('/', '\\');
            }
            return path;
        } else {
            // on linux
            if (path.contains(":\\") || path.contains("\\\\")) {
                // convert windows path to unix path
                path = path.replace(winHome, unixHome);
                path = path.replace('\\', '/');
            }
            return path;
        }
    }

    /**
     * Simple JPanel for displaying image
     * @author The ProActive Team
     * @since 3.9
     */
    public static class SimpleImagePanel extends JPanel {
        private transient Image img;
        private String path;

        /**
         * Create a new instance of SimpleImagePanel.
         *
         * @param path
         */
        public SimpleImagePanel(String path) {
            this.path = path;
            this.img = new ImageIcon(path).getImage();
            setBackground(Color.DARK_GRAY);
            setDoubleBuffered(true);
            Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
            setSize(size);
            setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            setLayout(null);
        }

        /**
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        protected void paintComponent(Graphics g) {
            g.clearRect(0, 0, this.getWidth(), this.getHeight());
            g.drawImage(img, 0, 0, null);
        }

        /**
         * @see java.awt.Component#toString()
         */
        @Override
        public String toString() {
            return "Simple image panel for " + this.path;
        }
    }

    /**
     * Simple JPanel for displaying text message
     * @author The ProActive Team
     * @since 3.9
     */
    public static class SimpleTextPanel extends JPanel {
        private String text;
        JTextArea textZone = new JTextArea(15, 40);

        /**
         * Create a new instance of SimpleTextPanel.
         *
         * @param text the test to be displayed.
         */
        public SimpleTextPanel(String text) {
            this.text = text;
            setBackground(Color.DARK_GRAY);
            setLayout(new BorderLayout());
            Font f = new Font("Arial", Font.BOLD, 12);
            textZone.setFont(f);
            add(textZone, BorderLayout.CENTER);
            textZone.setEditable(false);
            textZone.setMargin(new Insets(7, 7, 7, 7));
            textZone.setText(text);
            setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        }

        /**
         * @see java.awt.Component#toString()
         */
        @Override
        public String toString() {
            return "Simple text panel for " + this.text;
        }
    }
}
