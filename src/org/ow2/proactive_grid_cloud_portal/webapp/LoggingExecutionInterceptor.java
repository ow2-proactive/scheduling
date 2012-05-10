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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.interception.DecoderPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.jboss.resteasy.spi.interception.MessageBodyReaderContext;
import org.jboss.resteasy.spi.interception.MessageBodyReaderInterceptor;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.util.StopWatch;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;


@Provider
@ServerInterceptor
@DecoderPrecedence
public class LoggingExecutionInterceptor implements MessageBodyReaderInterceptor, AcceptedByMethod,
        ClientExecutionInterceptor {
    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.PREFIX + ".rest");

    @SuppressWarnings("unchecked")
    public ClientResponse execute(ClientExecutionContext ctx) throws Exception {
        String uri = ctx.getRequest().getUri();
        logger.info(String.format("Reading url %s", uri));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ClientResponse response = ctx.proceed();
        stopWatch.stop();
        String contentLength = (String) response.getMetadata().getFirst(HttpHeaderNames.CONTENT_LENGTH);
        logger.debug(String.format("Read url %s in %d ms size %s.", uri, stopWatch.getTime(), contentLength));
        return response;
    }

    public Object read(MessageBodyReaderContext ctx) throws IOException, WebApplicationException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return ctx.proceed();
        } finally {
            stopWatch.stop();
            logger.debug(String.format("Read mediaType %s as %s in %d ms.", ctx.getMediaType().toString(),
                    ctx.getType().getName(), stopWatch.getTime()));
        }
    }

    public boolean accept(Class declaring, Method method) {
        return true;
    }
}
