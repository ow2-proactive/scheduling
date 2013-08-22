package org.ow2.proactive.scheduler.core.db;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;


@Embeddable
public class FlowActionData {

    private FlowActionType type;

    private int dupNumber;

    private String target;

    private String targetContinuation;

    private String targetElse;

    @Column(name = "FA_TYPE")
    public FlowActionType getType() {
        return type;
    }

    public void setType(FlowActionType type) {
        this.type = type;
    }

    @Column(name = "FA_DUP_NUMBER")
    public int getDupNumber() {
        return dupNumber;
    }

    public void setDupNumber(int dupNumber) {
        this.dupNumber = dupNumber;
    }

    @Column(name = "FA_TARGET")
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Column(name = "FA_TARGET_CONTINUE")
    public String getTargetContinuation() {
        return targetContinuation;
    }

    public void setTargetContinuation(String targetContinuation) {
        this.targetContinuation = targetContinuation;
    }

    @Column(name = "FA_TARGET_ELSE")
    public String getTargetElse() {
        return targetElse;
    }

    public void setTargetElse(String targetElse) {
        this.targetElse = targetElse;
    }

}
