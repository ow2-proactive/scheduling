importPackage(java.lang);
importPackage(java.io);
importPackage(java.util);

if(System.getProperty("os.name").contains("Windows"))
{
	//windows verification
        for(i=0; i<args.length; i++) {
		myRegex=new RegExp("^[a-zA-Z]:");
		if(myRegex.test(args[i])) {
		command=args[i];
		break;
                }//if
        }//for
}
else
{
	//unix Command to chose
	print("unix node \n");
	for(i=0; i<args.length; i++) {
		print("testing : "+args[i]+"\n");
		commandToken= new StringTokenizer(args[i]).nextToken();
		if( (commandToken.startsWith("/") ||commandToken.startsWith("./") || commandToken.startsWith("../"))) {
			cmds= new Array;
                        cmds[0]="/bin/sh";
                        cmds[1]="-c";
			cmds[2]="ldd -v "+commandToken+" | egrep 'not found|not a dynamic executable'";
                        command_result=java.lang.Runtime.getRuntime().exec(cmds).waitFor();
                        if(command_result != 0) {
                                print("command selected "+ args[i] +"\n");
                                command=""+args[i];
                                break;
                        }//if
		}//if
	}//for
}//else
