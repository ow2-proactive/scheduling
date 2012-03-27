include Java

import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabEngineConfig

import java.lang.System

module JavaIO
  include_package "java.io"
end
import java.net.InetAddress
import java.util.Date
import java.util.HashSet

class ReserveMatlab

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
        "spline" => %q!Spline_Toolbox!,
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



  def toolbox_code(tb)
    return @toolboxmap[tb]
  end


  def checkFeature(client, rid, login, feature)
    if client.areLicensed(feature)
      # TODO login with runAsMe
      begin
      request = LicenseRequest.new(rid, login, feature)
      tf = client.hasLicense(request);
      rescue
          return false
        end
      log(tf)
      return tf;
    else
      log("unlicensed")
    end
    return false

  end


  def checkMatlab

    log(Date.new().to_string()+" : Executing toolbox checking script on " + @host)
    begin
      import com.activeeon.proactive.license_saver.client.LicenseSaverClient
      import com.activeeon.proactive.license_saver.LicenseRequest
    rescue Exception => e
      log("Warning : Licensing proxy classes not found, license checking disabled")
      return true
    end

    if (defined? $args) && ($args.size >= 3)
      rid = $args[0]
      login = $args[1]
      serverurl = $args[2]
      $args.each do |a|
        log(a)
      end
      if serverurl == nil
         log("Warning : Licensing proxy not specified, license checking disabled")
         return true
      end
      begin
        client = LicenseSaverClient.new(serverurl)
      rescue Exception => e
        log(e.message + "\n" + e.backtrace.join("\n"))
        log("Error : Licensing proxy cannot be found at url : "+serverurl +" , host not selected")
        return false
      end
      if $args.length > 3
        feat_set = HashSet.new();

        $args[3..$args.length].each do |a|
          tcode = toolbox_code(a)
          log(tcode)
          feat_set.add(tcode)

        end

        tf = checkFeature(client, rid, login, feat_set)

        # use the code and login to contact the proxy server for each Matlab feature
        return tf;
      else
         tcode = toolbox_code("matlab")
         return checkFeature(client, rid, login, tcode)
      end
    end
    return false;
  end
end

$selected = false

begin
  cm = ReserveMatlab.new

  #$selected = true
  $selected = cm.checkMatlab

rescue Exception => e
  puts e.message + "\n" + e.backtrace.join("\n")
  raise java.lang.RuntimeException.new(e.message + "\n" + e.backtrace.join("\n"))
ensure
  cm.close
end
