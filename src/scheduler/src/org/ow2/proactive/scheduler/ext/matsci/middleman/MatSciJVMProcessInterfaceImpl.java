package org.ow2.proactive.scheduler.ext.matsci.middleman;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.ow2.proactive.scheduler.ext.matsci.client.common.MatSciEnvironment;
import org.ow2.proactive.scheduler.ext.matsci.client.common.MatSciJVMProcessInterface;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * JVMProcessInterfaceImpl
 *
 * @author The ProActive Team
 */
public class MatSciJVMProcessInterfaceImpl implements InitActive, MatSciJVMProcessInterface {

    MatSciEnvironment matlab_env;
    MatSciEnvironment scilab_env;

    MatSciJVMProcessInterfaceImpl stubOnThis;

    public MatSciJVMProcessInterfaceImpl() {

    }

    public MatSciJVMProcessInterfaceImpl(MatSciEnvironment matlab_env, MatSciEnvironment scilab_env) {
        this.scilab_env = scilab_env;
        this.matlab_env = matlab_env;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        stubOnThis = (MatSciJVMProcessInterfaceImpl) PAActiveObject.getStubOnThis();
    }

    /** {@inheritDoc} */
    public Integer getPID() {
        RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
        String processName = rtb.getName();

        Integer result = null;

        /* tested on: */
        /* - windows xp sp 2, java 1.5.0_13 */
        /* - mac os x 10.4.10, java 1.5.0 */
        /* - debian linux, java 1.5.0_13 */
        /* all return pid@host, e.g 2204@antonius */

        Pattern pattern = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(processName);
        if (matcher.matches()) {
            result = new Integer(Integer.parseInt(matcher.group(1)));
        }
        return result;

    }

    /** {@inheritDoc} */
    public boolean shutdown() {
        try {
            matlab_env.disconnect();
            matlab_env.terminate();
        } catch (Throwable e) {
        }
        try {
            scilab_env.disconnect();
            scilab_env.terminate();
        } catch (Throwable e) {
        }
        stubOnThis.destroyJVM();
        return true;
    }

    protected void destroyJVM() {
        PALifeCycle.exitSuccess();
    }
}
