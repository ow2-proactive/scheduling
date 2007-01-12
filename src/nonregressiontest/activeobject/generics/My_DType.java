package nonregressiontest.activeobject.generics;

import java.io.Serializable;


/**
 * A class with a '_' in its name to check the generation of the Stub whith a
 * correct name, no mix with '_' as separator.
 * @author cdalmass
 *
 */
public class My_DType implements Serializable {
    private String str = null;

    public My_DType(String str) {
        this.str = str;
    }

    public String toString() {
        return str;
    }
}
