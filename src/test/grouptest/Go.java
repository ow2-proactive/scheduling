package test.grouptest;


import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.NodeException;


public class Go {

    public static void main (String args[]) {


	Obj group = null;

	Object [] params = new Object[1];


	try {
	    group = (Obj) ProActiveGroup.newActiveGroup("test.grouptest.Obj");
	}
	catch (Exception e) {System.err.println("Go : Unable to create the typed group");}
	
	Group groupForm = ProActiveGroup.getGroup(group);

//	printMessageAndWait("Active objects creation");

	try {
	    params[0] = new MyString ("Laurent");
	    groupForm.add(ProActive.newActive("test.grouptest.Obj",params));//,"//scurra:4545/node1"));
	    params[0] = new MyString ("Arnaud");
	    groupForm.add(ProActive.newActive("test.grouptest.Obj",params));//,"//scurra:4545/node1"));
	    params[0] = new MyString ("Fabrice");
	    groupForm.add(ProActive.newActive("test.grouptest.Obj",params));//,"//scurra:4646/node2"));
	    params[0] = new MyString ("Julien");
	    groupForm.add(ProActive.newActive("test.grouptest.Obj",params));//,"//scurra:4646/node2"));
	    //groupForm.add(new Obj (new MyString("Zobi")));
	}
	catch (ActiveObjectCreationException e) { System.err.println("Go : Unable to create an active object");}
	catch (NodeException e)                 { System.err.println("Go : Unable to reach the default node(s)");}
	


//	System.out.println(((StubObject)ProActiveGroup.get(group,1)).getProxy().getClass());

	
//	printMessageAndWait("Method");
//	((org.objectweb.proactive.core.group.ProxyForGroup)groupForm).testClass();

//	Obj theNewOne = new Obj(new MyString("The New"));
	
//	groupForm.add(theNewOne);
	
//	((org.objectweb.proactive.core.group.ProxyForGroup)groupForm).testClass();


	group.sayYourName();
//	MyString msg = group.getName();
//	msg.display();

//	printMessageAndWait("duplicate");

	MyString groupString = null;
	try {
	    groupString = (MyString) ProActiveGroup.newActiveGroup("test.grouptest.MyString");
	}
	catch (Exception e) {System.err.println("Go : Unable to create the typed group");}
	
	Group groupStringForm = ProActiveGroup.getGroup(groupString);

	groupStringForm.add(new MyString("I'm the best."));
	groupStringForm.add(new MyString("I want a Linux."));
	groupStringForm.add(new MyString("Have you statistics ?"));
	groupStringForm.add(new MyString("My MOP is beautiful."));

//	((org.objectweb.proactive.core.group.ProxyForGroup)groupStringForm).testClass();
	groupString.display();

	ProActiveGroup.setScatterGroup(groupString);
	group.say(groupString);
//	ProActiveGroup.unsetScatterGroup(group);
	MyString res = group.write(groupString, new MyString("yeah !!!"));

	System.out.println("TEST DE VERITE :");

	res.display();
	Obj group2 = group.duplicate();
	group2.sayYourName();

//	Group group2Form = ProActiveGroup.getGroup(group2);
 //     	((org.objectweb.proactive.core.group.ProxyForGroup)group2Form).testClass();







/*
	String[] nodeList = new String[2];
	nodeList[0] = "//scurra:4545/node1";
	nodeList[1] = "//scurra:4646/node2";

	Object [][] params = new Object[4][1];
	params[0][0] = new MyString("Laurent");
	params[1][0] = new MyString("Arnaud");
	params[2][0] = new MyString("Fabrice");
	params[3][0] = new MyString("Julien");

	Obj group = null;
	
	try {
		group = (Obj) ProActiveGroup.newActiveGroupTHREAD("test.grouptest.Obj", params, nodeList); }
	catch (Exception e) { e.printStackTrace(); }

	group.sayYourName();
	

	MyString groupString = null;
	try {
	    groupString = (MyString) ProActiveGroup.newActiveGroup("test.grouptest.MyString");
	}
	catch (Exception e) {System.err.println("Go : Unable to create the typed group");}
	
	Group groupStringForm = ProActiveGroup.getGroup(groupString);
	groupStringForm.add(new MyString("I'm the best."));
	groupStringForm.add(new MyString("I want a Linux."));
	groupStringForm.add(new MyString("Have you statistics ?"));
	groupStringForm.add(new MyString("My MOP is beautiful."));

	groupString.display();
*/
	System.out.println("\n  Yeaahh, That's all Folks, Everything is good in group communication !!! \n");
    }









    private static void printMessageAndWait(String msg) {
	java.io.BufferedReader d = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
	System.out.print(msg);
	System.out.println("   --> Press <return> to continue");
	try { 
	    d.readLine(); 
	} catch (Exception e) {}
    } 
    
    
}
