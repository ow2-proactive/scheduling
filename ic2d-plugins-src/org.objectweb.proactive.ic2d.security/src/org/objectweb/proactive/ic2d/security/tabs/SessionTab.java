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
package org.objectweb.proactive.ic2d.security.tabs;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.Session;
import org.objectweb.proactive.ic2d.security.widgets.CertificateDetailsSection;
import org.objectweb.proactive.ic2d.security.widgets.CommunicationDetailsComposite;


public class SessionTab extends UpdatableTab {
    private List<SessionWithID> sessionList;
    private FormToolkit toolkit;
    private Table sessionTable;
    private TableViewer sessionTableViewer;
    private CertificateDetailsSection certDetailsSection;
    private CommunicationDetailsComposite requestComposite;
    private CommunicationDetailsComposite replyComposite;

    public SessionTab(CTabFolder folder, FormToolkit tk) {
        super(folder, SWT.NULL);
        setText("Sessions browser");

        this.sessionList = new ArrayList<SessionWithID>();
        this.toolkit = tk;

        Composite body = this.toolkit.createComposite(folder);

        body.setLayout(new GridLayout(3, true));

        createSectionSessionList(body).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createSectionDistantCertificate(body).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createSectionContext(body).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        setControl(body);
    }

    private Section createSectionSessionList(Composite parent) {
        Section section = this.toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
        section.setText("Sessions List");

        Composite client = this.toolkit.createComposite(section);
        client.setLayout(new GridLayout());

        createListSessions(client);

        section.setClient(client);

        return section;
    }

    private Table createListSessions(Composite parent) {
        this.sessionTable = this.toolkit.createTable(parent, SWT.NULL);
        this.sessionTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        this.sessionTableViewer = new TableViewer(this.sessionTable);

        this.sessionTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateViewers();

                super.widgetSelected(e);
            }
        });

        return this.sessionTable;
    }

    private Section createSectionDistantCertificate(Composite parent) {
        this.certDetailsSection = new CertificateDetailsSection(parent, this.toolkit);

        return this.certDetailsSection.get();
    }

    private Section createSectionContext(Composite parent) {
        Section section = this.toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
        section.setText("Communication");

        Composite client = this.toolkit.createComposite(section);
        client.setLayout(new GridLayout());

        this.requestComposite = new CommunicationDetailsComposite(client, this.toolkit, "Request");
        this.requestComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.replyComposite = new CommunicationDetailsComposite(client, this.toolkit, "Reply");
        this.replyComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        section.setClient(client);

        return section;
    }

    private void updateSessionTable() {
        this.sessionTable.removeAll();
        for (SessionWithID session : this.sessionList) {
            this.sessionTableViewer.add(session.getThisID() + " -> " +
                session.getSession().getDistantSessionID());
        }
        updateViewers();
    }

    protected void updateViewers() {
        Session session = this.sessionList.get(this.sessionTable.getSelectionIndex()).getSession();

        this.certDetailsSection.update(session.getDistantCertificate());

        SecurityContext sc = session.getSecurityContext();

        this.requestComposite.updateCommunication(sc.getSendRequest());

        this.replyComposite.updateCommunication(sc.getSendReply());
    }

    public void setSessions(Hashtable<Long, Session> sessions) {
        this.sessionList.clear();
        for (Long id : sessions.keySet()) {
            this.sessionList.add(new SessionWithID(sessions.get(id), id.longValue()));
        }
    }

    @Override
    public void update() {
        updateSessionTable();
    }

    private class SessionWithID {
        private Session session;
        private long id;

        public SessionWithID(Session session, long id) {
            this.session = session;
            this.id = id;
        }

        public Session getSession() {
            return this.session;
        }

        public long getThisID() {
            return this.id;
        }
    }
}
