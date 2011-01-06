package ScilabObjects;

public class ScilabJavaClass extends ScilabJavaObject {

    protected ScilabJavaConstructor sjc = null;
    
    public ScilabJavaClass(Class cls) {
	super(cls, cls);
	sjc = new ScilabJavaConstructor(cls);
    }

    public static int newInstance(int id, int[] args) throws ScilabJavaException {
	if (id == 0) {
	    throw new ScilabJavaException("null cannot be instantiated");
	}
	if ((arraySJO[id] instanceof ScilabJavaClass) && ((ScilabJavaClass)arraySJO[id]).sjc != null) {
	    return new ScilabJavaObject(((ScilabJavaClass)arraySJO[id]).sjc.invoke(args)).id;
	}
	throw new ScilabJavaException("The object " + id + " is not a valid Class object");
    }
}