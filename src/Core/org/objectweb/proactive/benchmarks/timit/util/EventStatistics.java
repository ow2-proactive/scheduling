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
package org.objectweb.proactive.benchmarks.timit.util;

import java.io.Serializable;

import org.objectweb.proactive.benchmarks.timit.TimIt;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventDataBag;


/**
 * Represents some pure counter statistics of one run
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */
public class EventStatistics implements Serializable {

    /**
     *
     */
    private String[] counterName;
    private Object[] value;
    private EventDataBag statDataBag;
    private int nb;
    private int padding;
    private boolean empty;

    public EventStatistics() {
        this(new String[1], new Object[1], 0, null);
        this.empty = true;
    }

    public EventStatistics(String[] counterName, Object[] value, int nb,
        EventDataBag statDataBag) {
        this.counterName = counterName.clone();
        this.value = value.clone();
        this.statDataBag = statDataBag;
        this.nb = nb;
        this.empty = false;
    }

    public Object getEventValue(int i) {
        return this.value[i];
    }

    public Object getEventValue(String name) { // returns an EventData
        for (int i = 0; i < this.counterName.length; i++) {
            if (name.equals(this.counterName[i])) {
                return this.value[i];
            }
        }
        return null;
    }

    public Object getEventDataValue(String name) {
        for (int i = 0; i < this.counterName.length; i++) {
            if (name.equals(this.counterName[i])) {
                return this.statDataBag.getEventData(i).getFinalized();
            }
        }
        return null;
    }

    public String getFormValue(int i) {
        return TimIt.df.format(this.value[i]);
    }

    public String getName(int i) {
        return this.counterName[i];
    }

    public EventDataBag getStatDataBag() {
        return this.statDataBag;
    }

    public int getNb() {
        return this.nb;
    }

    public void setTimerName(int id, String name) {
        this.counterName[id] = name;
    }

    @Override
    public String toString() {
        if (!this.empty) {
            return this.show();
        }
        return "";
    }

    public String show() {
        String result = "";

        for (int i = 0; i < this.nb; i++) {
            result += (this.counterName[i] + " : " + this.value[i] + "\n");
        }

        return result;
    }

    public final String format(double t) {
        return this.paddingString(TimIt.df.format(t), this.padding, ' ', true) +
        "    ";
    }

    /**
     * pad a string S with a size N with char C on the left (True) or on the
     * right(false)
     */
    private String paddingString(String s, int n, char c, boolean paddingLeft) {
        StringBuffer str = new StringBuffer(s);
        int strLength = str.length();
        if ((n > 0) && (n > strLength)) {
            for (int i = 0; i <= n; i++) {
                if (paddingLeft) {
                    if (i < (n - strLength)) {
                        str.insert(0, c);
                    }
                } else {
                    if (i > strLength) {
                        str.append(c);
                    }
                }
            }
        }
        return str.toString();
    }
}
