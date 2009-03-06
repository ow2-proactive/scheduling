importPackage(java.lang);
importPackage(java.io);
importPackage(java.util);
importPackage(org.ow2.proactive.scripting.helper.filetransfer);
importPackage(org.ow2.proactive.scripting.helper.filetransfer.driver);
importPackage(org.ow2.proactive.scripting.helper.filetransfer.initializer);

//This script example will use SCP_TRILEAD_Driver to putt files from the local node to the remote host 

//set this to true if yo want to have your logs in a file on the remote machine
var logToFile = true;
//set this to true to ienable debug mode
var mode_debug=true;
var task_id=System.getProperty("pas.task.id");
var logsFile = "task_"+task_id+".log";

     log ("Start Put Files Script");
	 //only if logToFile is true:
	 
	 log("logs file at "+System.getProperty("java.io.tmpdir")+File.separator+logsFile);


  //-------------- Script ARGS --------------------------  



     if (args.length<5)
      {
        log("Script usage: host username password foldeR_on_remote_host file1 [file2] ... [filen]");
        log("Not enough parameters. Script cannot be executed");
        throw new Exception ("Not enough parameters.");
      } 	 	      
      





		//host where the file is to be copied from
		var host = args[0];
		debug("host="+host);
		
		//identification to the remote host
		var username=args[1];
		debug("user="+username);
		var password=args[2];
		debug("password="+password);
		//folder to copy the files to
		var to_folder_on_host = args[3];
		debug("remote folder="+to_folder_on_host);

		debug("Files to copy: ");
		var files = new LinkedList();
		
		var k=0;
		for (k=4;k<args.length;k++)
			{
			files.add(args[k]);
			log(args[k]);
			}

	 		
		//how long will it take? 
		var t1=System.currentTimeMillis();

   //--------------- Define Driver to be used for file transfer (or use default one) ------------
	

//----(SCP Protocol) Use this code for in order to use SCP_Trilead_Driver to copy the files ---- 
	//(By default, the initializaer will use SCP_Trilead_Driver to init the connection)
	var ftInit= new FileTransfertInitializerSCP(host, username, password);
	
    // ----------- OR ----------------



	//----(SFTP Protocol) Use this code for in order to use SFTP_Trilead_Driver to copy the files ---- 
	//	var driver = new SFTP_Trilead_Driver();
	//	var ftInit= new FileTransfertInitializerSCP(host, username, password,driver.getClass());
		
    // ----------- OR ----------------
	//---- (SFTP Protocol) Use this code for in order to use SFTP_VFS_Driver to copy the files ---- 
			var driver = new SFTP_VFS_Driver();
			var ftInit= new FileTransfertInitializerSCP(host, username, password,driver.getClass());


    // ----------- OR ----------------
	//---- (FTP Protocol) Use this code for in order to use FTP_VFS_Driver to copy the files ---- 
		

	//		var driver = new FTP_VFS_Driver();
	//		var ftInit= new FileTransfertInitializerFTP(host, username, password,driver.getClass());


// ---------- Create  session for the file transfer ----------------		
		var session = new FileTransfertSession(ftInit);

// -------------------- Copy the files -------------------------- 
		session.putFiles(files, to_folder_on_host);
		

	var t2=System.currentTimeMillis();
	log("Put script ended. Copying files to remote host took "+(t2-t1)/1000+" seconds = "+(t2-t1)/60000)+" minutes";




function log(msg)
{
	msg="(putScript) "+msg;
		 if (logToFile)
			  {
				   ScriptLoggerHelper.logToFile(logsFile,"\n"+msg+"\n");
			  }
	
		 println(msg);
}
	

	
function debug(msg)
{
       if (mode_debug)
       {
       	log ("DEBUG: "+msg);
       }
}	

	