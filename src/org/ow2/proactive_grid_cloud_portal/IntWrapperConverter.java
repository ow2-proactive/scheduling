package org.ow2.proactive_grid_cloud_portal;

import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.StringConverter;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


@Provider
public class IntWrapperConverter implements StringConverter<IntWrapper> {
    public IntWrapper fromString(String str) {
        return new IntWrapper(Integer.parseInt(str));
    }

    public String toString(IntWrapper value) {
        return value.toString();
    }
}
