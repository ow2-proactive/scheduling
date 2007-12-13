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
package org.objectweb.proactive.ic2d.security.actions;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.objectweb.proactive.core.security.PolicyRule;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.crypto.Session;
import org.objectweb.proactive.core.security.securityentity.RuleEntity;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;
import org.objectweb.proactive.ic2d.security.core.KeystoreUtils;
import org.objectweb.proactive.ic2d.security.core.SimplePolicyRule;
import org.objectweb.proactive.ic2d.security.perspectives.SecurityPerspective;
import org.objectweb.proactive.ic2d.security.views.PolicyEditorView;


public class GetSecurityManager extends Action implements IActionExtPoint {
    public static final String GET_SECURITY_MANAGER = "Get Security Manager";
    private AbstractData object;

    public GetSecurityManager() {
        setId(GET_SECURITY_MANAGER);
        setToolTipText("Export SM to Policy view");
        setText("Export SM to Policy view");
        setEnabled(false);
    }

    @Override
    public final void run() {
        ProActiveSecurityManager psm = null;
        try {
            psm = (ProActiveSecurityManager) this.object.invoke("getSecurityManager", new Object[] { null },
                    new String[] { "org.objectweb.proactive.core.security.securityentity.Entity" });
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        try {
            IWorkbench iworkbench = PlatformUI.getWorkbench();
            IWorkbenchPage page = iworkbench.showPerspective(SecurityPerspective.ID, iworkbench
                    .getActiveWorkbenchWindow());
            IViewPart part = page.showView(PolicyEditorView.ID);

            PolicyEditorView pev = (PolicyEditorView) part;

            List<SimplePolicyRule> sprl = new ArrayList<SimplePolicyRule>();
            for (PolicyRule policy : psm.getPolicies()) {
                sprl.add(prToSpr(policy));
            }

            List<String> users = new ArrayList<String>();
            for (RuleEntity entity : psm.getAccessAuthorizations()) {
                users.add(entity.getName());
            }

            Hashtable<Long, Session> sessions = new Hashtable<Long, Session>();
            sessions.putAll(psm.getSessions());

            pev.update(KeystoreUtils.listKeystore(psm.getKeyStore()), sprl, psm.getApplicationName(), users,
                    sessions);
        } catch (WorkbenchException e2) {
            e2.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private SimplePolicyRule prToSpr(PolicyRule policy) {
        SimplePolicyRule rule = new SimplePolicyRule();
        for (RuleEntity entity : policy.getEntitiesFrom()) {
            rule.addFrom(entity.getType() + ":" + entity.getName());
        }
        for (RuleEntity entity : policy.getEntitiesTo()) {
            rule.addTo(entity.getType() + ":" + entity.getName());
        }
        rule.setRepAuth(policy.getCommunicationReply().getAuthentication());
        rule.setRepConf(policy.getCommunicationReply().getConfidentiality());
        rule.setRepInt(policy.getCommunicationReply().getIntegrity());
        rule.setReply(policy.getCommunicationReply().isCommunicationAllowed());
        rule.setReqAuth(policy.getCommunicationRequest().getAuthentication());
        rule.setReqConf(policy.getCommunicationRequest().getConfidentiality());
        rule.setReqInt(policy.getCommunicationRequest().getIntegrity());
        rule.setRequest(policy.getCommunicationRequest().isCommunicationAllowed());
        rule.setAoCreation(policy.isAoCreation());
        rule.setMigration(policy.isMigration());

        return rule;
    }

    public void setAbstractDataObject(AbstractData ref) {
        this.object = ref;
        super.setEnabled(this.object instanceof ActiveObject || this.object instanceof RuntimeObject ||
            this.object instanceof NodeObject);
    }

    public void setActiveSelect(AbstractData ref) {
        // TODO Auto-generated method stub
    }
}
