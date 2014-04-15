
//try {
   // Get the args of the script:
   // args[0] : the name of the java system property
   // args[1] : the expected value
   var propertyName = args[0];
   var expectedValue = args[1];

    /* Check if java property 'a.jvm.property' value is 'toto' */
   if (org.ow2.proactive.scripting.helper.selection.SelectionUtils.checkJavaProperty(args[0], args[1]) )
   {
	    selected = true;
	    print(args[0] + " = " + args[1] + " ==> selected");
   }
   else 
   {
	    selected = false;
	    print(args[0] + " <> " + args[1] + " ==> not selected");
   }

/*
}
catch(e) 
{

    print("An exception occured during the selection script");
    print(e);

    selected = false;
    }*/