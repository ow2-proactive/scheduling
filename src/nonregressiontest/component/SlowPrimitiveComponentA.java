package nonregressiontest.component;

/**
 * 
 * Sleeps a bit while processing the messages. This allows the filling of the queue while processing the request.
 * Then it is possible to test the use of filters on non functional requests.
 * 
 * @author Matthieu Morel
 *
 */
public class SlowPrimitiveComponentA extends PrimitiveComponentA {

    /*
     * @see nonregressiontest.component.I1#processInputMessage(nonregressiontest.component.Message)
     */
    public Message processInputMessage(Message message) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return super.processInputMessage(message);

    }
}
