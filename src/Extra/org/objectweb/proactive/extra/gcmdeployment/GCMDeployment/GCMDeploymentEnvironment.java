package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import java.util.HashMap;
import java.util.Map;


public class GCMDeploymentEnvironment {
    protected Map<String, String> environment;

    public GCMDeploymentEnvironment() {
        environment = new HashMap<String, String>();
    }

    public void addValue(String varname, String value) {
        for (int i = 0; i < varname.length(); ++i) {
            if (Character.isWhitespace(varname.charAt(i))) {
                throw new RuntimeException(
                    "no whitespace allowed in environment variable name");
            }
        }
        environment.put(varname, value);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();

        for (String key : environment.keySet()) {
            res.append(key);
            res.append("='");
            res.append(environment.get(key));
            res.append('\'');
            // TODO - some escaping needed here
        }

        return res.toString();
    }
}
