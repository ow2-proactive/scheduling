importPackage(java.lang);
importPackage(java.io);
importPackage(java.util);
importPackage(java.exception);
importClass(org.ow2.proactive.scripting.helper.selection.SelectionUtils);



//try {
   // Get the arguments of the script:
   // args[0] : the name of the java system property
   // args[1] : the expected value
   var propertyName = args[0];
   var expectedValue = args[1];

    /* Check if java property 'a.jvm.property' value is 'toto' */
   if (SelectionUtils.checkJavaProperty(args[0], args[1]) ) 
   {
	    selected = true;
	    println(args[0] + " = " + args[1] + " ==> selected");
   }
   else 
   {
	    selected = false;
	    println(args[0] + " <> " + args[1] + " ==> not selected");
   }

/*
}
catch(e) 
{

    println("An exception occured during the selection script");
    println(e);

    selected = false;
    }*/