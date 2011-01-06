include Java

import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabEngineConfig
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabFinder

import java.lang.System

module JavaIO
  include_package "java.io"
end
import java.net.InetAddress
import java.util.Date
import java.lang.Runtime
import java.lang.ProcessBuilder

class ReserveMatlab

  require 'rbconfig'

  def initialize
    @toolboxmap = {
        # Standard Matlab
        "matlab" => %q!a=1!,
        # Simulink
        "simulink" => %q!a=a&&license('checkout','simulink')!,
        # Control System
        "control" => %q!a=a&&license('checkout','control_toolbox')!,
        # Fixed Point
        # The fixed point toolbox has a very weird licencing scheme, it takes no toolbox licence
        # if the logging mode is not activated, here the command simulates that a licence is being taken
        # If you want to use fixed point without the logging, replace the matlab command below with a dummy commmand like "a=1;"
        "fixedpoint" => %q!pref = fipref('LoggingMode','on'),a = fi!,
        # Image Processing
        "images" => %q!a=a&&license('checkout','image_toolbox')!,
        # Neural Networks
        "nnet" => %q!a=a&&license('checkout','neural_network_toolbox')!,
        # Optimization
        "optim" => %q!a=a&&license('checkout','optimization_toolbox')!,
        # PDE
        "pde" => %q!a=a&&license('checkout','pde_toolbox')!,
        # Robust Control
        "robust" => %q!a=a&&license('checkout','robust_toolbox')!,
        # Signal Processing
        "signal" => %q!a=a&&license('checkout','signal_toolbox')!,
        # Spline
        "spline" => %q!a=a&&license('checkout','spline_toolbox')!,
        # Statistics
        "stats" => %q!a=a&&license('checkout','statistics_toolbox')!,
        # Symbolic Maths
        "symbolic" => %q!a=a&&license('checkout','symbolic_toolbox')!,
        # System Identification
        "ident" => %q!a=a&&license('checkout','identification_toolbox')!,
        # Virtual Reality
        "vr" => %q!myworld = vrworld([])!,
        # Simulink Control Design
        "slcontrol" => %q!a=a&&license('checkout','simulink_control_design')!,
        # Simulink Stateflow
        "stateflow" => %q!object = sfclipboard!,
        # Compiler
        "compiler" => %q!a=a&&license('checkout','compiler')!

    }
    begin
      @nodeName = MatlabEngineConfig.getNodeName()
      #nodeName = "CheckMatlab"
    rescue
      @nodeName = "DummyNode"
    end

    @tmpPath = System.getProperty("java.io.tmpdir");

    logFileJava = JavaIO::File.new(@tmpPath, "ReserveMatlab"+@nodeName+".log");
    @orig_stdout = $stdout
    @orig_stderr = $stderr
    $stdout.reopen(logFileJava.toString(), "a")
    $stdout.sync=true
    $stderr.reopen $stdout

    #@logWriter = JavaIO::PrintStream.new(JavaIO::BufferedOutputStream.new(JavaIO::FileOutputStream.new(logFile, true)));

    @nodeDir = JavaIO::File.new(@tmpPath, @nodeName);
    if not @nodeDir.exists()
      @nodeDir.mkdir();
    end

    @host = InetAddress.getLocalHost().getHostName();

    @sep = System.getProperty("file.separator")

  end

  def log(str)
    #@logWriter.println(str)
  end

  def close
    #@logWriter.close();

    $stdout = @orig_stdout
    $stderr = @orig_stderr
  end


  def os
    @os ||= begin
      case RbConfig::CONFIG['host_os']
        when /mswin|msys|mingw32|Windows/
          :windows
        when /darwin|mac os/
          :macosx
        when /linux/
          :linux
        when /solaris|bsd/
          :unix
        else
          # unlikely
          raise "unknown os #{RbConfig::CONFIG['host_os']}"
      end
    end
  end


  def toolbox_code(tb)
    return @toolboxmap[tb]
  end

  # Executes the selection script
  def findConf!
    @conf = MatlabEngineConfig.getCurrentConfiguration()
    if @conf == nil
      return false
    end

    return true
  end

  def conf
    return @conf
  end

  def flock(file, mode)
    success = file.flock(mode)
    if success
      begin
        yield file
      ensure
        file.flock(File::LOCK_UN)
      end
    end
    return success
  end

  def open_lock(filename, openmode="r", lockmode=nil)
    if openmode == 'r' || openmode == 'rb'
      lockmode ||= File::LOCK_SH
    else
      lockmode ||= File::LOCK_EX
    end
    value = nil
    open(filename, openmode) do |f|
      flock(f, lockmode) do
        begin
          value = yield f
        ensure
          f.flock(File::LOCK_UN) # Comment this line out on Windows.
        end
      end
      return value
    end
  end


  def runMatlab

    log(Date.new().to_string()+" : Executing toolbox checking script on " + @host)
    puts Date.new().to_string()+" : Executing toolbox checking script on " + @host

    testF = JavaIO::File.new(@nodeDir, "matlabTest1.lock")
    testF2 = JavaIO::File.new(@nodeDir, "matlabTest2.lock")
    if testF2.exists
      testF2.delete()
    end
    if testF.exists
      testF.delete()
    end
    commandArray = Array.new
    case os
      when :windows
        if @conf.hasManyConfig()
          lastregjfile = JavaIO::File.new(@tmpPath, 'matlabLastReg.txt')
          currentversion = false;
          if lastregjfile.exists
            open_lock(lastregjfile.toString, 'r') do |f|
              version = f.gets
              if version != nil
                currentversion = (version == @conf.getVersion)
              end
            end
          end
          if !currentversion
            open_lock(lastregjfile.toString, 'w') do |f|

              lastregfile = File.new(lastregjfile.toString, 'w')
              lastregfile.flock(File::LOCK_EX)
              regCommandArray = Array.new
              regCommandArray << @conf.get_matlab_home() + @sep + @conf.get_matlab_bin_dir + @sep + @conf.get_matlab_command_name
              regCommandArray << "/regserver"
              regCommandArray << "-r"
              regCommandArray << %q!fid = fopen('!+testF2.getAbsolutePath()+%q!','w');fclose(fid);exit();!
              puts regCommandArray
              proc2 = Runtime.getRuntime().exec(regCommandArray.to_java java.lang.String)
              cpt = 0
              while (not testF2.exists()) and (cpt < 1000)
                sleep(0.05)
                cpt = cpt + 1
              end
              if testF2.exists()
                f << @conf.getVersion()
                log("OK");
                puts "OK"
                testF2.delete
                proc2.destroy();
              else
                proc2.destroy();
                log("KO");
                puts "KO"
                return false
              end
            end
          end


        end


        commandArray << @conf.get_matlab_home() + @sep + @conf.get_matlab_bin_dir + @sep + @conf.get_matlab_command_name
        commandArray << "/MLAutomation"
        commandArray << "-Embedding"
        commandArray << "-r"
      when :linux
        commandArray << @conf.get_matlab_home() + @sep + @conf.get_matlab_bin_dir + @sep + @conf.get_matlab_command_name
        commandArray << "-nodisplay"
        commandArray << "-nojvm"
        commandArray << "-nosplash"
        commandArray << "-r"
      else
        raise "Unsupported os #{RbConfig::CONFIG['host_os']}"
    end

    tcode = toolbox_code('matlab') + ','
    if (defined? $args) && ($args.size > 0)
      $args.each do |a|
        tcode += toolbox_code(a) + ','
      end
    end
    commandArray << tcode + %q!if a,fid = fopen('!+testF.getAbsolutePath()+%q!','w'),fclose(fid),else exit(),end;while true,pause(500),! + tcode + %q!end;exit()!

    puts commandArray
    #cmdList = java.util.Arrays.asList(commandArray.to_java java.lang.String)
    #log(cmdList)


    pb = ProcessBuilder.new(commandArray.to_java java.lang.String)
    mapenv = pb.environment
    mapenv.put("SELECTION_SCRIPT", @nodeName)
    proc = pb.start
    #proc = Runtime.getRuntime().exec(commandArray.to_java java.lang.String)

    puts 'command executed'
    cpt = 0
    while (not testF.exists()) and (cpt < 1000)
      sleep(0.05)
      cpt = cpt + 1
    end
    if testF.exists()
      testF.delete
      MatlabEngineConfig.setSelectionScriptProcess(proc)
      log("OK");
      puts "OK"
      return true
    else
      proc.destroy();
      log("KO");
      puts "KO"
      return false
    end


#      if $?.to_i == 0
#          info.each_line do |l|
#            puts l
#          end
#      end

  end
end

$selected = false

begin
  cm = ReserveMatlab.new

  if cm.findConf!
    $selected = cm.runMatlab
  end

rescue Exception => e
  puts e.message + "\n" + e.backtrace.join("\n")
  raise java.lang.RuntimeException.new(e.message + "\n" + e.backtrace.join("\n"))
ensure
  cm.close
end
