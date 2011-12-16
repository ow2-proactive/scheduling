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
package org.ow2.proactive.scheduler.gui.data;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.gui.common.ActiveObjectCallResultHandler;
import org.ow2.proactive.gui.common.ActiveObjectPingerThread;
import org.ow2.proactive.gui.common.ActiveObjectProxy;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.gui.actions.DisconnectAction;
import org.ow2.proactive.scheduler.gui.actions.JMXActionsManager;
import org.ow2.proactive.scheduler.gui.composite.StatusLabel;
import org.ow2.proactive.scheduler.gui.dialog.SelectSchedulerDialogResult;
import org.ow2.proactive.scheduler.gui.listeners.SchedulerConnectionListener;
import org.ow2.proactive.scheduler.gui.views.SeparatedJobView;


/**
 * SchedulerProxy...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SchedulerProxy extends ActiveObjectProxy<Scheduler> implements
        ActiveObjectPingerThread.PingListener {

    private static SchedulerProxy instance;
    private SchedulerAuthenticationInterface sai;
    private String userName;
    private transient ActiveObjectPingerThread pinger;
    private String schedulerURL;
    private boolean connected;

    private static final Hashtable<TaskId, TaskResult> cachedTaskResult = new Hashtable<TaskId, TaskResult>();

    private List<SchedulerConnectionListener> observers;

    public SchedulerProxy() {
        observers = new LinkedList<SchedulerConnectionListener>();
    }

    private Credentials credentials;

    @Override
    protected Scheduler doCreateActiveObject() throws Exception {
        return sai.login(credentials);
    }

    @Override
    protected boolean doPingActiveObject(Scheduler activeObject) {
        return activeObject.isConnected();
    }

    public boolean connectToScheduler(SelectSchedulerDialogResult dialogResult) throws Throwable {
        try {
            userName = dialogResult.getLogin();
            byte[] cred = dialogResult.getCred();
            schedulerURL = dialogResult.getUrl();
            sai = SchedulerConnection.join(schedulerURL);

            if (cred == null) {
                CredData cd;
                if (dialogResult.getSshkey() == null) {
                    cd = new CredData(CredData.parseLogin(userName), CredData.parseDomain(userName),
                        dialogResult.getPassword());
                } else {
                    cd = new CredData(CredData.parseLogin(userName), CredData.parseDomain(userName),
                        dialogResult.getPassword(), dialogResult.getSshkey());
                }
                credentials = Credentials.createCredentials(cd, sai.getPublicKey());
            } else {
                credentials = Credentials.getCredentialsBase64(cred);
            }

            if (!isActiveObjectCreated() || !syncPingActiveObject()) {
                createActiveObject();
            }
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    initActiveObjectHolderForCurrentThread();
                }
            });

            sendConnectionCreatedEvent(dialogResult.getUrl(), userName, dialogResult.getPassword());
            // SCHEDULING-1434 - should start the pinger after the first call to addEventListener
            //startPinger();
            connected = true;

            JMXActionsManager.getInstance().initJMXClient(schedulerURL, sai,
                    new Object[] { dialogResult.getLogin(), credentials });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // exception is handled by the GUI
            userName = null;
            return false;
        } catch (Throwable t) {
            Activator.log(IStatus.ERROR, "- Error when connecting to the scheduler ", t);
            userName = null;
            throw t;
        }
    }

    public SchedulerState syncAddEventListener(final SchedulerEventListener listener,
            final boolean myEventsOnly, final boolean getSchedulerState, final SchedulerEvent... events) {
        try {
            SchedulerState result = syncCallActiveObject(new ActiveObjectSyncAccess<Scheduler>() {
                @Override
                public SchedulerState accessActiveObject(Scheduler scheduler) {
                    SchedulerState schedState = null;
                    try {
                        schedState = (SchedulerState) scheduler.addEventListener(listener, myEventsOnly,
                                getSchedulerState, events);
                    } catch (PermissionException pe) {
                        Activator.log(IStatus.ERROR, "Error getting full state : " + pe.getMessage(), pe);
                        try {
                            schedState = (SchedulerState) scheduler.addEventListener(listener, true,
                                    getSchedulerState, events);
                        } catch (SchedulerException e) {
                            logAndDisplayError(e, "Error in Scheduler Proxy ");
                        }
                    } catch (SchedulerException e) {
                        logAndDisplayError(e, "Error in Scheduler Proxy ");
                    }
                    return schedState;
                }
            });

            if (result != null) {
                //SCHEDULING-1434 -
                //Note: the pinger will only be started once, see condition in startPinger()
                startPinger();
            }

            return result;
        } catch (Exception e) {
            Activator.log(IStatus.ERROR, "Error adding scheduler event listener", e);
            return null;
        }
    }

    public void disconnect(boolean serverIsDown) {
        if (isActiveObjectCreated()) {
            if (pinger != null) {
                pinger.stopPinger();
            }
            if (!serverIsDown) {
                asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
                    @Override
                    public void accessActiveObject(Scheduler scheduler) {
                        try {
                            scheduler.disconnect();
                        } catch (Exception e) {
                            logAndDisplayError(e, "- Scheduler Proxy: Error in  disconnect action");
                        }
                    }
                });
            }
            connected = false;
            sendConnectionLostEvent();

            terminateActiveObjectHolder();
        }
    }

    private void serverDown(final String disconnectionReason) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openInformation(SeparatedJobView.getSchedulerShell(), "Disconnection",
                        disconnectionReason);
                StatusLabel.getInstance().disconnect();
                DisconnectAction.disconnection(true);
                connected = false;
            }
        });
    }

    public static void deleteTaskResultCache() {
        cachedTaskResult.clear();
    }

    public void getTaskResult(final JobId jobId, final TaskId tid,
            final ActiveObjectCallResultHandler<TaskResult> resultHadler) {
        TaskResult taskResult = cachedTaskResult.get(tid);
        if (taskResult != null) {
            resultHadler.handleResult(taskResult);
        } else {
            asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
                @Override
                public void accessActiveObject(Scheduler scheduler) {
                    try {
                        TaskResult taskResult = scheduler.getTaskResult(jobId, tid.getReadableName());
                        if (taskResult != null) {
                            cachedTaskResult.put(tid, taskResult);
                            resultHadler.handleResult(taskResult);
                        }
                    } catch (SchedulerException e) {
                        logAndDisplayError(e, "- Scheduler Proxy: Error when getting task result");
                    }
                }

            });
        }
    }

    public void killTask(JobId jobId, String taskName) {
        killTask(jobId.value(), taskName);
    }

    public void preemptTask(JobId jobId, String taskName, int restartDelay) {
        preemptTask(jobId.value(), taskName, restartDelay);
    }

    public void restartTask(JobId jobId, String taskName, int restartDelay) {
        restartTask(jobId.value(), taskName, restartDelay);
    }

    public void killTask(final String jobId, final String taskName) {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.killTask(jobId, taskName);
                } catch (Exception e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Failed to kill task");
                }
            }

        });
    }

    public void preemptTask(final String jobId, final String taskName, final int restartDelay) {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.preemptTask(jobId, taskName, restartDelay);
                } catch (Exception e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Failed to preempt task");
                }
            }

        });
    }

    public void restartTask(final String jobId, final String taskName, final int restartDelay) {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.restartTask(jobId, taskName, restartDelay);
                } catch (Exception e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Failed to restart task");
                }
            }

        });
    }

    public void killJob(final JobId jobId) {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.killJob(jobId);
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on kill job ");
                }
            }

        });
    }

    public void listenJobLogs(final JobId jobId, final AppenderProvider appenderProvider) {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.listenJobLogs(jobId, appenderProvider);
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on listen log");
                }
            }
        });
    }

    public void pauseJob(final JobId jobId) {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.pauseJob(jobId);
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on pause");
                }
            }

        });
    }

    public void resumeJob(final JobId jobId) {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.resumeJob(jobId);
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on resume action");
                }
            }

        });
    }

    public void jobSubmit(final Job job) {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.submit(job);
                } catch (Exception e) {
                    logAndDisplayError(e, "Couldn't submit job");
                }
            }
        });

    }

    // NOTE: method is synchronous, so it shouldn't be called from GUI thread   
    public JobId syncJobSubmit(final Job job) throws Exception {
        return syncCallActiveObject(new ActiveObjectSyncAccess<Scheduler>() {
            @Override
            public JobId accessActiveObject(Scheduler scheduler) throws Exception {
                return scheduler.submit(job);
            }
        });
    }

    public void removeJob(final JobId jobId) {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.removeJob(jobId);
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on remove job action ");
                }
            }

        });
    }

    public void kill() {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.kill();
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            DisconnectAction.disconnection(true);
                        }
                    });
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on kill action ");
                }
            }

        });
    }

    public void pause() {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.pause();
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on pause action ");
                }
            }

        });
    }

    public void freeze() {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.freeze();
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on freeze action ");
                }
            }

        });
    }

    public void resume() {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.resume();
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on resume action ");
                }
            }
        });
    }

    public void shutdown() {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.shutdown();
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            DisconnectAction.disconnection(true);
                        }
                    });
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on shut down action ");
                }
            }
        });
    }

    public void start() {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.start();
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on start action");
                }
            }
        });
    }

    public void stop() {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.stop();
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on stop action ");
                }
            }
        });
    }

    public void changeJobPriority(final JobId jobId, final JobPriority priority) {
        asyncCallActiveObject(new ActiveObjectAccess<Scheduler>() {
            @Override
            public void accessActiveObject(Scheduler scheduler) {
                try {
                    scheduler.changeJobPriority(jobId, priority);
                } catch (SchedulerException e) {
                    logAndDisplayError(e, "- Scheduler Proxy: Error on change priority action ");
                }
            }
        });
    }

    private void startPinger() {
        if ((pinger != null) && (!pinger.isStopped()))
            return;
        System.out.println("SchedulerProxy.startPinger() - starting pinger .... ");
        pinger = new ActiveObjectPingerThread(this, this);
        pinger.start();
    }

    @Override
    public void onPingError() {
        String disconnectionReason = "Scheduler  '" + schedulerURL +
            "'  seems to be down. Now disconnecting.";
        serverDown(disconnectionReason);
    }

    @Override
    public void onPingTimeout() {
        onPingError();
    }

    @Override
    public void onPingFalse() {
        String disconnectionReason = "Your connection to the Scheduler '" + schedulerURL +
            "' has expired. Now disconnecting.";
        serverDown(disconnectionReason);
    }

    public Boolean isItHisJob(String userName) {
        if ((this.userName == null) || (userName == null)) {
            return false;
        }
        return this.userName.equals(userName);
    }

    public static SchedulerProxy getInstance() {
        if (instance == null) {
            instance = new SchedulerProxy();
        }
        return instance;
    }

    public String getUsername() {
        return this.userName;
    }

    public boolean isProxyConnected() {
        return connected;
    }

    public static void clearInstance() {
        instance = null;
    }

    public void addConnectionListener(SchedulerConnectionListener obs) {
        this.observers.add(obs);
    }

    public void removeObserver(Observer obs) {
        this.observers.remove(obs);
    }

    public void sendConnectionCreatedEvent(String schedulerUrl, String user, String password) {
        Iterator<SchedulerConnectionListener> it = this.observers.iterator();
        while (it.hasNext()) {
            SchedulerConnectionListener o = it.next();
            o.connectionCreatedEvent(schedulerUrl, user, password);
        }
    }

    public void sendConnectionLostEvent() {
        Iterator<SchedulerConnectionListener> it = this.observers.iterator();
        while (it.hasNext()) {
            SchedulerConnectionListener o = it.next();
            o.connectionLostEvent();
        }
    }

    private void logAndDisplayError(final Exception e, final String message) {
        Activator.log(IStatus.ERROR, message, e);

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openError(Display.getDefault().getActiveShell(), "Error !", e.getMessage());
            }
        });
    }

}
