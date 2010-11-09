package org.ow2.proactive_grid_cloud_portal;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.interception.DecoderPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.MessageBodyReaderContext;
import org.jboss.resteasy.spi.interception.MessageBodyReaderInterceptor;
import org.jboss.util.StopWatch;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;

@Provider
@ServerInterceptor
@DecoderPrecedence
public class LoggingExecutionInterceptor implements MessageBodyReaderInterceptor,AcceptedByMethod
{
   private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.PREFIX + ".rest");



//   @SuppressWarnings("unchecked")
//   public ClientResponse execute(ClientExecutionContext ctx) throws Exception
//   {
//      String uri = ctx.getRequest().getUri();
//      logger.info(String.format("Reading url %s", uri));
//      StopWatch stopWatch = new StopWatch();
//      stopWatch.start();
//      ClientResponse response = ctx.proceed();
//      stopWatch.stop();
//      String contentLength = (String) response.getMetadata().getFirst(
//              HttpHeaderNames.CONTENT_LENGTH);
//      logger.info(String.format("Read url %s in %d ms size %s.", uri,
//              stopWatch.getTime(), contentLength));
//      System.out.println("SDDDDDDDDDDDDDDDDDDDDDDDSsssssssssssss");
//      return response;
//   }

   public Object read(MessageBodyReaderContext ctx) throws IOException,
           WebApplicationException
   {



       System.out.println("raaaaaaaaaaaaaaa");
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      try
      {
         return ctx.proceed();
      }
      finally
      {
         stopWatch.stop();
         logger.info(String.format("Read mediaType %s as %s in %d ms.", ctx
                 .getMediaType().toString(), ctx.getType().getName(),
                 stopWatch.getTime()));
      }
   }



public boolean accept(Class declaring, Method method) {
    System.out.println("should I accept ?");
    return true;
}
}
