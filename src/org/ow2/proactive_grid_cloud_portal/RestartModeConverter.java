package org.ow2.proactive_grid_cloud_portal;

import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.StringConverter;
import org.ow2.proactive.scheduler.common.task.RestartMode;


@Provider
public class RestartModeConverter implements StringConverter<RestartMode>
{
   public RestartMode fromString(String str)
   {
      return RestartMode.getMode(str);
   }

   public String toString(RestartMode value)
   {
      return value.toString();
   }
}
