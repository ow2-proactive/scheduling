package org.ow2.proactive.scheduler.ext.matsci.common;

import org.objectweb.proactive.core.process.JVMProcessImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * DummyJVMProcess
 *
 * @author The ProActive Team
 */
public class DummyJVMProcess extends JVMProcessImpl implements Serializable {

    public DummyJVMProcess() {
        super();
    }

    /**
     *
     */
    public List<String> getJavaCommand() {
        String javaCommand = buildJavaCommand();
        List<String> javaCommandList = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(javaCommand, " ");

        while (st.hasMoreElements()) {
            javaCommandList.add(st.nextToken());
        }

        return javaCommandList;
    }
}
