package org.objectweb.proactive.core.mop;


/**
 * This class holds the type of a method call (OneWay, Asynchronous and Synchronous)
 * along with the reason associated if it is synchronous
 * @author fviale
 *
 */
public class MethodCallInfo {
    public enum CallType {OneWay,
        Asynchronous,
        Synchronous;
    }
    public enum SynchronousReason {NotApplicable,
        ThrowsCheckedException,
        NotReifiable;
    }
    private CallType type;
    private SynchronousReason reason;
    private String message;

    public MethodCallInfo() {
    }

    /**
     * Constructor used when we don't care what the reason for this call is
     * @param type
     */
    public MethodCallInfo(CallType type) {
        this.setType(type);
        this.setReason(SynchronousReason.NotApplicable);
        this.setMessage(null);
    }

    public MethodCallInfo(CallType type, SynchronousReason reason,
        String message) {
        this.setType(type);
        this.setReason(reason);
        this.setMessage(message);
    }

    public CallType getType() {
        return type;
    }

    public SynchronousReason getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    public void setType(CallType type) {
        this.type = type;
    }

    public void setReason(SynchronousReason reason) {
        this.reason = reason;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
