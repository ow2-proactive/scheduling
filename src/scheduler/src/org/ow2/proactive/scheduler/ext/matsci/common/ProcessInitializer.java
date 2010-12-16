package org.ow2.proactive.scheduler.ext.matsci.common;

import java.util.Map;


/**
 * ProcessInitializer
 *
 * @author The ProActive Team
 */
public interface ProcessInitializer {

    void initProcess(DummyJVMProcess jvmprocess, Map<String, String> env) throws Throwable;
}
