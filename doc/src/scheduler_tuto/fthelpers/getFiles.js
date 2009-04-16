importPackage(java.lang);
importPackage(java.io);
importPackage(java.util);
importPackage(org.ow2.proactive.scripting.helper.filetransfer);
importPackage(org.ow2.proactive.scripting.helper.filetransfer.driver);
importPackage(org.ow2.proactive.scripting.helper.filetransfer.initializer);

//This script example ilustrates the use of org.objectweb.proactive.scheduler.helper API to get files from the remote host to the local node

//set this to true if yo want to have your logs in a file on the remote machine
var logToFile = false;
//set this to true to enable debug mode
var mode_debug= false;

var task_id=System.getProperty("pas.task.id");
var logsFile = "task_"+task_id+".log";


log ("Start Get Files Script");
//only if logToFile is true:
log("logs file at "+System.getProperty("java.io.tmpdir")+File.separator+logsFile);





  //-------------- Script ARGS --------------------------
     if (args.length<5)
      {
        log("Script usage: host username password working_dir_on_node file1 [file2] ... [filen]");
        log("Not enough parameters. Script cannot be executed");
        throw new Exception ("Not enough parameters.");
      }


		//host where the file is to be copied from
		var host = args[0];
		debug("remote host="+host);

		//identification to the remote host
		var username=args[1];
		debug("user="+username);
		var password=args[2];
		debug("password="+password);
		//for the scp implementation- the driver will first try to connect through ssh keys
		//and will use the password only if the connection fails. 
		
		var working_dir_on_node = args[3];
		debug("destination folder on the compute node: "+working_dir_on_node);

		debug("Files to copy: ");
		var files = new LinkedList();

		var k=0;
		for (k=4;k<args.length;k++)
			{
			files.add(args[k]);
			debug(args[k]);
			}


		//how long will it take?
		var t1=System.currentTimeMillis();


		//-------- Create Local Folder if it doesn't exist
		var tFolder = new File(working_dir_on_node);
		if (!tFolder.isDirectory())
		if (tFolder.mkdirs())
		{
			debug ("Folder have been created: "+tFolder);
		}
		else
		{
		  debug("Could not create folder " + tFolder );
		}



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
	//		var driver = new SFTP_VFS_Driver();
	//		var ftInit= new FileTransfertInitializerSCP(host, username, password,driver.getClass());


    // ----------- OR ----------------
	//---- (FTP Protocol) Use this code for in order to use FTP_VFS_Driver to copy the files ----


	//		var driver = new FTP_VFS_Driver();
	//		var ftInit= new FileTransfertInitializerFTP(host, username, password,driver.getClass());


		// ---------- Create  session for the file transfer ----------------
		var session = new FileTransfertSession(ftInit);

		// -------------------- Copy the files --------------------------
		session.getFiles(files, working_dir_on_node);


		var t2=System.currentTimeMillis();
		log("Get files script ended. Copying files took "+(t2-t1)/1000+" seconds = "+(t2-t1)/60000)+" minutes";



function log(msg)
{
		msg="(getFiles.js) "+msg;
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

	