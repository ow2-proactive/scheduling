package org.ow2.proactive.scheduler.ext.matsci.worker.util;

import org.objectweb.proactive.utils.OperatingSystem;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
* MatSciFinder
*
* @author The ProActive Team
*/
public abstract class MatSciFinder {

    // the OS where this JVM is running
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    private static MatSciFinder instance = null;

    protected ScriptEngine initEngine() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine jsEngine = mgr.getEngineByName("jruby");
        return jsEngine;
    }

    public abstract MatSciEngineConfig findMatSci(String version_pref, HashSet<String> versionsRej,
            String versionMin, String versionMax) throws Exception;

    protected MatSciEngineConfig chooseMatSciConfig(ArrayList<MatSciEngineConfig> configs,
            String version_pref, Set<String> versionsRej, String versionMin, String versionMax) {
        List<MatSciEngineConfig> selected = new ArrayList<MatSciEngineConfig>();
        for (MatSciEngineConfig conf : configs) {
            if (versionsRej != null && versionsRej.contains(conf.getVersion()))
                continue;
            if (versionMin != null && infStrictVersion(conf.getVersion(), versionMin))
                continue;
            if (versionMax != null && infStrictVersion(versionMax, conf.getVersion()))
                continue;
            if (version_pref != null && version_pref.equals(conf.getVersion()))
                return conf;
            selected.add(conf);
        }
        if (selected.size() > 0) {
            return selected.get(0);
        }
        return null;
    }

    protected boolean infStrictVersion(String v1, String v2) {
        String[] majmin1 = v1.split("\\.");
        String[] majmin2 = v2.split("\\.");
        int n = Math.min(majmin1.length, majmin2.length);
        for (int i = 1; i < n; i++) {
            if (Integer.parseInt(majmin1[i]) < Integer.parseInt(majmin2[i]))
                return true;
            else if (Integer.parseInt(majmin1[i]) > Integer.parseInt(majmin2[i]))
                return false;
        }
        if (n < majmin1.length)
            return false;
        else if (n < majmin2.length)
            return true;
        return false;
    }

    protected HashSet<String> parseVersionRej(String vrej) {
        HashSet<String> vrejSet = new HashSet<String>();
        if (vrej != null) {
            vrej = vrej.trim();
            String[] vRejArr = vrej.split("[ ,;]+");

            for (String rej : vRejArr) {
                vrejSet.add(rej);
            }
        }
        return vrejSet;
    }
}
