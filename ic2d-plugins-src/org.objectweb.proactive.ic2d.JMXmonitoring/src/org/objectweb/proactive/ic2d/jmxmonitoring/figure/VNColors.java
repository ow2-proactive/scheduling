package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;


public class VNColors {
    private static final Color[] colors = {
            ColorConstants.cyan, ColorConstants.yellow, ColorConstants.lightBlue,
            ColorConstants.orange, ColorConstants.green
        };
    private Map<String, Color> vnColors;

    public VNColors() {
        vnColors = new HashMap<String, Color>();
    }

    public Color getColor(String vnID) {
        Color c = vnColors.get(vnID);
        if (c == null) {
            c = colors[vnColors.size() % 5];
            vnColors.put(vnID, c);
        }
        return c;
    }
}
