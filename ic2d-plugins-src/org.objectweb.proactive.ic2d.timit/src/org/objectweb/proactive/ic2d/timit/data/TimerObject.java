package org.objectweb.proactive.ic2d.timit.data;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;


public class TimerObject extends AbstractObject {
    public static final String P_CHILDREN = "_children";
    public static final String P_LABEL = "_label";
    public static final String P_SELECTION = "_selection";
    public static final String P_VIEW = "_view";
    public static final String P_EXPAND_STATE = "_expand_state";
    protected double percentageFromParent;
    protected String labelName;
    protected TimerObject parent;
    protected BasicTimer currentTimer;
    protected List<TimerObject> children;

    /** boolean used during the build of a chart **/
    protected boolean isViewed;

    public TimerObject(String name, boolean isViewed) {
        this.labelName = name;
        this.isViewed = isViewed;
        this.children = new ArrayList<TimerObject>();
    }

    public TimerObject(BasicTimer currentTimer, TimerObject parent) {
        this.parent = parent;
        this.currentTimer = currentTimer;
        this.isViewed = true;
        this.updateLabel();
        if (parent != null) {
            parent.children.add(this);
        }
        this.children = new ArrayList<TimerObject>();
    }

    public void setCurrentTimer(BasicTimer currentTimer) {
        this.currentTimer = currentTimer;
        this.updateLabel();
        //		if ( this.parent!= null && !this.parent.children.contains(this) ){
        //			System.out.println("TimerObject.setCurrentTimer() ------> ");
        //			this.parent.children.add(this);
        //		}
    }

    public void updateLabel() {
        this.updatePercentage();
        this.labelName = currentTimer.getName() + " [ " +
            String.format("%1.2f", this.percentageFromParent) +
            "% , cumulatedTime : " +
            Math.ceil((double) currentTimer.getTotalTime() / 1000000d) +
            "ms ]";
    }

    public void updatePercentage() {
        double currentTotalTime;
        double parentTotalTime;
        if ((parent != null) && (parent.currentTimer != null) &&
                ((currentTotalTime = this.currentTimer.getTotalTime()) != 0) &&
                ((parentTotalTime = this.parent.currentTimer.getTotalTime()) != 0)) {
            this.percentageFromParent = (currentTotalTime * 100) / parentTotalTime;
        }
    }

    public List<TimerObject> getChildren() {
        return this.children;
    }

    public String getLabelName() {
        return this.labelName;
    }

    public String toString() {
        return this.labelName;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public void setViewed(boolean isViewedNew) {
        if (this.parent == null) {
            return;
        }
        if (this.isViewed == true) {
            if (isViewedNew == false) {
                // Remove this from parent			
                this.parent.children.remove(this);
            }
        } else {
            if ((isViewedNew == true) && !this.parent.children.contains(this)) {
                // Add this to parent
                this.parent.children.add(this);
            }
        }

        this.isViewed = isViewedNew;
    }

    public TimerObject getParent() {
        return parent;
    }

    public void setParent(TimerObject parent) {
        this.parent = parent;
    }

    public BasicTimer getCurrentTimer() {
        return currentTimer;
    }
}
