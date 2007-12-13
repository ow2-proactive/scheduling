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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Attribute;
import org.jdom.Element;
import org.objectweb.proactive.benchmarks.timit.util.XMLHelper;


public class Series extends Tag {
    private ConfigChart[] charts;
    private Benchmark[] benchmarks;

    public Series(Element eSerie) {
        super(eSerie);

        // Construct ConfigChart array (if needed)
        if (eSerie.getChild("charts") != null) {
            List chartList = eSerie.getChild("charts").getChildren();
            this.charts = new ConfigChart[chartList.size()];
            for (int i = 0; i < this.charts.length; i++) {
                this.charts[i] = new ConfigChart((Element) chartList.get(i));
            }
        }

        // Construct Benchmark array
        List benchList = eSerie.getChild("benchmarks").getChildren();
        this.benchmarks = Benchmark.toArray(benchList);
    }

    @Override
    public String get(String name) {
        name = name.toLowerCase();
        String value = super.get(name);

        if (value != null) {
            return value;
        }

        if (name.equals("errorfile")) {
            return "error.log";
        }

        throw new RuntimeException("Variable benchmark.'" + name + "' missing in configuration file");
    }

    public Benchmark[] getBenchmarks() {
        return this.benchmarks.clone();
    }

    public ConfigChart[] getCharts() {
        if (this.charts == null) {
            return null;
        }
        return this.charts.clone();
    }

    @Override
    public String toString() {
        return super.toString() + Arrays.toString(this.charts) + "  -  " + Arrays.toString(this.benchmarks) +
            "\n";
    }

    public static Series[] oldtoArray(List serieList) {
        int quantity = serieList.size();
        Series[] result = new Series[quantity];

        for (int i = 0; i < quantity; i++) {
            Element eSerie = (Element) serieList.get(i);
            result[i] = new Series(eSerie);
        }

        return result;
    }

    public static Series[] toArray(List serieList) {
        ArrayList<String> seqList;
        int quantity = serieList.size();
        ArrayList<Series> result = new ArrayList<Series>();

        Pattern p = Pattern.compile("[^\\x23\\x7B\\x7D]*\\x23\\x7B" + // *#{
            "([^\\x7D]*)" + // A,B,C
            "\\x7D[^\\x7D\\x23\\x7B]*"); // }*

        for (int i = 0; i < quantity; i++) {
            Element eSerie = (Element) serieList.get(i);
            seqList = new ArrayList<String>();

            // 1 : searching sequences in attributes
            Iterator itAttr = eSerie.getAttributes().iterator();
            while (itAttr.hasNext()) {
                Attribute attr = (Attribute) itAttr.next();
                Matcher m = p.matcher(attr.getValue());
                while (m.find()) {
                    String sequence = m.group(1);
                    if (!seqList.contains(sequence)) {
                        seqList.add(sequence);
                    }
                }
            }

            // 2 : expanding sequences (recursive call)
            if (seqList.size() > 0) {
                expand(eSerie, seqList, 0, result);
            } else {
                result.add(new Series(eSerie));
            }
        }

        return result.toArray(new Series[1]);
    }

    private static void expand(Element eSerie, ArrayList<String> seqList, int index, ArrayList<Series> out) {
        String seq = seqList.get(index);
        String[] values = seq.split(",");

        for (String value : values) {
            Element eSerieClone = (Element) eSerie.clone();
            XMLHelper.replaceAll(eSerieClone, "\\x23\\x7B" + seq + "\\x7D", // #{*}
                    value);
            Iterator itDesc = eSerieClone.getDescendants();
            while (itDesc.hasNext()) {
                Object eDesc = itDesc.next();
                if (eDesc instanceof Element) {
                    XMLHelper.replaceAll((Element) eDesc, "\\x23\\x7B" + seq + "\\x7D", // #{*},
                            value);
                }
            }

            if ((index + 1) < seqList.size()) {
                expand(eSerieClone, seqList, index + 1, out);
            } else {
                out.add(new Series(eSerieClone));
            }
        }
    }
}
