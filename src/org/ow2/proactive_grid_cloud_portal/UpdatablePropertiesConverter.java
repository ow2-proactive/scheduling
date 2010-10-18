package org.ow2.proactive_grid_cloud_portal;

import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.StringConverter;
import org.ow2.proactive.scheduler.common.task.UpdatableProperties;

@Provider
public class UpdatablePropertiesConverter implements StringConverter<UpdatableProperties>{
        
    public UpdatableProperties fromString(String str) {
           return null;
        }

        public String toString(UpdatableProperties value) {
            return value.getValue().toString();
        }
    

}
