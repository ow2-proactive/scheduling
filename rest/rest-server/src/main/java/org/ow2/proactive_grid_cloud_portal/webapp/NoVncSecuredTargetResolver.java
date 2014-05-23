/*
 *  *
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;
import com.netiq.websockify.IProxyTargetResolver;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;


/**
 * A target resolved for websocket proxy capable of reading parameters from the websocket path.
 * <br/>
 * The parameters are used to retrieve a task's logs and check for the "PA_REMOTE_CONNECTION" string to appear.
 * Then parameters of "PA_REMOTE_CONNECTION" are used to proxy the websocket connection.
 */
public class NoVncSecuredTargetResolver implements IProxyTargetResolver {

    private static final Logger LOGGER = ProActiveLogger.getLogger(NoVncSecuredTargetResolver.class);

    private static final String PLATFORM_INDEPENDENT_LINE_BREAK = "\r\n?|\n";

    private static final String PA_REMOTE_CONNECTION = "PA_REMOTE_CONNECTION";
    private static final String PA_REMOTE_CONNECTION_SEPARATOR = ";";
    private static final String PA_REMOTE_CONNECTION_HOST_PORT_SEPARATOR = ":";

    private static final String VNC_PROTOCOL = "vnc";

    @Override
    public InetSocketAddress resolveTarget(MessageEvent messageEvent) {
        Map<String, String> parameters = getQueryMap(((HttpRequest) messageEvent.getMessage()).getUri());

        String sessionId = parameters.get("sessionId");
        String jobId = parameters.get("jobId");
        String taskName = parameters.get("taskName");

        return doResolve(sessionId, jobId, taskName);
    }

    // package-protected for testing
    InetSocketAddress doResolve(String sessionId, String jobId, String taskName) {
        if (sessionId == null || jobId == null || taskName == null) {
            LOGGER.warn("One of the web socket path parameter is missing (sessionId, jobId, taskName).");
            return null;
        }

        Session session = SharedSessionStore.getInstance().get(sessionId);
        if (session == null) {
            LOGGER.warn("Unknown sessionId.");
            return null;
        }

        SchedulerProxyUserInterface scheduler = session.getScheduler();

        try {
            TaskResult taskResult = scheduler.getTaskResult(jobId, taskName);
            List<String> paRemoteConnectionLines = retrievePaRemoteConnectionLines(session, jobId, taskResult);

            String taskIdFromTaskName = retrieveTaskId(taskName, scheduler.getJobState(jobId));
            return resolveVncTargetFromLogs(paRemoteConnectionLines, taskIdFromTaskName);

        } catch (NotConnectedException e) {
            LOGGER.warn("Failed to connect to scheduler", e);
        } catch (UnknownJobException e) {
            LOGGER.warn("Job does not exist", e);
        } catch (UnknownTaskException e) {
            LOGGER.warn("Task does not exist", e);
        } catch (PermissionException e) {
            LOGGER.warn("Not allowed to access task", e);
        }
        return null;
    }

    private InetSocketAddress resolveVncTargetFromLogs(List<String> paRemoteConnectionLines,
            String taskIdFromTaskName) {
        for (String paRemoteConnectionLine : paRemoteConnectionLines) {
            String[] paRemoteConnectionArgs = paRemoteConnectionLine.split(PA_REMOTE_CONNECTION);
            if (paRemoteConnectionArgs.length == 2) {
                paRemoteConnectionArgs = paRemoteConnectionArgs[1].split(PA_REMOTE_CONNECTION_SEPARATOR);
                if (paRemoteConnectionArgs.length == 4) {
                    String taskId = paRemoteConnectionArgs[1];
                    String type = paRemoteConnectionArgs[2];
                    String args = paRemoteConnectionArgs[3];

                    if (taskId.equals(taskIdFromTaskName) && VNC_PROTOCOL.equals(type)) {
                        String[] targetHostAndPort = args.split(PA_REMOTE_CONNECTION_HOST_PORT_SEPARATOR);
                        String vncHost = targetHostAndPort[0].trim();
                        String vncPort = targetHostAndPort[1].trim();
                        LOGGER.debug("Proxying to " + vncHost + ":" + vncPort);
                        return new InetSocketAddress(vncHost, Integer.parseInt(vncPort));
                    } else {
                        LOGGER.debug("Protocol or task unknown in PA_REMOTE_CONNECTION string (" +
                            paRemoteConnectionLine + ")");
                    }
                }
            }
            LOGGER.debug("Missing arguments in PA_REMOTE_CONNECTION string, " + "(" + paRemoteConnectionLine +
                ")" + "format should be PA_REMOTE_CONNECTION;$taskId;vnc;host:port");
        }
        LOGGER.warn("Could not find the PA_REMOTE_CONNECTION string");
        return null;
    }

    private static String retrieveTaskId(String taskName, JobState jobState) {
        for (TaskState taskState : jobState.getHMTasks().values()) {
            if (taskState.getName().equals(taskName)) {
                return taskState.getId().value();
            }
        }
        return null;
    }

    private List<String> retrievePaRemoteConnectionLines(Session session, String jobId, TaskResult taskResult) {
        List<String> paRemoteConnectionLines = Collections.emptyList();
        String liveLogs = getJobLiveLogs(session, jobId);
        if (liveLogs != null) {
            paRemoteConnectionLines = retrievePaRemoteConnectionLines(liveLogs);
        }
        if (paRemoteConnectionLines.isEmpty()) {
            if (taskResult != null && taskResult.getOutput() != null) {
                paRemoteConnectionLines = retrievePaRemoteConnectionLines(taskResult.getOutput().getAllLogs(
                        false));
            }
        }
        return paRemoteConnectionLines;
    }

    private String getJobLiveLogs(Session session, String jobId) {
        try {
            return session.getJobOutputAppender(jobId).getJobOutput().fetchAllLogs();
        } catch (Exception e) {
            LOGGER.warn("Could not retrieve live logs", e);
            return null;
        }
    }

    private List<String> retrievePaRemoteConnectionLines(String logs) {
        List<String> lines = new ArrayList<String>();
        for (String line : lineByLine(logs)) {
            if (line.contains(PA_REMOTE_CONNECTION)) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static Map<String, String> getQueryMap(String query) {
        String[] pathAndParams = query.split("\\?");
        String paramString = pathAndParams[0];
        if (pathAndParams.length > 1) {
            paramString = pathAndParams[1];
        }
        String[] params = paramString.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    private static String[] lineByLine(String lines) {
        return lines.split(PLATFORM_INDEPENDENT_LINE_BREAK);
    }

}
