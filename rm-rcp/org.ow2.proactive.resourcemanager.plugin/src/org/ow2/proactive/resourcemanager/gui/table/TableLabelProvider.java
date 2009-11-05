/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.table;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.ow2.proactive.resourcemanager.gui.Activator;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;


public class TableLabelProvider extends ColumnLabelProvider {

    private static final int NS_COLUMN_NUMBER = 0;
    private static final int HOST_COLUMN_NUMBER = 1;
    private static final int STATE_COLUMN_NUMBER = 2;
    private static final int SINCE_COLUMN_NUMBER = 3;
    private static final int URL_COLUMN_NUMBER = 4;

    private int columnIndex;

    public TableLabelProvider(int columnNumber) {
        super();
        this.columnIndex = columnNumber;
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof Node && columnIndex == STATE_COLUMN_NUMBER) {
            switch (((Node) element).getState()) {
                case DOWN:
                    return Activator.getImageDescriptor("icons/down.gif").createImage();
                case FREE:
                    return Activator.getImageDescriptor("icons/free.gif").createImage();
                case BUSY:
                    return Activator.getImageDescriptor("icons/busy.gif").createImage();
                case TO_BE_RELEASED:
                    return Activator.getImageDescriptor("icons/to_release.gif").createImage();
            }
        }
        return null;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof Node) {
            Node node = (Node) element;
            String str = null;
            switch (columnIndex) {
                case NS_COLUMN_NUMBER:
                    str = node.getParent().getParent().getParent().getName();
                    break;
                case HOST_COLUMN_NUMBER:
                    str = node.getParent().getParent().getName();
                    break;
                case URL_COLUMN_NUMBER:
                    str = node.getName();
                    break;
                case SINCE_COLUMN_NUMBER:
                    str = node.getStateChangeTime();
                    break;
            }
            return str;
        }
        return null;
    }

    @Override
    public int getToolTipDisplayDelayTime(Object object) {
        return 800;
    }

    @Override
    public int getToolTipTimeDisplayed(Object object) {
        return 3000;
    }

    @Override
    public Point getToolTipShift(Object object) {
        return new Point(5, 5);
    }

    @Override
    public boolean useNativeToolTip(Object object) {
        return false;
    }

    @Override
    public String getToolTipText(Object element) {
        if (element instanceof Node && columnIndex == STATE_COLUMN_NUMBER) {
            switch (((Node) element).getState()) {
                case DOWN:
                    return "Node is down or unreachable";
                case FREE:
                    return "Node is ready to perform tasks";
                case BUSY:
                    return "Node is currently performing a task";
                case TO_BE_RELEASED:
                    return "Node is busy and will be removed at task's end";
            }
        }
        return null;
    }
}
