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

import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerSession;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerSessionMapper;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.netiq.websockify.IProxyTargetResolver;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

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
    private static final String VNC_PROTOCOL = "vnc";

    @Override
    public InetSocketAddress resolveTarget(MessageEvent messageEvent) {
        Map<String, String> parameters = getQueryMap(((HttpRequest) messageEvent.getMessage()).getUri());

        String sessionId = parameters.get("sessionId");
        String jobId = parameters.get("jobId");
        String taskName = parameters.get("taskName");

        if (sessionId == null || jobId == null || taskName == null) {
            LOGGER.warn("One of the web socket path parameter is missing (sessionId, jobId, taskName).");
            return null;
        }

        SchedulerSession session = SchedulerSessionMapper.getInstance().getSchedulerSession(sessionId);
        if (session == null) {
            LOGGER.warn("Unknown sessionId.");
            return null;
        }

        SchedulerProxyUserInterface scheduler = session.getScheduler();

        try {
            TaskResult taskResult = scheduler.getTaskResult(jobId, taskName);

            String paRemoteConnectionLine = retrievePaRemoteConnectionLine(taskResult.getOutput().getAllLogs(false));
            if (paRemoteConnectionLine == null) {
                LOGGER.warn("Could not retrieve VNC connection information in task's logs");
                return null;
            }

            String[] paRemoteConnectionArgs = paRemoteConnectionLine.split(";");
            if(paRemoteConnectionArgs.length != 4){
                LOGGER.warn("Missing arguments in PA_REMOTE_CONNECTION string, format should be PA_REMOTE_CONNECTION;$taskId;vnc;ip:port");
                return null;
            }
            String taskId = paRemoteConnectionArgs[1];
            String type = paRemoteConnectionArgs[2];
            String args = paRemoteConnectionArgs[3];

            if (taskId.equals(taskResult.getTaskId().value()) && VNC_PROTOCOL.equals(type)) {
                String[] targetHostAndPort = args.split(":");
                String vncHost = targetHostAndPort[0];
                String vncPort = targetHostAndPort[1];
                LOGGER.debug("Proxying to " + vncHost + ":" + vncPort);
                return new InetSocketAddress(vncHost, Integer.parseInt(vncPort));
            } else {
                LOGGER.warn("Protocol or task unknown in PA_REMOTE_CONNECTION string");
            }

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

    private String retrievePaRemoteConnectionLine(String logs) {
        for (String line : lineByLine(logs)) {
            if (line.contains(PA_REMOTE_CONNECTION)) {
                return line;
            }
        }
        return null;
    }

    private String[] lineByLine(String lines) {
        return lines.split(PLATFORM_INDEPENDENT_LINE_BREAK);
    }
}
