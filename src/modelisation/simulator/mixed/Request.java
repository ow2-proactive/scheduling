/*
 * Created by IntelliJ IDEA.
 * User: fhuet
 * Date: May 2, 2002
 * Time: 6:34:59 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package modelisation.simulator.mixed;

import modelisation.simulator.common.SimulatorElement;

public class Request extends SimulatorElement {

    public static final int SOURCE = 0;
    public static final int AGENT = 1;

    protected int number;

    public Request() {
    }

    public Request(int state, int number) {
        this.state = state;
        this.number = number;
    }

    public boolean isFromAgent() {
        return (this.state == AGENT);
    }

    public int getNumber() {
        return this.number;
    }


    public void update(double time) {

    }

    public String toString() {

        String s = new String("request " +
                              ((state == SOURCE)?" from source":" from agent"));
        s = s + " number " + number;
        return s;

    }


}
