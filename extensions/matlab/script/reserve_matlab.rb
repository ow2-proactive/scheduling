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
        "matlab" => %q!MATLAB!,
        # Simulink
        "simulink" => %q!SIMULINK!,
        # Control System
        "control" => %q!Control_Toolbox!,
        # Curve Fitting Toolbox
        "curvefit" => %q!Curve_Fitting_Toolbox!,
        # Image Processing
        "images" => %q!Image_Toolbox!,
        # Compiler
        "compiler" => %q!Compiler!,
        # Neural Networks
        "nnet" => %q!Neural_Network_Toolbox!,
        # Optimization
        "optim" => %q!Optimization_Toolbox!,
        # PDE
        "pde" => %q!PDE_Toolbox!,
        # Robust Control
        "robust" => %q!Robust_Toolbox!,
        # Signal Processing
        "signal" => %q!Signal_Toolbox!,
        # Simulink Control Design
        "slcontrol" => %q!Simulink_Control_Design!,
        # Spline
        "spline" => %q!Spline_Toolbox')!,
        # Statistics
        "stats" => %q!Statistics_Toolbox!,
        # Symbolic Maths
        "symbolic" => %q!Symbolic_Toolbox!,
        # System Identification
        "ident" => %q!Identification_Toolbox!

    }
    begin
      @nodeName = MatlabEngineConfig.getNodeName()
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

    @nodeDir = JavaIO::File.new(@tmpPath, @nodeName);
    if not @nodeDir.exists()
      @nodeDir.mkdir();
    end

    @host = InetAddress.getLocalHost().getHostName();

    @sep = System.getProperty("file.separator")

  end

  def log(str)
    puts(str)
  end

  def close
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


  def checkMatlab

    log(Date.new().to_string()+" : Executing toolbox checking script on " + @host)
    if (defined? $args) && ($args.size > 0)
      login = $args[0]
      if $args.size > 1
        $args[1..$args.size-1].each do |a|
          tcode = toolbox_code(a)
          # use the code and login to contact the proxy server for each Matlab feature
        end
      end
    end
    return true;
  end
end

$selected = false

begin
  cm = ReserveMatlab.new

  if cm.findConf!
    $selected = cm.checkMatlab
  end

rescue Exception => e
  puts e.message + "\n" + e.backtrace.join("\n")
  raise java.lang.RuntimeException.new(e.message + "\n" + e.backtrace.join("\n"))
ensure
  cm.close
end
