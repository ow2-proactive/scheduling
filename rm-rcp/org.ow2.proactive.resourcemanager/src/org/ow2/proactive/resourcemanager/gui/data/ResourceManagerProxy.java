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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.data;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.gui.common.DefaultActiveObjectProxy;
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;


/**
 * ResourceManagerProxy is used as a gateway to talk to the RMcore with the same thread !
 * Needed by authentication methods
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class ResourceManagerProxy extends DefaultActiveObjectProxy<ResourceManager> {

    private static ResourceManagerProxy proxyInstance;

    private RMAuthentication auth;

    private Credentials creds;

    @Override
    protected boolean doPingActiveObject(ResourceManager resourceManager) {
        return resourceManager.isActive().getBooleanValue();
    }

    @Override
    protected ResourceManager doCreateActiveObject() throws Exception {
        return auth.login(creds);
    }

    public static ResourceManagerProxy getProxyInstance() {
        if (proxyInstance == null) {
            proxyInstance = new ResourceManagerProxy();
        }
        return proxyInstance;
    }

    public void connect(RMAuthentication auth, Credentials creds) throws Exception {
        this.auth = auth;
        this.creds = creds;
        createActiveObject();
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                initActiveObjectHolderForCurrentThread();
            }
        });
    }

    public boolean syncAddNode(final String nodeUrl, final String sourceName) throws Exception {
        return syncCallActiveObject(new ActiveObjectSyncAccess<ResourceManager>() {
            @Override
            public Boolean accessActiveObject(ResourceManager resourceManager) throws Exception {
                return resourceManager.addNode(nodeUrl, sourceName).getBooleanValue();
            }
        });
    }

    public boolean syncCreateNodeSource(final String nodeSourceName, final String infrastructureType,
            final Object[] infrastructureParameters, final String policyType, final Object[] policyParameters)
            throws Exception {
        return syncCallActiveObject(new ActiveObjectSyncAccess<ResourceManager>() {
            @Override
            public Boolean accessActiveObject(ResourceManager resourceManager) throws Exception {
                return resourceManager.createNodeSource(nodeSourceName, infrastructureType,
                        infrastructureParameters, policyType, policyParameters).getBooleanValue();
            }
        });
    }

    public void disconnect() {
        asyncCallActiveObject(new ActiveObjectAccess<ResourceManager>() {
            @Override
            public void accessActiveObject(ResourceManager resourceManager) {
                try {
                    BooleanWrapper result = resourceManager.disconnect();
                    if (!result.getBooleanValue()) {
                        Activator.log(IStatus.INFO, "Resource Manager Proxy: Failed to disconnect", null);
                    }
                } catch (Throwable t) {
                    Activator.log(IStatus.INFO, "Resource Manager Proxy: Error on get disconnect", t);
                }
            }

        });

        terminateActiveObjectHolder();
    }

    public RMMonitoring syncGetMonitoring() throws Exception {
        RMMonitoring result = syncCallActiveObject(new ActiveObjectSyncAccess<ResourceManager>() {
            @Override
            public RMMonitoring accessActiveObject(ResourceManager resourceManager) throws Exception {
                return resourceManager.getMonitoring();
            }
        });
        PAFuture.waitFor(result);
        return result;
    }

    public SupportedPluginDescriptors syncGetSupportedPluginDescriptors() throws Exception {
        Collection<PluginDescriptor> infrastructures = syncCallActiveObject(new ActiveObjectSyncAccess<ResourceManager>() {
            @Override
            public Collection<PluginDescriptor> accessActiveObject(ResourceManager resourceManager)
                    throws Exception {
                return resourceManager.getSupportedNodeSourceInfrastructures();
            }
        });
        PAFuture.waitFor(infrastructures);
        Collection<PluginDescriptor> policies = syncCallActiveObject(new ActiveObjectSyncAccess<ResourceManager>() {
            @Override
            public Collection<PluginDescriptor> accessActiveObject(ResourceManager resourceManager)
                    throws Exception {
                return resourceManager.getSupportedNodeSourcePolicies();
            }
        });
        PAFuture.waitFor(policies);
        return new SupportedPluginDescriptors(infrastructures, policies);
    }

    public void removeNode(final String nodeUrl, final boolean preempt) {
        asyncCallActiveObject(new ActiveObjectAccess<ResourceManager>() {
            @Override
            public void accessActiveObject(ResourceManager resourceManager) {
                try {
                    BooleanWrapper result = resourceManager.removeNode(nodeUrl, preempt);
                    if (!result.getBooleanValue()) {
                        displayError("Unknown reason", "Cannot remove node");
                    }
                } catch (Exception e) {
                    logAndDisplayError(e, "Cannot remove node");
                }

            }
        });
    }

    public boolean syncRemoveNodeSource(final String sourceName, final boolean preempt) throws Exception {
        return syncCallActiveObject(new ActiveObjectSyncAccess<ResourceManager>() {
            @Override
            public Boolean accessActiveObject(ResourceManager resourceManager) throws Exception {
                return resourceManager.removeNodeSource(sourceName, preempt).getBooleanValue();
            }
        });
    }

    public void shutdown(final boolean preempt) {
        asyncCallActiveObject(new ActiveObjectAccess<ResourceManager>() {
            @Override
            public void accessActiveObject(ResourceManager resourceManager) {
                try {
                    BooleanWrapper result = resourceManager.shutdown(preempt);
                    if (!result.getBooleanValue()) {
                        displayError("Unknown reason", "Cannot shutdown the resource manager");
                    }
                } catch (Exception e) {
                    logAndDisplayError(e, "Cannot shutdown the resource manager");
                }

            }
        });
    }

    public Topology syncGetTopology() {
        try {
            Topology result = syncCallActiveObject(new ActiveObjectSyncAccess<ResourceManager>() {
                @Override
                public Topology accessActiveObject(ResourceManager resourceManager) throws Exception {
                    return resourceManager.getTopology();
                }
            });
            PAFuture.waitFor(result);
            return result;
        } catch (Exception e) {
            logAndDisplayError(e, "Failed to get nodes topology");
            throw new RuntimeException(e);
        }
    }

    public void lockNodes(final Set<String> nodesUrls) {
        asyncCallActiveObject(new ActiveObjectAccess<ResourceManager>() {
            @Override
            public void accessActiveObject(ResourceManager resourceManager) {
                try {
                    BooleanWrapper result = resourceManager.lockNodes(nodesUrls);
                    if (!result.getBooleanValue()) {
                        displayError("Unknown reason", "Cannot lock nodes");
                    }
                } catch (Exception e) {
                    logAndDisplayError(e, "Cannot lock nodes");
                }
            }
        });
    }

    public void unlockNodes(final Set<String> nodesUrls) {
        asyncCallActiveObject(new ActiveObjectAccess<ResourceManager>() {
            @Override
            public void accessActiveObject(ResourceManager resourceManager) {
                try {
                    BooleanWrapper result = resourceManager.unlockNodes(nodesUrls);
                    if (!result.getBooleanValue()) {
                        displayError("Unknown reason", "Cannot unlock nodes");
                    }
                } catch (Exception e) {
                    logAndDisplayError(e, "Cannot unlock nodes");
                }
            }
        });
    }

    public void logAndDisplayError(Exception e, final String title) {
        Activator.log(IStatus.ERROR, title, e);

        String message = e.getMessage();
        if (e.getCause() != null) {
            message = e.getCause().getMessage();
        }

        displayError(message, title);
    }

    public void displayError(final String message, final String title) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openError(Display.getDefault().getActiveShell(), title, message);
            }
        });
    }

}
