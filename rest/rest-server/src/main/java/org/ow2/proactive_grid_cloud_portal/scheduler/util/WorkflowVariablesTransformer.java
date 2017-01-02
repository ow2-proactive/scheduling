package org.ow2.proactive_grid_cloud_portal.scheduler.util;

import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import com.google.common.collect.Maps;


public class WorkflowVariablesTransformer {

    public Map<String, String> getWorkflowVariablesFromPathSegment(PathSegment pathSegment) {
        Map<String, String> variables = null;
        MultivaluedMap<String, String> matrixParams = pathSegment.getMatrixParameters();
        if (matrixParams != null && !matrixParams.isEmpty()) {
            variables = Maps.newHashMap();
            for (String key : matrixParams.keySet()) {
                String value = matrixParams.getFirst(key) == null ? "" : matrixParams.getFirst(key);
                variables.put(key, value);
            }
        }
        return variables;
    }

}
