import org.ow2.proactive.scheduler.util.GroovySchedulerStarter

import java.util.concurrent.CountDownLatch

javaExe = joinPath(System.getProperty('java.home'), 'bin', isWindows() ? 'java.exe' : 'java')
schedulerHome = GroovySchedulerStarter.getSchedulerDir()
classpathDistLib = joinPath(schedulerHome, 'dist', 'lib', '*')
classpathAddons = joinPath(schedulerHome, 'addons', '*')
classpath = joinClasspath(classpathDistLib, classpathAddons)



routerProcess = startRouterAndWaitStarted()

Process schedulerProcess = schedulerStartCommand().execute()
schedulerProcess.consumeProcessOutput(System.out, System.err)

addShutdownHook {
    schedulerProcess.destroy()
}

schedulerProcess.waitFor()
routerProcess.waitFor()



def schedulerStartCommand() {
    [javaExe, '-cp', classpath,
     'org.ow2.proactive.scheduler.util.SchedulerStarter', '--no-router', *args]
}

def routerStartCommand() {
    [javaExe, '-cp', classpath,
     'org.ow2.proactive.utils.PAMRRouterStarter', '-f', joinPath(schedulerHome, 'config', 'router', 'router.ini')]
}

def Process startRouterAndWaitStarted(){
    Process routerProcess = routerStartCommand().execute()
    addShutdownHook {
        routerProcess.destroy()
    }
    def routerStarted = new CountDownLatch(1)
    Thread.start {
        routerProcess.in.eachLine { line ->
            println line
            if (line.contains('router listening on')) {
                routerStarted.countDown()
            }
        }
    }
    routerStarted.await()
    return routerProcess
}

def isWindows() {
    System.properties['os.name'].toLowerCase().contains('windows')
}

def joinPath(String... list) {
    list.join(File.separator)
}

def joinClasspath(String... list) {
    list.join(File.pathSeparator)
}