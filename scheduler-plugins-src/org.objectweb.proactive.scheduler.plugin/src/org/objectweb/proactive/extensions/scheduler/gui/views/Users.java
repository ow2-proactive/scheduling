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
package org.objectweb.proactive.extensions.scheduler.gui.views;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extensions.scheduler.common.job.UserIdentification;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerUsers;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.Tools;
import org.objectweb.proactive.extensions.scheduler.gui.data.JobsController;
import org.objectweb.proactive.extensions.scheduler.gui.listeners.SchedulerUsersListener;


/**
 * This view display many informations about a job.
 *
 * @author FRADJ Johann
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class Users extends ViewPart implements SchedulerUsersListener {

    /** an id */
    public static final String ID = "org.objectweb.proactive.extensions.scheduler.gui.views.Users";

    /** the unique id and the title for the column "Name" */
    public static final String COLUMN_NAME_TITLE = "Name";

    /** the unique id and the title for the column "Admin" */
    public static final String COLUMN_ADMIN_TITLE = "Admin";

    /** the unique id and the title for the column "# Job Submitted" */
    public static final String COLUMN_SUBMITTED_JOBS_TITLE = "# Job submitted";

    /** the unique id and the title for the column "Host name" */
    public static final String COLUMN_HOSTNAME_TITLE = "Host name";

    /** the unique id and the title for the column "Connected at" */
    public static final String COLUMN_CONNECTED_AT_TITLE = "Connected at";

    // The shared instance
    private static Users instance = null;
    private static boolean isDisposed = true;
    private Table table = null;

    //    private int order = InternalJob.ASC_ORDER;
    //    private int lastSorting = InternalJob.SORT_BY_ID;

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
    public void update(SchedulerUsers userss) {
        final SchedulerUsers users = userss;
        if (!table.isDisposed()) {
            table.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    // Turn off drawing to avoid flicker
                    table.setRedraw(false);

                    // We remove all the table entries and then add the new entries
                    table.removeAll();

                    Collection<UserIdentification> tmp = users.getUsers();

                    for (UserIdentification user : tmp)
                        createItem(user);

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
            if (title.equals(COLUMN_ADMIN_TITLE)) {
                item.setText(i, user.isAdmin() ? "Yes" : "");
            } else if (title.equals(COLUMN_CONNECTED_AT_TITLE)) {
                item.setText(i, Tools.getFormattedDate(user.getConnectionTime()));
            } else if (title.equals(COLUMN_HOSTNAME_TITLE)) {
                item.setText(i, user.getHostName());
            } else if (title.equals(COLUMN_NAME_TITLE)) {
                item.setText(i, user.getUsername());
            } else if (title.equals(COLUMN_SUBMITTED_JOBS_TITLE)) {
                item.setText(i, user.getSubmitNumber() + "");
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

    public void init() {
        SchedulerUsers users = JobsController.getLocalView().getUsers();
        if (users != null)
            update(users);
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

        TableColumn tc1 = new TableColumn(table, SWT.LEFT);
        TableColumn tc2 = new TableColumn(table, SWT.LEFT);
        TableColumn tc3 = new TableColumn(table, SWT.LEFT);
        TableColumn tc4 = new TableColumn(table, SWT.LEFT);
        TableColumn tc5 = new TableColumn(table, SWT.LEFT);

        // setText
        tc1.setText(COLUMN_NAME_TITLE);
        tc2.setText(COLUMN_ADMIN_TITLE);
        tc3.setText(COLUMN_SUBMITTED_JOBS_TITLE);
        tc4.setText(COLUMN_HOSTNAME_TITLE);
        tc5.setText(COLUMN_CONNECTED_AT_TITLE);
        // setWidth
        tc1.setWidth(150);
        tc2.setWidth(70);
        tc3.setWidth(150);
        tc4.setWidth(200);
        tc5.setWidth(100);
        // setMoveable
        tc1.setMoveable(true);
        tc2.setMoveable(true);
        tc3.setMoveable(true);
        tc4.setMoveable(true);
        tc5.setMoveable(true);

        JobsController.getLocalView().addSchedulerUsersListener(this);
        init();
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
