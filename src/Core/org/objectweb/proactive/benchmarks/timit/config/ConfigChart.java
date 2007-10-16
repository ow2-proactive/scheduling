/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.benchmarks.timit.config;

import org.jdom.Element;
import org.objectweb.proactive.benchmarks.timit.util.charts.Chart;


public class ConfigChart extends Tag {
    public ConfigChart(Element eChart) {
        super(eChart);
    }

    @Override
    public String get(String name) {
        name = name.toLowerCase();
        String value = super.get(name);

        if (value != null) {
            return value;
        }

        // Specify default values
        if (name.equalsIgnoreCase("width")) {
            return "800";
        }
        if (name.equalsIgnoreCase("height")) {
            return "600";
        }
        if (name.equalsIgnoreCase("scalemode")) {
            return "" + Chart.Scale.DEFAULT;
        }
        if (name.equalsIgnoreCase("legendformatmode")) {
            return "" + Chart.LegendFormat.DEFAULT;
        }
        if (name.equalsIgnoreCase("alpha")) {
            return "255";
        }
        if (name.equalsIgnoreCase("filter")) {
            return "";
        }
        if (name.equalsIgnoreCase("subtitle")) {
            return "";
        }

        throw new RuntimeException("Variable chart.'" + name +
            "' missing in configuration file");
    }

    public static Chart.Scale scaleValue(String scaleMode) {
        if (scaleMode.equalsIgnoreCase("LINEAR")) {
            return Chart.Scale.LINEAR;
        }
        if (scaleMode.equalsIgnoreCase("LOGARITHMIC")) {
            return Chart.Scale.LOGARITHMIC;
        }
        return Chart.Scale.DEFAULT;
    }

    public static Chart.LegendFormat legendValue(String legendMode) {
        if (legendMode.equalsIgnoreCase("NONE")) {
            return Chart.LegendFormat.NONE;
        }
        if (legendMode.equalsIgnoreCase("POW10")) {
            return Chart.LegendFormat.POW10;
        }
        if (legendMode.equalsIgnoreCase("POW2")) {
            return Chart.LegendFormat.POW2;
        }
        return Chart.LegendFormat.DEFAULT;
    }
}
