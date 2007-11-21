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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.PolicyRule;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.security.securityentity.CertificatedRuleEntity;
import org.objectweb.proactive.core.security.securityentity.RuleEntities;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;
import org.objectweb.proactive.ic2d.jmxmonitoring.perspective.MonitoringPerspective;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;
import org.objectweb.proactive.ic2d.security.core.KeystoreUtils;
import org.objectweb.proactive.ic2d.security.core.SimplePolicyRule;
import org.objectweb.proactive.ic2d.security.perspectives.SecurityPerspective;
import org.objectweb.proactive.ic2d.security.views.PolicyEditorView;


public class SetSecurityManager extends Action implements IActionExtPoint {
    public static final String SET_SECURITY_MANAGER = "Set Security Manager";
    private AbstractData object;

    public SetSecurityManager() {
        setId(SET_SECURITY_MANAGER);
        setToolTipText("Import SM from Policy view");
        setText("Import SM from Policy view");
        setEnabled(false);
    }

    @Override
    public void run() {
        IWorkbench iworkbench = PlatformUI.getWorkbench();
        IWorkbenchPage page = null;
        try {
            page = iworkbench.showPerspective(SecurityPerspective.ID,
                    iworkbench.getActiveWorkbenchWindow());
        } catch (WorkbenchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        IViewPart part = null;
        try {
            part = page.showView(PolicyEditorView.ID);
        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PolicyEditorView pev = (PolicyEditorView) part;

        try {
            iworkbench.showPerspective(MonitoringPerspective.ID,
                iworkbench.getActiveWorkbenchWindow())
                      .showView(MonitoringView.ID);
        } catch (PartInitException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (WorkbenchException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        KeyStore keystore = null;
        try {
            keystore = KeystoreUtils.createKeystore(pev.getKeystore(),
                    pev.getKeysToKeep());
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<PolicyRule> policyRules = new ArrayList<PolicyRule>();
        for (SimplePolicyRule policy : pev.getRt().getRules()) {
            RuleEntities entitiesFrom = new RuleEntities();
            for (String name : policy.getFrom()) {
                try {
                    entitiesFrom.add(new CertificatedRuleEntity(
                            EntityType.fromString(name.substring(0,
                                    name.indexOf(':'))), keystore,
                            name.substring(name.indexOf(':') + 1)));
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
            RuleEntities entitiesTo = new RuleEntities();
            for (String name : policy.getTo()) {
                try {
                    entitiesTo.add(new CertificatedRuleEntity(
                            EntityType.fromString(name.substring(0,
                                    name.indexOf(':'))), keystore,
                            name.substring(name.indexOf(':') + 1)));
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
            Communication reply = new Communication(policy.isReply(),
                    policy.getRepAuth(), policy.getRepConf(), policy.getRepInt());
            Communication request = new Communication(policy.isRequest(),
                    policy.getReqAuth(), policy.getReqConf(), policy.getReqInt());

            policyRules.add(new PolicyRule(entitiesFrom, entitiesTo, request,
                    reply, policy.isAoCreation(), policy.isMigration()));
        }

        RuleEntities users = new RuleEntities();
        for (String user : pev.getRt().getAuthorizedUsers()) {
            try {
                users.add(new CertificatedRuleEntity(EntityType.fromString(
                            user.substring(0, user.indexOf(':'))), keystore,
                        user.substring(user.indexOf(':') + 1)));
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

        PolicyServer ps = new PolicyServer(keystore, policyRules,
                pev.getAppName(), "unknown", users);

        try {
            this.object.invoke("setSecurityManager", new Object[] { null, ps },
                new String[] {
                    "org.objectweb.proactive.core.security.securityentity.Entity",
                    "org.objectweb.proactive.core.security.PolicyServer"
                });
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

    public void setAbstractDataObject(AbstractData ref) {
        this.object = ref;
        super.setEnabled(this.object instanceof ActiveObject ||
            this.object instanceof RuntimeObject ||
            this.object instanceof NodeObject);
    }

    public void setActiveSelect(AbstractData ref) {
        // TODO Auto-generated method stub
    }
}
