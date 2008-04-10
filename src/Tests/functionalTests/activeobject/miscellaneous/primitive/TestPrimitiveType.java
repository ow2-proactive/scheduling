package functionalTests.activeobject.miscellaneous.primitive;

import java.io.Serializable;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;


public class TestPrimitiveType extends FunctionalTest {

    @Test
    public void testBoolean() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Boolean.class.getName(), new Object[] {});
    }

    @Test
    public void testByte() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Byte.class.getName(), new Object[] {});
    }

    @Test
    public void testChar() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Char.class.getName(), new Object[] {});
    }

    @Test
    public void testShort() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Short.class.getName(), new Object[] {});
    }

    @Test
    public void testInt() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Int.class.getName(), new Object[] {});
    }

    @Test
    public void testLong() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Long.class.getName(), new Object[] {});
    }

    @Test
    public void testFloat() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Float.class.getName(), new Object[] {});
    }

    @Test
    public void testDouble() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Double.class.getName(), new Object[] {});
    }

    static public class Boolean implements Serializable {
        public Boolean() {
        }

        public void serve(boolean[] buf) {
        }
    }

    static public class Byte implements Serializable {
        public Byte() {
        }

        public void serve(byte[] buf) {
        }
    }

    static public class Char implements Serializable {
        public Char() {
        }

        public void serve(char[] buf) {
        }
    }

    static public class Short implements Serializable {
        public Short() {
        }

        public void serve(short[] buf) {
        }
    }

    static public class Int implements Serializable {
        public Int() {
        }

        public void serve(int[] buf) {
        }
    }

    static public class Long implements Serializable {
        public Long() {
        }

        public void serve(long[] buf) {
        }
    }

    static public class Float implements Serializable {
        public Float() {
        }

        public void serve(float[] buf) {
        }
    }

    static public class Double implements Serializable {
        public Double() {
        }

        public void serve(double[] buf) {
        }
    }

}
