/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.scheduler.common.SchedulerUsers;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.listeners.SchedulerUsersListener;


/**
 * This view display many informations about a job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class Users extends ViewPart implements SchedulerUsersListener {

    /** an id */
    public static final String ID = "org.ow2.proactive.scheduler.gui.views.Users";

    /** the unique id and the title for the column "Name" */
    public static final String COLUMN_NAME_TITLE = "Name";

    /** the unique id and the title for the column "# Job Submitted" */
    public static final String COLUMN_SUBMITTED_JOBS_TITLE = "# Job submitted";

    /** the unique id and the title for the column "Host name" */
    public static final String COLUMN_HOSTNAME_TITLE = "Host name";

    /** the unique id and the title for the column "Connected at" */
    public static final String COLUMN_CONNECTED_AT_TITLE = "Connected at";

    /** the unique id and the title for the column "Last submitted job" */
    public static final String COLUMN_LAST_SUBMITTED_JOB_TITLE = "Last submitted job";

    // The shared instance
    private static Users instance = null;
    private static boolean isDisposed = true;
    private Table table = null;

    //    private int order = JobState.ASC_ORDER;
    //    private int lastSorting = JobState.SORT_BY_ID;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * The default constructor
     */
    public Users() {
        instance = this;
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- public ------------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * For display info about users
     *
     * @param job a job
     */
    public void update(final Collection<UserIdentification> users) {
        if (!table.isDisposed()) {
            table.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    // Turn off drawing to avoid flicker
                    table.setRedraw(false);

                    // We remove all the table entries and then add the new entries
                    table.removeAll();

                    for (UserIdentification user : users) {
                        createItem(user);
                    }

                    // Turn drawing back on
                    table.setRedraw(true);
                }
            });
        }
    }

    private TableItem createItem(UserIdentification user) {
        TableColumn[] cols = table.getColumns();
        TableItem item = new TableItem(table, SWT.NONE);
        item.setData(user.getUsername());

        for (int i = 0; i < cols.length; i++) {
            String title = cols[i].getText();
            if (title.equals(COLUMN_CONNECTED_AT_TITLE)) {
                item.setText(i, Tools.getFormattedDate(user.getConnectionTime()));
            } else if (title.equals(COLUMN_HOSTNAME_TITLE)) {
                item.setText(i, user.getHostName());
            } else if (title.equals(COLUMN_NAME_TITLE)) {
                item.setText(i, user.getUsername());
            } else if (title.equals(COLUMN_SUBMITTED_JOBS_TITLE)) {
                item.setText(i, user.getSubmitNumber() + "");
            } else if (title.equals(COLUMN_LAST_SUBMITTED_JOB_TITLE)) {
                item.setText(i, Tools.getFormattedDate(user.getLastSubmitTime()));
            }
        }
        return item;
    }

    /**
     * To clear the view
     */
    public void clear() {
        table.removeAll();
    }

    /**
     * to display or not the view
     *
     * @param isVisible
     */
    public void setVisible(boolean isVisible) {
        if (table != null) {
            table.setVisible(isVisible);
        }
    }

    /**
     * To enabled or not the view
     *
     * @param isEnabled
     */
    public void setEnabled(boolean isEnabled) {
        if (table != null) {
            table.setEnabled(isEnabled);
        }
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Users getInstance() {
        if (isDisposed) {
            return null;
        }
        return instance;
    }

    // -------------------------------------------------------------------- //
    // ------------------------- extends viewPart ------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        isDisposed = false;
        table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        List<TableColumn> cols = new ArrayList<TableColumn>();
        for (int i = 0; i < 6; i++) {
            TableColumn tc = new TableColumn(table, SWT.LEFT);
            tc.setMoveable(true);
            tc.setData(ID, i);
            tc.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    sort(event);

                }
            });
            cols.add(tc);
        }

        // setText
        cols.get(0).setText(COLUMN_NAME_TITLE);
        cols.get(1).setText(COLUMN_SUBMITTED_JOBS_TITLE);
        cols.get(2).setText(COLUMN_HOSTNAME_TITLE);
        cols.get(3).setText(COLUMN_CONNECTED_AT_TITLE);
        cols.get(4).setText(COLUMN_LAST_SUBMITTED_JOB_TITLE);
        // setWidth
        cols.get(0).setWidth(120);
        cols.get(1).setWidth(120);
        cols.get(2).setWidth(170);
        cols.get(3).setWidth(140);
        cols.get(4).setWidth(140);

        SchedulerUsers users = JobsController.getLocalView().getUsers();
        if (users != null) {
            update(users.getUsers());
        }
        JobsController.getLocalView().addSchedulerUsersListener(this);
    }

    private void sort(SelectionEvent event) {
        Collection<UserIdentification> col = JobsController.getLocalView().getUsers().getUsers();
        List<UserIdentification> users = new ArrayList<UserIdentification>();
        for (UserIdentification ui : col) {
            users.add(ui);
        }
        if ((TableColumn) event.widget == table.getSortColumn()) {
            //same column -> change order
            table.setSortDirection(table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
        } else {
            //different column -> order = DOWN
            table.setSortDirection(SWT.DOWN);
        }
        //set new sort column
        table.setSortColumn((TableColumn) event.widget);
        //get id of the column
        int id = (Integer) ((TableColumn) event.widget).getData(ID);
        //sort
        Collections.sort(users, new UserComparator(table.getSortDirection(), id));
        //update gui
        update(users);
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        isDisposed = true;
        JobsController.getLocalView().removeSchedulerUsersListener(this);
        super.dispose();
    }
}

class UserComparator implements Comparator<UserIdentification> {

    private int sortOrder;
    private int type;

    /**
     * Create a new instance of UserComparator
     *
     * @param upOrder a integer representing the sort order : values can be SWT.DOWN, SWT.UP
     * @param type an integer representing the column number to sort
     */
    public UserComparator(int sortOrder, int type) {
        this.sortOrder = sortOrder;
        this.type = type;
    }

    public int compare(UserIdentification o1, UserIdentification o2) {
        switch (type) {
            case 1:
                int i1 = o1.getSubmitNumber();
                int i2 = o2.getSubmitNumber();
                return (sortOrder == SWT.UP) ? (i2 - i1) : (i1 - i2);
            case 3:
                long l1 = o1.getConnectionTime();
                long l2 = o2.getConnectionTime();
                return (int) ((sortOrder == SWT.UP) ? (l2 - l1) : (l1 - l2));
            case 4:
                l1 = o1.getLastSubmitTime();
                l2 = o2.getLastSubmitTime();
                return (int) ((sortOrder == SWT.UP) ? (l2 - l1) : (l1 - l2));
            case 0:
                String s1 = o1.getUsername();
                String s2 = o2.getUsername();
                return (sortOrder == SWT.UP) ? s1.compareTo(s2) : s2.compareTo(s1);
            case 2:
                s1 = o1.getHostName();
                s2 = o2.getHostName();
                return (sortOrder == SWT.UP) ? s1.compareTo(s2) : s2.compareTo(s1);
        }
        return 0;
    }

}
