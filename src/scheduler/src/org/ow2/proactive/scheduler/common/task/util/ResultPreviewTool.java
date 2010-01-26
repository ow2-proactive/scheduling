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

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;


/**
 * Static util methods for result preview definition
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class ResultPreviewTool {

    public static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.SCHEDULER);

    /**
     * Name of the environment variable for windows home directory
     * on the common file system.
     */
    public static final String WINDOWS_HOME_ENV_VAR = SchedulerConstants.WINDOWS_HOME_ENV_VAR;

    /**
     * Name of the environment variable for unix home directory
     * on the common file system.
     */
    public static final String UNIX_HOME_ENV_VAR = SchedulerConstants.UNIX_HOME_ENV_VAR;

    /**
     * Convert path parameter into system compliant path on a common file system
     * @param path the path to convert
     * @return converted path
     */
    public static String getSystemCompliantPath(String path) {
        //Check home dir env variable
        String winHome = System.getenv(WINDOWS_HOME_ENV_VAR);
        String unixHome = System.getenv(UNIX_HOME_ENV_VAR);

        logger.info("[RESULT_PREVIEW] WINHOME: [" + winHome + "]");
        logger.info("[RESULT_PREVIEW] UNIXHOME: [" + unixHome + "]");

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
