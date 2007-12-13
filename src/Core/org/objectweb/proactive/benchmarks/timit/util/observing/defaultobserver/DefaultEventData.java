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
package org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver;

import java.util.Vector;

import org.objectweb.proactive.benchmarks.timit.util.observing.EventData;


/**
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 */
public class DefaultEventData implements EventData {

    /**
     *
     */
    public static final int MIN = 0;
    public static final int MAX = 1;
    public static final int AVERAGE = 2;
    public static final int SUM = 3;
    private String name;
    private int collapseOperation;
    private int notifyOperation;
    private double value;
    private double result;
    private int nbNotify;
    private Vector<Object> collapsedValues;

    /** Creates a new instance of DefaultStatData */
    public DefaultEventData(String name, int collapseOperation, int notifyOperation) {
        this.name = name;
        this.collapseOperation = collapseOperation;
        this.notifyOperation = notifyOperation;
        this.nbNotify = 0;
        this.result = 0;

        switch (this.notifyOperation) {
            case DefaultEventData.MIN:
                this.value = Double.MAX_VALUE;
                break;
            case DefaultEventData.MAX:
                this.value = Double.MIN_VALUE;
                break;
            default:
                this.value = 0;
        }
    }

    public void performNotifyOperation(double value) {
        switch (this.notifyOperation) {
            case DefaultEventData.MIN:
                if (value < this.value) {
                    this.value = value;
                }
                break;
            case DefaultEventData.MAX:
                if (value > this.value) {
                    this.value = value;
                }
                break;
            case DefaultEventData.AVERAGE:
                this.nbNotify++;
                this.value += value;
                break;
            default:
                this.value += value;
        }
    }

    public void setData(Object object) {
    }

    public Object getData() {
        if (this.notifyOperation == DefaultEventData.AVERAGE) {
            this.value /= this.nbNotify;
        }
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    /**
     * @return The vector of collapsed values.
     */
    public Object collapseWith(EventData anotherData, int anotherRank) {
        if (this.collapsedValues == null) {
            this.collapsedValues = new Vector<Object>();
            // Add current value
            if (this.notifyOperation == DefaultEventData.AVERAGE) {
                this.value /= this.nbNotify;
            }
            this.collapsedValues.add(this.value);
        }
        if (anotherData.equals(this)) {
            this.result = this.value;
            return this.collapsedValues;
        }

        this.collapsedValues.add(anotherData.getData());
        switch (this.collapseOperation) {
            case DefaultEventData.MIN:
                this.result = Double.MAX_VALUE;
                for (int i = 0; i < this.collapsedValues.size(); i++) {
                    this.result = (((Double) this.collapsedValues.get(i) < this.result) ? (Double) this.collapsedValues
                            .get(i)
                            : this.result);
                }
                break;
            case DefaultEventData.MAX:
                this.result = Double.MIN_VALUE;
                for (int i = 0; i < this.collapsedValues.size(); i++) {
                    this.result = (((Double) this.collapsedValues.get(i) > this.result) ? (Double) this.collapsedValues
                            .get(i)
                            : this.result);
                }
                break;
            case DefaultEventData.AVERAGE:
                this.result = 0;
                for (int i = 0; i < this.collapsedValues.size(); i++) {
                    this.result += (Double) this.collapsedValues.get(i);
                }
                this.result /= this.collapsedValues.size();
                break;
            default:
                this.result = 0;
                for (int i = 0; i < this.collapsedValues.size(); i++) {
                    this.result += (Double) this.collapsedValues.get(i);
                }
                break;
        }
        return this.collapsedValues;
    }

    public Object getFinalized() {
        return this.result;
    }

    @Override
    public String toString() {
        return "" + this.result;
    }
}
