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
var SCHEDULER_PORT = 64739 // port used by the Scheduler in case of PNP
var JETTY_PORT = 8080  // port used by the Web Server

var MAIN_PROTOCOL
var ADDITIONAL_PROTOCOL
var PORT_PROTOCOL

var SCHEDULER_URL

// Please always use this variale instead of os-dependant separator
var fs = File.separator;

// The name of the current script used for checking current location
var SCRIPT_NAME = 'start-server.js'

// Get current directory (must be SCHEDULER_HOME/bin)
var currDir = new File(getCheckedCurrDir())
var homeDir = new File(currDir.getParent())
var logsDir = new File(homeDir, 'logs')
var configDir = new File(homeDir, 'config')
var distDir = new File(homeDir, 'dist')
var paConfigFile = new File(configDir, 'proactive'+fs+'ProActiveConfiguration.xml')

// Load all jars from dist/lib
loadClasspath();

// OS switch and independent absolute path to Java executable
var javaExe = System.getProperty('java.home')+fs+'bin'+fs+(com.sun.jna.Platform.isWindows() ? 'java.exe' : 'java')

// Logs locations
var routerOutputFile = new File(logsDir,'Router-stdout.log')
var schedulerOutputFile = new File(logsDir, 'Scheduler-stdout.log')

// Processes
var routerProcess, schedulerProcess

startEverything()

function startEverything() {
	println('---------------------------------')
	println('    Starting server processes    ')
	println('---------------------------------')

	println('\nSetting up config and checking ports ...')
    setupConfigAndCheckPorts()
    setupURLs()	

    println('\nDumping configuration into ' + paConfigFile)
    dumpProActiveConfiguration(paConfigFile)

	// Add shutdownhook to terminate all processes if the current process is killed
	Runtime.getRuntime().addShutdownHook(new Thread(function () {
		if (schedulerProcess != null) schedulerProcess.destroy() 
		if (routerProcess != null) routerProcess.destroy()
	}))

	var executor = Executors.newFixedThreadPool(3)
	var service = new ExecutorCompletionService(executor)

	if (!logsDir.exists()) {
		logsDir.mkdir()
	}

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
                'SCHEDULER':SCHEDULER_PORT,
                'JETTY':JETTY_PORT
            }
            break
        case CONFIGS.PNP:
            MAIN_PROTOCOL = 'pnp'
            ADDITIONAL_PROTOCOL = null
            PORT_PROTOCOL = 'pnp'
            CHECK_PORTS = {
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
                'SCHEDULER':SCHEDULER_PORT,
                'JETTY':JETTY_PORT
            }
    }
    checkPorts(CHECK_PORTS)
}

function setupURLs() {
    if (config == CONFIGS.PAMR) {
        SCHEDULER_URL = 'pamr://0'
    } else {
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
	cmd.push(configDir+fs+'router'+fs+'router.ini')
	cmd.push('-v')
    cmd.push('-i', '0.0.0.0')   // ip to bind
    cmd.push('-p', ROUTER_PORT) // port to listen
	cmd.push('-t', '180000')    // heartbeat timeout
	cmd.push('-e', '86400000')  // disconnected clients timeout
	var proc = execCmdAsync(cmd, homeDir, routerOutputFile, 'router listening on')
	println('PAMR Router stdout/stderr redirected into ' + routerOutputFile)
	return proc
}

function startScheduler() {
	var cmd = initCmd()
    if (PORT_PROTOCOL != null) {
	    cmd.push('-Dproactive.'+PORT_PROTOCOL+'.port='+SCHEDULER_PORT)
    }
	cmd.push('-Dlog4j.configuration=file:'+configDir+fs+'log4j' + fs+'scheduler-log4j-server')
    if (config == CONFIGS.PNP_PAMR || config == CONFIGS.PAMR) {
	    cmd.push('-Dproactive.pamr.agent.id=0')
	    cmd.push('-Dproactive.pamr.agent.magic_cookie=scheduler')
    }
    cmd.push('org.ow2.proactive.scheduler.util.SchedulerStarter')

	var proc = execCmdAsync(cmd, homeDir, schedulerOutputFile, 'The scheduler created on')
	println('Scheduler stdout/stderr redirected into ' + schedulerOutputFile)
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

function dumpProActiveConfiguration(/*File*/ targetFile) {
    var pconf = new PrintWriter(targetFile)
    pconf.println('<?xml version=\'1.0\' encoding=\'UTF-8\'?>')
    pconf.println('<ProActiveUserProperties>')
    pconf.println(' <properties>')
    pconf.println('     <prop key=\'proactive.communication.protocol\' value=\''+MAIN_PROTOCOL+'\'/>')
    // normally we don't put additional protocols in standard proactive configuration
    if (config == CONFIGS.PNP_PAMR || config == CONFIGS.PAMR) {
        pconf.println('     <prop key=\'proactive.pamr.router.address\' value=\''+java.net.InetAddress.getLocalHost().getHostName()+'\'/>')
        pconf.println('     <prop key=\'proactive.pamr.router.port\' value=\''+ROUTER_PORT+'\'/>')
    }

    // prevent proactive from binding on loopback adresses
    pconf.println('     <prop key=\'proactive.net.nolocal\' value=\'true\'/>')

    // the following properties are often used, uncomment them if you need :
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

function execCmdAsync(cmdarray, wdir, outputFile, stringToWait) {
	// The classpath should be built from addons/* and dist/lib/*
	var env = Collections.singletonMap('CLASSPATH', fillClasspath(['*']))

	// Force to string an all elements of the cmdarray, start the process and redirect output to a file
	var cmd = new ArrayList(cmdarray.length)
	for (x in cmdarray) {
		cmd.add(cmdarray[x].toString())
	}
	var pb = new ProcessBuilder(cmd)
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

// Add all jars from dist/lib to current classpath
function loadClasspath() {
	if (System.getProperty('un') == null) {
		System.setProperty('un','un')
	} else {
		return;
	}
	
	var urlsList = new ArrayList()
	var jars = new File(distDir, 'lib').listFiles()
	for (x in jars) {
		urlsList.add(jars[x].toURL())
	}
	var urls = urlsList.toArray(java.lang.reflect.Array.newInstance(java.net.URL, urlsList.size()))
	var currentThreadClassLoader = Thread.currentThread().getContextClassLoader()
	var urlClassLoader = new URLClassLoader(urls, currentThreadClassLoader)
	Thread.currentThread().setContextClassLoader(urlClassLoader)
	var seManager = new javax.script.ScriptEngineManager(urlClassLoader);
    var engine = seManager.getEngineByName('javascript');
	engine.eval(new java.io.FileReader(new File(currDir,SCRIPT_NAME)));	
}
