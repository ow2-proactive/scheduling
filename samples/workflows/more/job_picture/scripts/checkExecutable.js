selected=false;
print("checking executable \n");
if(java.lang.System.getProperty("os.name").contains("Windows"))
{
	//windows verification
        for(i=0; i<args.length; i++) {
                if(java.io.File(args[i]).exists()) {
			//verif de la commande

                }//if
        }//for
}
else
{
	//unix verification
	for(i=0; i<args.length; i++) {
	print("checking : "+ args[i] +"\n");
		if( (args[i].startsWith("/") ||args[i].startsWith("./") || args[i].startsWith("../")) &&  (new java.io.File(args[i])).exists()) {
		        cmds= new Array;
			cmds[0]="/bin/sh";
		        cmds[1]="-c";
			cmds[2]="ldd -v "+args[i]+" | egrep 'not found|not a dynamic executable'";
			command_result=java.lang.Runtime.getRuntime().exec(cmds).waitFor();
		        if(command_result != 0) {
				print("node good for "+ args[i] +"\n");
		                selected=true;
				break;
			}
		}//if
	}//for
}//else
