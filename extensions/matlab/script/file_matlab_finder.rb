include Java

import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabEngineConfig
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabFinder

import java.lang.System

module JavaIO
    include_package "java.io"
end

$selected = false
begin

  if defined?($args)

    begin
      nodeName = MatlabEngineConfig.getNodeName()
    rescue
      nodeName = "DummyNode"
    end

    tmpPath = System.getProperty("java.io.tmpdir");

    logFileJava = JavaIO::File.new(tmpPath, "CheckMatlab"+nodeName+".log");
    #logFile = File.new(logFileJava.toString(), "a");
    orig_stdout = $stdout
    orig_stderr = $stderr
    fos = JavaIO::FileOutputStream.new(logFileJava);
    logout = JavaIO::PrintStream.new(fos);
    orig_jstdout = System.out
    orig_jstderr = System.err;
    $stdout.reopen(logFileJava.toString(), "a")
    $stdout.sync=true
    $stderr.reopen $stdout
    System.setOut(logout);
    System.setErr(logout);
    
    cpt = 0
    versionPref = nil
    versionMin = nil
    versionMax = nil
    versionRej = nil
    while cpt < $args.size
      case $args[cpt]
        when "versionPref"
          versionPref  = $args[cpt+1]
        when "versionMin"
          versionMin  = $args[cpt+1]
        when "versionMax"
          versionMax  = $args[cpt+1]
        when "versionRej"          
            versionRej = $args[cpt+1]

      end
      cpt += 2
    end

    begin
      cf = MatlabFinder.getInstance().findMatSci(versionPref, versionRej, versionMin, versionMax);
    rescue JavaIO::FileNotFoundException => fnfe
      puts fnfe.message
    rescue Exception => e
      puts e.message + "\n" + e.backtrace.join("\n")
    end    

    if (cf == nil)      
      $selected = false
    else
      puts cf
      MatlabEngineConfig.setCurrentConfiguration(cf)
      $selected = true
    end
  end
rescue Exception => e
  puts e.message + "\n" + e.backtrace.join("\n")
  raise java.lang.RuntimeException.new(e.message + "\n" + e.backtrace.join("\n"))
ensure
  $stdout = orig_stdout
  $stderr = orig_stderr
  System.setOut(orig_jstdout);
  System.setErr(orig_jstderr);
  logout.close();
  fos.close();
end
