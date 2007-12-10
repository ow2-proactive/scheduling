package org.objectweb.proactive.core.mop;

import java.io.IOException;
import java.io.OutputStream;


public class SunMarshalOutputStream extends sun.rmi.server.MarshalOutputStream {
    public SunMarshalOutputStream(OutputStream out) throws IOException {
        super(out);
    }
}
