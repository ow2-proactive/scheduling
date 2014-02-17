// To run, please use jrunscript (jdk tool)
// example: jrunscript start-server.js

importPackage(java.lang)
importPackage(java.io)
importPackage(java.util)
importPackage(java.util.concurrent)
importPackage(java.util.zip)

// This script support the following configurations :
// PNP_PAMR : main protocol : PNP , additional protocol : PAMR (default)
// PNP : PNP protocol only
// PAMR : PAMR protocol only
var CONFIGS = {
    PNP_PAMR : 1,
    PNP : 2,
    PAMR : 3
}

// Change this variable in order to switch configurations
var config = CONFIGS.PNP_PAMR

// Change these ports if required
var ROUTER_PORT = 64737 // port used by the PAMR router
var RM_PORT = 64738 // port used by the RM in case of PNP
var SCHEDULER_PORT = 64739 // port used by the Scheduler in case of PNP
var JETTY_PORT = 8080  // port used by the Web Server

var MAIN_PROTOCOL
var ADDITIONAL_PROTOCOL
var PORT_PROTOCOL

var RM_URL
var SCHEDULER_URL

// Please always use this variale instead of os-dependant separator
var fs = File.separator;

// The name of the current script used for checking current location
var SCRIPT_NAME = 'start-server.js'

// Get current directory (must be SCHEDULER_HOME/bin)
var currDir = new File(getCheckedCurrDir())
var homeDir = new File(currDir.getParent())
var logsDir = new File(homeDir, '.logs')
var configDir = new File(homeDir, 'config')
var distDir = new File(homeDir, 'dist')
var warsDir = new File(distDir, 'war')
var paConfigFile = new File(configDir, 'proactive'+fs+'ProActiveConfiguration.xml')

// OS switch and independent absolute path to Java executable
var isWindows = System.getProperty('os.name').startsWith('Windows')
var javaExe = System.getProperty('java.home') + fs + 'bin' + fs + (isWindows ? 'java.exe' : 'java')

// Logs locations
var routerOutputFile = new File(logsDir,'Router-stdout.log')
var rmOutputFile = new File(logsDir,'RM-stdout.log')
var schedulerOutputFile = new File(logsDir, 'Scheduler-stdout.log')
var jettyOutputFile = new File(logsDir, 'Jetty-stdout.log')

// Jars relative to lib
var scriptJars = ['jruby-1.7.4.jar', 'jython-2.5.4-rc1.jar', 'groovy-all-2.1.6.jar']
var vfsJars = ['commons-logging-1.1.1.jar','commons-httpclient-3.1.jar']
var coreJars = ['*']
var jettyJars = ['*']

// Processes
var routerProcess, rmProcess, schedulerProcess, jettyProcess

startEverything()

function startEverything() {
	println('---------------------------------')
	println('    Starting server processes    ')
	println('---------------------------------')

	if (!logsDir.exists()) {
		logsDir.mkdir()
	}

	println('\nSetting up config and checking ports ...')
    setupConfigAndCheckPorts()
    setupURLs()	

    println('\nDumping configuration into ' + paConfigFile)
    dumpProActiveConfiguration()

	// Add shutdownhook to terminate all processes if the current process is killed 
	var cleaner = new java.lang.Thread(function () {
		stopEverything()
	})
	java.lang.Runtime.getRuntime().addShutdownHook(cleaner)

	var executor = Executors.newFixedThreadPool(4)
	var service = new ExecutorCompletionService(executor)

    if (config == CONFIGS.PNP_PAMR || config == CONFIGS.PAMR) {
        println('\nRunning PAMR Router process ...')
        routerProcess = startRouter()

        if (routerProcess != null) {
            var routerWaiter = new Callable({
                call: function () {
                    var exitValue = routerProcess.waitFor()
                    println('!! Router HAS EXITED !! Please consult ' + routerOutputFile)
                    return exitValue
                }})
            service.submit(routerWaiter)
        }
    }

	println('\nRunning Resource Manager process ...')
	rmProcess = startRM()
	if (rmProcess != null) {
	    var rmWaiter = new Callable({ 
		   call: function () {
		      var exitValue = rmProcess.waitFor()
			  println('!! RM HAS EXITED !! Please consult ' + rmOutputFile)
		      return exitValue
	    }})
	    service.submit(rmWaiter)
	}

	println('\nRunning Scheduler process ...')
	schedulerProcess = startScheduler()
	if (schedulerProcess != null) {
		var schedulerWaiter = new Callable({ 
			call: function () {
				var exitValue = schedulerProcess.waitFor()
				println('!! Scheduler HAS EXITED !! Please consult ' + schedulerOutputFile)
				return exitValue
		}})
		service.submit(schedulerWaiter)
	}

	println('Running Jetty process ...')
	jettyProcess = startJetty()
	if (jettyProcess != null) {
		var jettyWaiter = new Callable({
			call: function () {
				var exitValue = jettyProcess.waitFor()
				println('!! JETTY HAS EXITED !! Please consult ' + jettyOutputFile)
				return exitValue
		}})
		service.submit(jettyWaiter)
	}

	var exitListener = new Callable({
    call: function () {
            try {
                var stream = new InputStreamReader(System['in'])
                var reader = new BufferedReader(stream)
                while (!(reader.readLine().equals('exit')));
            } catch (e) {
                println('Unable to get input due to ' + e)
            }
            return 'exit by user'
    }})
    service.submit(exitListener)

	// For each process a waiter thread is used
	println('Preparing to wait for processes to exit ...')
	// no more tasks are going to be submitted, this will let the executor clean up its threads
	executor.shutdown()

	if (!executor.isTerminated()) {
	    println('Hit CTRL+C or enter \'exit\' to terminate all server processes and exit')
		var finishedFuture = service.take()
		println('Finishing process returned ' + finishedFuture.get())
		stopEverything()
		// Exit current process ... if under agent it will restart it
		System.exit(-1)
	}
}

// Configure protocol and heck that given ports are free
function setupConfigAndCheckPorts() {
    switch (config) {
        case CONFIGS.PNP_PAMR:
            MAIN_PROTOCOL = 'pnp'
            ADDITIONAL_PROTOCOL = 'pamr'
            PORT_PROTOCOL = 'pnp'
            CHECK_PORTS = {
                'ROUTER':ROUTER_PORT,
                'RM':RM_PORT,
                'SCHEDULER':SCHEDULER_PORT,
                'JETTY':JETTY_PORT
            }
            break
        case CONFIGS.PNP:
            MAIN_PROTOCOL = 'pnp'
            ADDITIONAL_PROTOCOL = null
            PORT_PROTOCOL = 'pnp'
            CHECK_PORTS = {
                'RM':RM_PORT,
                'SCHEDULER':SCHEDULER_PORT,
                'JETTY':JETTY_PORT
            }
            break
        case CONFIGS.PAMR:
            MAIN_PROTOCOL = 'pamr'
            ADDITIONAL_PROTOCOL = null
            PORT_PROTOCOL = null
            CHECK_PORTS = {
                'ROUTER':ROUTER_PORT,
                'JETTY':JETTY_PORT
            }
            break
        default:
            MAIN_PROTOCOL = 'pnp'
            ADDITIONAL_PROTOCOL = 'pamr'
            PORT_PROTOCOL = 'pnp'
            CHECK_PORTS = {
                'ROUTER':ROUTER_PORT,
                'RM':RM_PORT,
                'SCHEDULER':SCHEDULER_PORT,
                'JETTY':JETTY_PORT
            }
    }
    checkPorts(CHECK_PORTS)
}

function setupURLs() {
    if (config == CONFIGS.PAMR) {
        RM_URL = 'pamr://0'
        SCHEDULER_URL = 'pamr://1'
    } else {
        RM_URL = PORT_PROTOCOL+'://localhost:'+RM_PORT
        SCHEDULER_URL = PORT_PROTOCOL+'://localhost:'+SCHEDULER_PORT
    }
}

function startRouter() {
	var cmd = initCmd()
	cmd.push('-server')
	cmd.push('-XX:+UseParNewGC')
	cmd.push('-XX:+UseConcMarkSweepGC')
	cmd.push('-XX:CMSInitiatingOccupancyFraction=50')
	cmd.push('-XX:NewRatio=2')
	cmd.push('-Xms512m')
	cmd.push('-Xmx512m')
	cmd.push('-Dlog4j.configuration=file:'+configDir+fs+'log4j' + fs+'log4j-router')
	cmd.push('org.objectweb.proactive.extensions.pamr.router.Main')
	cmd.push('--configFile')
	cmd.push(configDir+fs+'router' + fs+'router.ini')
	cmd.push('-v')
    cmd.push('-p')
    cmd.push(ROUTER_PORT)
	cmd.push('-t')
	cmd.push('180000')
	cmd.push('-e')
	cmd.push('86400000')
	cmd.push('-i')
	cmd.push('0.0.0.0')	
	var env = Collections.singletonMap('CLASSPATH', fillClasspath(scriptJars, vfsJars, coreJars))
	var proc = execCmdAsync(cmd, homeDir, routerOutputFile, 'router listening on', env)
	println('PAMR Router stdout/stderr redirected into ' + routerOutputFile)
	return proc
}

function startRM() {
	var cmd = initCmd()
    if (PORT_PROTOCOL != null) {
        cmd.push('-Dproactive.'+PORT_PROTOCOL+'.port='+RM_PORT)
    }
	cmd.push('-Dlog4j.configuration=file:'+configDir+fs+'log4j' + fs+'rm-log4j-server')
    if (config == CONFIGS.PNP_PAMR || config == CONFIGS.PAMR) {
        cmd.push('-Dproactive.pamr.agent.id=0')
        cmd.push('-Dproactive.pamr.agent.magic_cookie=rm')
    }
	cmd.push('org.ow2.proactive.resourcemanager.utils.RMStarter')
	cmd.push('-ln') // with default 4 local nodes
	var env = Collections.singletonMap('CLASSPATH', fillClasspath(scriptJars, vfsJars, coreJars))
	var proc = execCmdAsync(cmd, homeDir, rmOutputFile, 'created on', env)
	println('Resource Manager stdout/stderr redirected into ' + rmOutputFile)
	return proc
}

function startScheduler() {
	var cmd = initCmd()
    if (PORT_PROTOCOL != null) {
	    cmd.push('-Dproactive.'+PORT_PROTOCOL+'.port='+SCHEDULER_PORT)
    }
	cmd.push('-Dlog4j.configuration=file:'+configDir+fs+'log4j' + fs+'scheduler-log4j-server')
    if (config == CONFIGS.PNP_PAMR || config == CONFIGS.PAMR) {
	    cmd.push('-Dproactive.pamr.agent.id=1')
	    cmd.push('-Dproactive.pamr.agent.magic_cookie=scheduler')
    }
    cmd.push('org.ow2.proactive.scheduler.util.SchedulerStarter')
    cmd.push('-u', RM_URL) // always on localhost

	var env = Collections.singletonMap('CLASSPATH', fillClasspath(scriptJars, vfsJars, coreJars))	
	var proc = execCmdAsync(cmd, homeDir, schedulerOutputFile, 'created on', env)
	println('Scheduler stdout/stderr redirected into ' + schedulerOutputFile)
	return proc
}

function injectProperties(propsFile, properties) {
    println('Injecting the Resource Manager and Scheduler urls into  ' + propsFile)
    var props = new Properties()
    var inputStream = new FileInputStream(propsFile)
    props.load(inputStream)
    inputStream.close()
    for (var prop in properties) {
        props.setProperty(prop, properties[prop])
    }
    var outputStream = new FileOutputStream(propsFile)
    props.store(outputStream, '')
    outputStream.close()
}

function extractWar(warsDir, path, warName) {
    var extractDir = new File(warsDir, path)
    println('Checking for ' + extractDir)
    if (!extractDir.exists()) {
        var restWar = new File(warsDir, warName)
        if (!restWar.exists()) {
            println('Unable to locate ' + restWar)
            return null
        }
        extractFolder(restWar, extractDir)
    }
    return extractDir
}

function startJetty() {
	if (!warsDir.exists()) {
	   println('Unable to locate ' + warsDir + ' directory, jetty will not be started')
	   return null
	}

    var restDir = extractWar(warsDir, 'rest', 'rest.war')
    var rmDir = extractWar(warsDir, 'rm', 'rm.war')
    var schedulerDir = extractWar(warsDir, 'scheduler', 'scheduler.war')

	java.nio.file.Files.copy(paConfigFile.toPath(), new File(restDir, 'WEB-INF'+fs+'ProActiveConfiguration.xml').toPath());    

    var restProperties = { 'rm.url': RM_URL, 'scheduler.url': SCHEDULER_URL}
    injectProperties(new File(restDir, 'WEB-INF'+fs+'portal.properties'), restProperties)

    var rmProperties = { 'rm.rest.url': 'http://localhost:'+JETTY_PORT+'/rest/rest'}
    injectProperties(new File(rmDir, 'rm.conf'), rmProperties)

    var schedulerProperties = { 'sched.rest.url': 'http://localhost:'+JETTY_PORT+'/rest/rest' }
    injectProperties(new File(schedulerDir, 'scheduler.conf'), schedulerProperties)


    var cmd = [ javaExe ]
	cmd.push('-Djava.security.manager')
	cmd.push('-Djava.security.policy=file:'+configDir+fs+'security.java.policy-client')
	cmd.push('org.ow2.proactive.utils.JettyLauncher')
	cmd.push('-p', JETTY_PORT)
	cmd.push(restDir, rmDir, schedulerDir)
	var env = Collections.singletonMap('CLASSPATH', fillClasspath(jettyJars))
	var proc = execCmdAsync(cmd, homeDir, jettyOutputFile, null, env)
	println('Jetty stdout/stderr redirected into ' + jettyOutputFile)

	println('Waiting for jetty to start ...')
	while (isTcpPortAvailable(JETTY_PORT)) {
		java.lang.Thread.sleep(1000)
	}

	var restHttpUrl = 'http://localhost:'+JETTY_PORT+'/rest'
	var rmHttpUrl = 'http://localhost:'+JETTY_PORT+'/rm'
	var schedulerHttpUrl = 'http://localhost:'+JETTY_PORT+'/scheduler'
	
	println('Rest Server webapp deployed at      ' + restHttpUrl)
	println('Resource Manager webapp deployed at ' + rmHttpUrl)
	println('Scheduler webapp deployed at        ' + schedulerHttpUrl)
	println('')
	println('Opening browser ...')
	println('Please use demo/demo as login/password to connect')
    try {
       java.awt.Desktop.getDesktop().browse(java.net.URI.create(restHttpUrl))
	   java.awt.Desktop.getDesktop().browse(java.net.URI.create(rmHttpUrl))
	   java.awt.Desktop.getDesktop().browse(java.net.URI.create(schedulerHttpUrl))
    } catch (e) {
       println(e)
    }
	return proc
}

function initCmd() {
	var cmd = [ javaExe ]
	cmd.push('-Dproactive.home='+homeDir)
	cmd.push('-Dpa.rm.home='+homeDir)
	cmd.push('-Dpa.scheduler.home='+homeDir)
	cmd.push('-Djava.security.manager')
	cmd.push('-Djava.security.policy=file:'+configDir+fs+'security.java.policy-server')
	cmd.push('-Dproactive.configuration='+configDir+fs+'proactive'+fs+'ProActiveConfiguration.xml')
	cmd.push('-Dderby.stream.error.file='+logsDir+fs+'derby.log')
	cmd.push('-Xms128m','-Xmx1048m')
	cmd.push('-Dproactive.communication.protocol='+MAIN_PROTOCOL)
    if (ADDITIONAL_PROTOCOL != null) {
        cmd.push('-Dproactive.communication.additional_protocols='+ADDITIONAL_PROTOCOL)
    }
	return cmd
}

function dumpProActiveConfiguration() {
    var pconf = new PrintWriter(paConfigFile)
    pconf.println('<?xml version=\'1.0\' encoding=\'UTF-8\'?>')
    pconf.println('<ProActiveUserProperties>')
    pconf.println(' <properties>')
    pconf.println('     <prop key=\'proactive.communication.protocol\' value=\''+MAIN_PROTOCOL+'\'/>')
    // normally we don't put additional protocols in standard proactive configuration
    if (config == CONFIGS.PNP_PAMR || config == CONFIGS.PAMR) {
        pconf.println('     <prop key=\'proactive.pamr.router.address\' value=\''+java.net.InetAddress.getLocalHost().getHostName()+'\'/>')
        pconf.println('     <prop key=\'proactive.pamr.router.port\' value=\''+ROUTER_PORT+'\'/>')
    }
    // the following properties are often used, uncomment them if you need :
    // pconf.println('     <prop key=\'proactive.net.nolocal\' value=\'true\'/>')
    // pconf.println('     <prop key=\'proactive.useIPaddress\' value=\'true\'/>')

    pconf.println('  </properties>')
    pconf.println('  <javaProperties>')
    pconf.println('  </javaProperties>')
    pconf.println('</ProActiveUserProperties>')
    pconf.flush()
    pconf.close()
}

function fillClasspath() {
	var libDir = new File(distDir, 'lib')
	var allJars = []
	for (var i = 0; i < arguments.length; i++){
		var array = arguments[i]
		for (x in array) {
			allJars.push(new File(libDir, array[x]))
		}
	}
	// Add jars in addons directory	
	new File(homeDir, 'addons').listFiles(new FileFilter({
		accept: function (file) {
			if (file.getName().endsWith('.jar')) {
				allJars.push(file)
			}
			return false
	}}))
	
	var classpath = new StringBuilder('.')	
	for (x in allJars) {
		classpath.append(File.pathSeparator).append(allJars[x])
	}	
	return classpath.toString()
}

function execCmdAsync(cmdarray, wdir, outputFile, stringToWait, env) {
	// Convert command from javascript to java array, start the process and redirect output to a file
	var jarray = toJavaArray(cmdarray)
	//println('---> command ' + Arrays.toString(jarray))
	var pb = new ProcessBuilder(jarray)
	pb.redirectErrorStream(true)
	pb.directory(wdir)
	pb.environment().putAll(env)

	var fos = new FileOutputStream(outputFile)
	var pw = new PrintWriter(fos)

	// Start the process and wait until it prints successfully
	var process = pb.start()
	var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
	var line = null
	if (stringToWait != null) {
		while ((line = reader.readLine()) != null) {
			println('> ' +line)
			pw.println(line)
			pw.flush()
			if (line.contains(stringToWait)) {
				break
			}
		}
	}
	// Create the stream gobbler 
	var obj = { run: function() {
		try {	        
	        while ( (line = reader.readLine()) != null) {
	            if (pw != null) {
	                pw.println(line)
	                pw.flush()
	            }
	        }
	    } catch (e) {
	        println('Unable to gobble the stream: ' + e + 'e.javaException')
		}
	}}
	
	// Start the gobbler as in a separate thread
	var r = new Runnable(obj)
	var th = new Thread(r)
	th.setDaemon(true)	
	th.start()	
	return process
}

function stopEverything() {
	println('Stopping everything ...')
	if ( jettyProcess != null) 
		jettyProcess.destroy()	
	if ( schedulerProcess != null )
		schedulerProcess.destroy()	
	if ( rmProcess != null )
		rmProcess.destroy()
	if ( routerProcess != null )
		routerProcess.destroy()
}

function toJavaArray(cmdarray) {
	var jarray = java.lang.reflect.Array.newInstance(java.lang.String, cmdarray.length)
	for ( var i = 0; i < cmdarray.length; i++) {
		jarray[i] = cmdarray[i]
	}
	return jarray
}

function getCheckedCurrDir() {
	var currentDir = new File(System.getProperty('user.dir'))
	try {
		var errmsg = 'Please run this script from SCHEDULER_HOME'+fs+'bin'
		assertExists(currentDir + fs + SCRIPT_NAME, errmsg)
		return currentDir
	} catch (e) {
		println('Problem found: ' + e)
		System.exit(-1)
	}
}

function assertExists(file, err) { // throws IllegalStateException
	var f = new File(file)
	if (!f.exists()) {
		throw IllegalStateException('File ' + file + ' not found. ' + err)
	}
	return
}

function extractFolder(zipFile, destDirFile){ // throws ZipException, IOException
    var BUFFER = 2048
    var zip = new ZipFile(zipFile)
    // Extract the name
    var filenameWithExt = zipFile.getName()
    var filenameWithoutExtension = filenameWithExt.substring(0,filenameWithExt.length() - 4)
    var zipFileEntries = zip.entries()
    // Process each entry
    while (zipFileEntries.hasMoreElements()) {
        // grab a zip file entry
        var entry = zipFileEntries.nextElement()
        var currentEntry = entry.getName()
        
        // The destination create the parent directory structure if needed
        var destFile = new File(destDirFile, currentEntry)
        var destinationParent = destFile.getParentFile()
        destinationParent.mkdirs()

        if (!entry.isDirectory()) {
            var is = new BufferedInputStream(zip.getInputStream(entry))
            var currentByte
            // establish buffer for writing file
            var data = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, BUFFER)

            // write the current file to disk
            var fos = new FileOutputStream(destFile)
            var dest = new BufferedOutputStream(fos, BUFFER)

            // read and write until last byte is encountered
            while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, currentByte)
            }
            dest.flush()
            dest.close()
            is.close()
        }
    }
}

function isTcpPortAvailable(port) { // throws IOException
	var sock = null
	try {
		sock = new java.net.ServerSocket(port)
	} catch (ee) {
		return false
	} finally {
		if (sock!=null) {
			try {
			sock.close()
			} catch (e){} 
		}
	}
	return true
}

function checkPorts(portmap){
	for (var serv in portmap) {
		var avail = isTcpPortAvailable(portmap[serv])
		if (!avail) {
			println('TCP port ' + portmap[serv] + ' (used by ' + serv + ') is busy...')
			println('This program will exit...')
			System.exit(-1)
		}
	}
}
