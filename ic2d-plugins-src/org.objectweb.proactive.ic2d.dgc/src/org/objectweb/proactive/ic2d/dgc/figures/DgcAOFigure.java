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
package org.objectweb.proactive.ic2d.dgc.figures;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.eclipse.draw2d.Label;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;


class DgcLabel extends Label {
    private String text;

    public DgcLabel(String text) {
        super(text);
        this.text = text;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public String getSubStringText() {
        return getText();
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }
}

public class DgcAOFigure extends AOFigure {
    public DgcAOFigure(String text) {
        super(text);
        this.label = new DgcLabel(text + "\nDGC_STATE");
        this.initFigure();
    }

    public void updateDgcState(ActiveObject model) {
        NodeObject node = model.getParent();
        String state = "";
        try {
            state = (String) model.getAttribute("DgcState");
            ((DgcLabel) this.label).setText(model.getName() + "\n" + state);
        } catch (AttributeNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
