package org.objectweb.proactive.p2p.peerconfiguration;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerListModel;

/**
 * This 1.4 example is used by the various SpinnerDemos.
 * It implements a SpinnerListModel that works only with
 * an Object array and that implements cycling (the next
 * value and previous value are never null).  It also
 * lets you optionally associate a spinner model that's
 * linked to this one, so that when a cycle occurs the
 * linked spinner model is updated.

 * The SpinnerDemos use the CyclingSpinnerListModel for
 * a month spinner that (in SpinnerDemo3) is tied to the
 * year spinner, so that -- for example -- when the month
 * changes from December to January, the year increases.
 */
public class CyclingSpinnerListModel extends SpinnerListModel {
    Object firstValue, lastValue;
    SpinnerModel linkedModel = null;

    public CyclingSpinnerListModel(Object[] values) {
        super(values);
        firstValue = values[0];
        lastValue = values[values.length - 1];
    }

    public void setLinkedModel(SpinnerModel linkedModel) {
        this.linkedModel = linkedModel;
    }

    public Object getNextValue() {
        Object value = super.getNextValue();
        if (value == null) {
            value = firstValue;
            if (linkedModel != null) {
                linkedModel.setValue(linkedModel.getNextValue());
            }
        }
        return value;
    }

    public Object getPreviousValue() {
        Object value = super.getPreviousValue();
        if (value == null) {
            value = lastValue;
            if (linkedModel != null) {
                linkedModel.setValue(linkedModel.getPreviousValue());
            }
        }
        return value;
    }
}
