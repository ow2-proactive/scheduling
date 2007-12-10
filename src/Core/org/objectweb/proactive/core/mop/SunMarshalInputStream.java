package org.objectweb.proactive.core.mop;

import java.io.IOException;
import java.io.InputStream;


public class SunMarshalInputStream extends sun.rmi.server.MarshalInputStream {
    public SunMarshalInputStream(InputStream in) throws IOException {
        super(in);
    }
}
