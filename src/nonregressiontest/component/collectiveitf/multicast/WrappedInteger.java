package nonregressiontest.component.collectiveitf.multicast;

import java.io.Serializable;


public class WrappedInteger implements Serializable {
    
    
    private Integer intValue;
    
    public WrappedInteger() {}

    public WrappedInteger(Integer value) {
        intValue = new Integer(value);
    }
    
    /**
     * @return Returns the intValue.
     */
    public Integer getIntValue() {
    
        return intValue;
    }

   
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof WrappedInteger) {
            return intValue.equals(((WrappedInteger)obj).getIntValue());
        }
        return false;
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return intValue.hashCode();
    }
    
    
    
    

}
