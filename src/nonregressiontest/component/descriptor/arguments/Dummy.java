package nonregressiontest.component.descriptor.arguments;


/**
 * @author Matthieu Morel
 *
 */
public class Dummy implements Action, Info {
    
    String info;
    
    public Dummy() {}
    
    /* (non-Javadoc)
     * @see nonregressiontest.component.descriptor.arguments.Action#doSomething()
     */
    public String doSomething() {
        return ("This component is storing the info : " + info);
    }
    /* (non-Javadoc)
     * @see nonregressiontest.component.descriptor.arguments.Info#getInfo()
     */
    public String getInfo() {
        return info;
    }
    /* (non-Javadoc)
     * @see nonregressiontest.component.descriptor.arguments.Info#setInfo(java.lang.String)
     */
    public void setInfo(String info) {
        this.info=info;

    }

}
