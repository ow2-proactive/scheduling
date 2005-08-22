package nonregressiontest.component;


/**
 *
 * Sleeps a bit while processing the messages. This allows the filling of the queue while processing the request.
 * Then it is possible to test the use of filters on non functional requests.
 * @author Matthieu Morel
 *
 */
public class SlowPrimitiveComponentB extends PrimitiveComponentB {

    /*
     * @see nonregressiontest.component.I2#processOutputMessage(nonregressiontest.component.Message)
     */
    public Message processOutputMessage(Message message) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return super.processOutputMessage(message);
    }
}
