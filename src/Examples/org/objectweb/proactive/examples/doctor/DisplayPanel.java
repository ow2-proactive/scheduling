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
package org.objectweb.proactive.examples.doctor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;


public class DisplayPanel extends javax.swing.JPanel implements Runnable {
    Dimension prefSize;
    int[] patState;
    int[] docState;
    int nPat;
    int nDoc;
    public java.awt.Color wellOn;
    public java.awt.Color sickOn;
    public java.awt.Color cureOn;
    public java.awt.Color patColor;
    public java.awt.Color docColor;
    Thread blinking;
    boolean on;

    public DisplayPanel() {
        prefSize = new java.awt.Dimension(200, 200);
        patState = new int[Office.MAX_PAT];
        docState = new int[Office.MAX_DOC];
        nPat = nDoc = 0;

        wellOn = new java.awt.Color(0, 255, 0);
        sickOn = new java.awt.Color(255, 0, 0);
        cureOn = new java.awt.Color(0, 0, 255);

        patColor = new java.awt.Color(0, 64, 0);
        docColor = new java.awt.Color(0, 0, 64);

        on = true;
        blinking = new Thread(this);
        blinking.start();
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
            }
            on = (!on);
            repaint();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Graphics g = getGraphics();

        FontMetrics fm = g.getFontMetrics();
        int wP = fm.stringWidth("Patient " + Office.MAX_PAT);
        int wD = fm.stringWidth("Doctor " + Office.MAX_DOC);
        int h = fm.getAscent();

        return new Dimension(130 + wP + wD + h, 80 + ((h + 10) * (Office.NB_PAT - 1)));
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(100, 100);
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        int i;
        int w;
        int h;

        java.awt.Font oldF = g.getFont();
        java.awt.Dimension size = getSize();
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        java.awt.FontMetrics fm = g.getFontMetrics();
        w = fm.stringWidth("The Doctor's Office");
        g.drawString("The Doctor's Office", (size.width - w) / 2, 20);
        g.setFont(oldF);

        java.awt.Color bg = getBackground();

        fm = g.getFontMetrics();
        w = fm.stringWidth("Patient " + Office.MAX_PAT);
        h = fm.getAscent();

        for (i = 0; i < nDoc; i++) {
            if (docState[i] != Office.DOC_UNDEF) {
                int p = docState[i] - 1;
                g.setColor(bg);
                g.fillOval(60 + w, (50 + ((h + 10) * p)) - h, h, h);
                g.drawLine(60 + w + h, (50 + ((h + 10) * p)) - (h / 2), 80 + w, (50 + ((h + 10) * p)) -
                    (h / 2));
                g.fillOval(80 + w, (50 + ((h + 10) * p)) - h, h, h);
                g.drawString("Doctor " + (i + 1), 90 + w + h, 50 + ((h + 10) * p));
                docState[i] = Office.DOC_UNDEF;
            }
        }

        for (i = 0; i < nPat; i++) {
            g.setColor(patColor);
            g.drawString("Patient " + (i + 1), 50, 50 + ((h + 10) * i));

            switch (patState[i]) {
                case Office.PAT_WELL:
                case Office.PAT_SICK:
                    g.setColor((patState[i] == Office.PAT_WELL) ? wellOn : sickOn);
                    g.fillOval(60 + w, (50 + ((h + 10) * i)) - h, h, h);
                    break;
                default:
                    g.setColor(on ? cureOn : bg);
                    g.fillOval(60 + w, (50 + ((h + 10) * i)) - h, h, h);
                    g.drawLine(60 + w + h, (50 + ((h + 10) * i)) - (h / 2), 80 + w, (50 + ((h + 10) * i)) -
                        (h / 2));
                    g.fillOval(80 + w, (50 + ((h + 10) * i)) - h, h, h);

                    g.setColor(docColor);
                    g.drawString("Doctor " + patState[i], 90 + w + h, 50 + ((h + 10) * i));
                    break;
            }
        }
    }

    public void setPatState(int pat, int state) {
        patState[pat - 1] = state;
        repaint();
    }

    public void setDocFinished(int doc, int patient) {
        docState[doc - 1] = patient;
        repaint();
    }

    public void addDoctor(int ps) {
        docState[ps - 1] = Office.DOC_UNDEF;
        nDoc++;
        repaint();
    }

    public void addPatient(int ps) {
        patState[ps - 1] = Office.PAT_WELL;
        nPat++;
        repaint();
    }
}
