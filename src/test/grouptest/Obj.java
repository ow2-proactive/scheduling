package test.grouptest;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeException;


public class Obj implements Active, java.io.Serializable {


    private MyString name;



    public Obj () {}

    public Obj(MyString ms) {
	name = ms;
    }



    public void doNothing () {}

    public MyString getName () {
	return name;
    }

    public void sayYourName () {
	System.out.println("My name is " + getName());
    }

	public void sayYourNameNTimes (int n) throws Exception {
	for (int i=0 ; i<n ; i++)
		System.out.println("My name is " + getName());
	}

	public void say (MyString s) {
	System.out.println(getName() + " says : \"" + s + "\"");
	}

	public void say (MyString s, int i) {
	System.out.println(getName() + " says : \"" + s + " (" + i + ")\"");
	}

	public void say (MyString s, Object o) {
	System.out.println(getName() + " says : \"" + s + " (" + o + ")\"");
	}

	public MyString write (MyString s) {
	return new MyString(getName() + " says : \"" + s + "\"");
	}

	public MyString write (MyString s, int i) {
	return new MyString(getName() + " says : \"" + s + " (" + i + ")\"");
	}

	public MyString write (MyString s, Object o) {
	return new MyString (getName() + " says : \"" + s + " (" + o + ")\"");
	}

    public Obj duplicate () {
	Obj result = null;
	Object[] params = new Object[1];
	params[0] = getName();	
	System.out.println("   creating the clone " + getName());
	try {
	    result = (Obj) ProActive.newActive("test.grouptest.Obj",params);
	}
	catch (ActiveObjectCreationException e) { System.err.println("Obj : Unable to create the clone");}
	catch (NodeException e)                 { System.err.println("Obj : Unable to reach the default node");}
	return result;
    }
    
}
