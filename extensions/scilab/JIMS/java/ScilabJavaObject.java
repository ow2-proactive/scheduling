package ScilabObjects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScilabJavaObject {

    protected static int currentCapacity = 1024;
    protected static int currentPos = 1;
    protected static ScilabJavaObject[] arraySJO = new ScilabJavaObject[currentCapacity];
    protected static List<Integer> freePlace = new ArrayList<Integer>(currentCapacity);
    protected static final Map<Class, Integer> unwrappableType = new HashMap<Class, Integer>(26);

    private final static Class[] returnType = new Class[1];

    static {
        unwrappableType.put(double.class, 0);
        unwrappableType.put(double[].class, 1);
        unwrappableType.put(double[][].class, 2);
        unwrappableType.put(String.class, 3);
        unwrappableType.put(String[].class, 4);
        unwrappableType.put(String[][].class, 5);
        unwrappableType.put(int.class, 6);
        unwrappableType.put(int[].class, 7);
        unwrappableType.put(int[][].class, 8);
        unwrappableType.put(boolean.class, 9);
        unwrappableType.put(boolean[].class, 10);
        unwrappableType.put(boolean[][].class, 11);
        unwrappableType.put(byte.class, 12);
        unwrappableType.put(byte[].class, 13);
        unwrappableType.put(byte[][].class, 14);
        unwrappableType.put(short.class, 15);
        unwrappableType.put(short[].class, 16);
        unwrappableType.put(short[][].class, 17);
        unwrappableType.put(char.class, 18);
        unwrappableType.put(char[].class, 19);
        unwrappableType.put(char[][].class, 20);
        unwrappableType.put(float.class, 21);
        unwrappableType.put(float[].class, 22);
        unwrappableType.put(float[][].class, 23);
        unwrappableType.put(long.class, 24);
        unwrappableType.put(long[].class, 25);
        unwrappableType.put(long[][].class, 26);
    }

    protected Map<String, ScilabJavaMethod> methods;
    protected Object object;
    protected Class clazz;

    protected int id;

    public ScilabJavaObject(Object obj) {
        this(obj, obj == null ? null : obj.getClass());
    }

    public ScilabJavaObject(Object obj, Class clazz) {
        this.object = obj;
        this.clazz = clazz;

        if (obj != null) {
            if (freePlace.isEmpty()) {
                this.id = currentPos;
                ++currentPos;
            } else {
                this.id = freePlace.remove(0);
            }

            arraySJO[this.id] = this;

            if (currentPos >= currentCapacity) {
                currentCapacity = currentCapacity * 2;
                ScilabJavaObject[] arr = new ScilabJavaObject[currentCapacity];
                System.arraycopy(arraySJO, 0, arr, 0, currentPos);
                arraySJO = arr;
            }

            this.methods = ScilabJavaMethod.getMethods(clazz);
        } else {
            this.id = 0;
            //this.object = "null";
            this.object = null;
            this.clazz = Void.class;
            arraySJO[0] = this;
        }
    }

    public static String getRepresentation(int id) {
        if (arraySJO[id].object != null) {
        return arraySJO[id].object.toString();
        } else {
            return "null";
        }
    }

    public static String[] getAccessibleMethods(int id) {
        if (id > 0) {
            return arraySJO[id].methods.keySet().toArray(new String[0]);
        } else {
            return new String[0];
        }
    }

    public static String[] getAccessibleFields(int id) {
        if (id > 0) {
            Field[] f = arraySJO[id].clazz.getFields();
            String[] sf = new String[f.length];
            for (int i = 0; i < f.length; i++) {
                sf[i] = f[i].getName();
            }
            return sf;
        } else {
            return new String[0];
        }
    }

    public static String getClassName(int id) {
        if (id > 0) {
            return arraySJO[id].clazz.getName();
        } else {
            return "null";
        }
    }

    public static void setField(int id, String fieldName, int idarg) throws ScilabJavaException {
        if (id > 0) {
            try {
                arraySJO[id].clazz.getField(fieldName).set(arraySJO[id].object, arraySJO[idarg].object);
            } catch (NoSuchFieldException e) {
                throw new ScilabJavaException("No field " + fieldName + " in object " + getClassName(id));
            } catch (IllegalArgumentException e) {
                throw new ScilabJavaException("Bad argument value for field " + fieldName + " in object " + getClassName(id));
            } catch (IllegalAccessException e) {
                throw new ScilabJavaException("Cannot access to the field " + fieldName + " in object " + getClassName(id));
            }
        } else {
            throw new ScilabJavaException("null is not an object");
        }
    }

    public static int getField(int id, String fieldName) throws ScilabJavaException {
        if (id > 0) {
            try {
                return new ScilabJavaObject(arraySJO[id].clazz.getField(fieldName).get(arraySJO[id].object)).id;
            } catch (NoSuchFieldException e) {
                throw new ScilabJavaException("No field " + fieldName + " in object " + getClassName(id));
            } catch (IllegalArgumentException e) {
                throw new ScilabJavaException("Bad argument value for field " + fieldName + " in object " + getClassName(id));
            } catch (IllegalAccessException e) {
                throw new ScilabJavaException("Cannot access to the field " + fieldName + " in object " + getClassName(id));
            }
        } else {
            throw new ScilabJavaException("null is not an object");
        }
    }

    public static int invoke(int id, String methName, int[] args) throws ScilabJavaException {
        if (id > 0) {
            return new ScilabJavaObject(arraySJO[id].methods.get(methName).invoke(arraySJO[id].object, returnType, args), returnType[0]).id;
        } else {
            throw new ScilabJavaException("null is not an object");
        }
    }

    public static int javaCast(int id, String objName) throws ScilabJavaException {
        if (id > 0) {
            int idC = ScilabClassLoader.loadJavaClass(objName);
            Class clazz = (Class) arraySJO[idC].object;
            try {
                return new ScilabJavaObject(clazz.cast(arraySJO[id].object), clazz).id;
            } catch (ClassCastException e) {
                throw new ScilabJavaException("Cannot cast object " + getClassName(id) + " into " + getClassName(idC));
            }
        } else {
            throw new ScilabJavaException("null is not an object");
        }
    }

    public static void removeScilabJavaObject(int id) {
        if (id > 0) {
            freePlace.add(0, id);
            arraySJO[id] = null;
        }
    }

    public static void garbageCollect() {
        System.gc();
    }

    public static int wrapDouble(double x) {
        return new ScilabJavaObject(x, double.class).id;
    }

    public static int wrapDouble(double[] x) {
        return new ScilabJavaObject(x, double[].class).id;
    }

    public static int wrapDouble(double[][] x) {
        return new ScilabJavaObject(x, double[][].class).id;
    }

    public static int wrapInt(int x) {
        return new ScilabJavaObject(x, int.class).id;
    }

    public static int wrapInt(int[] x) {
        return new ScilabJavaObject(x, int[].class).id;
    }

    public static int wrapInt(int[][] x) {
        return new ScilabJavaObject(x, int[][].class).id;
    }

    public static int wrapUInt(long x) {
        return new ScilabJavaObject(x, long.class).id;
    }

    public static int wrapUInt(long[] x) {
        return new ScilabJavaObject(x, long[].class).id;
    }

    public static int wrapUInt(long[][] x) {
        return new ScilabJavaObject(x, long[][].class).id;
    }

    public static int wrapByte(byte x) {
        return new ScilabJavaObject(x, byte.class).id;
    }

    public static int wrapByte(byte[] x) {
        return new ScilabJavaObject(x, byte[].class).id;
    }

    public static int wrapByte(byte[][] x) {
        return new ScilabJavaObject(x, byte[][].class).id;
    }

    public static int wrapShort(short x) {
        return new ScilabJavaObject(x, short.class).id;
    }

    public static int wrapShort(short[] x) {
        return new ScilabJavaObject(x, short[].class).id;
    }

    public static int wrapShort(short[][] x) {
        return new ScilabJavaObject(x, short[][].class).id;
    }

    public static int wrapUShort(int x) {
        return new ScilabJavaObject(x, int.class).id;
    }

    public static int wrapUShort(int[] x) {
        return new ScilabJavaObject(x, int[].class).id;
    }

    public static int wrapUShort(int[][] x) {
        return new ScilabJavaObject(x, int[][].class).id;
    }

    public static int wrapUByte(short x) {
        return new ScilabJavaObject(x, short.class).id;
    }

    public static int wrapUByte(short[] x) {
        return new ScilabJavaObject(x, short[].class).id;
    }

    public static int wrapUByte(short[][] x) {
        return new ScilabJavaObject(x, short[][].class).id;
    }

    public static int wrapString(String x) {
        if (x != null) {
            return new ScilabJavaObject(x, String.class).id;
        } else {
            return new ScilabJavaObject(null).id;
        }
    }

    public static int wrapString(String[] x) {
        return new ScilabJavaObject(x, String[].class).id;
    }

    public static int wrapString(String[][] x) {
        return new ScilabJavaObject(x, String[][].class).id;
    }

    public static int wrapBoolean(boolean x) {
        return new ScilabJavaObject(x, boolean.class).id;
    }

    public static int wrapBoolean(boolean[] x) {
        return new ScilabJavaObject(x, boolean[].class).id;
    }

    public static int wrapBoolean(boolean[][] x) {
        return new ScilabJavaObject(x, boolean[][].class).id;
    }

    public static int wrapChar(char x) {
        return new ScilabJavaObject(x, char.class).id;
    }

    public static int wrapChar(char[] x) {
        return new ScilabJavaObject(x, char[].class).id;
    }

    public static int wrapChar(char[][] x) {
        return new ScilabJavaObject(x, char[][].class).id;
    }

    public static int wrapFloat(float x) {
        return new ScilabJavaObject(x, float.class).id;
    }

    public static int wrapFloat(float[] x) {
        return new ScilabJavaObject(x, float[].class).id;
    }

    public static int wrapFloat(float[][] x) {
        return new ScilabJavaObject(x, float[][].class).id;
    }

    public static int wrapLong(long x) {
        return new ScilabJavaObject(x, long.class).id;
    }

    public static int wrapLong(long[] x) {
        return new ScilabJavaObject(x, long[].class).id;
    }

    public static int wrapLong(long[][] x) {
        return new ScilabJavaObject(x, long[][].class).id;
    }

    public static double unwrapDouble(int id) {
        return ((Double) (arraySJO[id].object)).doubleValue();
    }

    public static double[] unwrapRowDouble(int id) {
        return (double[]) (arraySJO[id].object);
    }

    public static double[][] unwrapMatDouble(int id) {
        return (double[][]) (arraySJO[id].object);
    }

    public static int unwrapInt(int id) {
        return ((Integer) (arraySJO[id].object)).intValue();
    }

    public static int[] unwrapRowInt(int id) {
        return (int[]) (arraySJO[id].object);
    }

    public static int[][] unwrapMatInt(int id) {
        return (int[][]) (arraySJO[id].object);
    }

    public static short unwrapShort(int id) {
        return ((Short) (arraySJO[id].object)).shortValue();
    }

    public static short[] unwrapRowShort(int id) {
        return (short[]) (arraySJO[id].object);
    }

    public static short[][] unwrapMatShort(int id) {
        return (short[][]) (arraySJO[id].object);
    }

    public static byte unwrapByte(int id) {
        return ((Byte) (arraySJO[id].object)).byteValue();
    }

    public static byte[] unwrapRowByte(int id) {
        return (byte[]) (arraySJO[id].object);
    }

    public static byte[][] unwrapMatByte(int id) {
        return (byte[][]) (arraySJO[id].object);
    }

    public static String unwrapString(int id) {
        return (String) (arraySJO[id].object);
    }

    public static String[] unwrapRowString(int id) {
        return (String[]) (arraySJO[id].object);
    }

    public static String[][] unwrapMatString(int id) {
        return (String[][]) (arraySJO[id].object);
    }

    public static boolean unwrapBoolean(int id) {
        return (Boolean) (arraySJO[id].object);
    }

    public static boolean[] unwrapRowBoolean(int id) {
        return (boolean[]) (arraySJO[id].object);
    }

    public static boolean[][] unwrapMatBoolean(int id) {
        return (boolean[][]) (arraySJO[id].object);
    }

    public static char unwrapChar(int id) {
        return (Character) (arraySJO[id].object);
    }

    public static char[] unwrapRowChar(int id) {
        return (char[]) (arraySJO[id].object);
    }

    public static char[][] unwrapMatChar(int id) {
        return (char[][]) (arraySJO[id].object);
    }

    public static float unwrapFloat(int id) {
        return (Float) (arraySJO[id].object);
    }

    public static float[] unwrapRowFloat(int id) {
        return (float[]) (arraySJO[id].object);
    }

    public static float[][] unwrapMatFloat(int id) {
        return (float[][]) (arraySJO[id].object);
    }

    public static long unwrapLong(int id) {
        return (Long) (arraySJO[id].object);
    }

    public static long[] unwrapRowLong(int id) {
        return (long[]) (arraySJO[id].object);
    }

    public static long[][] unwrapMatLong(int id) {
        return (long[][]) (arraySJO[id].object);
    }

    public static int isUnwrappable(int id) {
        Integer t = unwrappableType.get(arraySJO[id].clazz);
        if (t != null) {
            return t.intValue();
        } else {
            return -1;
        }
    }
}