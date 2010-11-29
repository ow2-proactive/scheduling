include Java

import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabEngineConfig
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabInitException
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabFinder
import com.sun.jna.NativeLibrary

import java.lang.System

import java.net.InetAddress
import java.util.Date
import java.lang.Runtime

module JavaIO
    include_package "java.io"
end



class MatsciFinder

  require 'rbconfig'

  def inf(v1, v2)
    p1 = v1.split(/\./)
    p1.map! { |x| x.to_i() }

    p2 = v2.split(/\./)
    p2.map! { |x| x.to_i() }
    return (p1 <=> p2) < 0;

  end

  def initialize
    begin
      nodeName = MatlabEngineConfig.getNodeName()
      #nodeName = "CheckMatlab"
    rescue
      nodeName = "DummyNode"
    end

    tmpPath = System.getProperty("java.io.tmpdir");
    
    logFileJava = JavaIO::File.new(tmpPath, "CheckMatlab"+nodeName+".log");
    #logFile = File.new(logFileJava.toString(), "a");
    @orig_stdout = $stdout
    @orig_stderr = $stderr
    $stdout.reopen(logFileJava.toString(), "a")
    $stdout.sync=true
    $stderr.reopen $stdout    

    #@logWriter = JavaIO::PrintStream.new(JavaIO::BufferedOutputStream.new(JavaIO::FileOutputStream.new(logFile, true)));

  end

  def log(str)
     #@logWriter.println(str);
  end

  def close
    #@logWriter.close();
    $stdout = @orig_stdout
    $stderr = @orig_stderr
  end

  def findMatlab(version_pref, versions_rejected, min_version, max_version)

    case MatsciFinder.os
      when :windows
        return findMatlabWindows(version_pref, versions_rejected, min_version, max_version)
      when :linux
        return findMatlabUnix(version_pref, versions_rejected, min_version, max_version)
      else
        raise "Unsupported os #{RbConfig::CONFIG['host_os']}"
    end
  end

  def findMatlabWindows(version_pref, versions_rejected, min_version, max_version)
    _configs = Array.new

    matlab_versions_found = Array.new

    access = Registry::KEY_READ

    libDirectory = matlabLibDir()
    extDirectory = matlabExtDir()
    binDirectory = matlabBinDir()
    keyname = "SOFTWARE\\Mathworks\\MATLAB"
    #puts ("Unsupported os #{RbConfig::CONFIG['host_os']}")
    Registry::HKEY_LOCAL_MACHINE.open(keyname, access) do |reg|
      reg.each_key { |k, v| matlab_versions_found.push k.chomp }
    end

    matlab_versions_found.each do |version|
      version.delete!(0.chr)
      matlabhome = Registry::HKEY_LOCAL_MACHINE.open(keyname+"\\"+version, access).read("MATLABROOT")[1]
      matlabhome = matlabhome.chomp.chop
      if (versions_rejected != nil && versions_rejected.index(version)) || (min_version != nil && inf(version, min_version)) || (max_version != nil && inf(max_version, version))
        puts version + " rejected"
        log(version + " rejected")
      elsif version == version_pref
          conf = checkConfig(matlabhome, version, libDirectory, binDirectory, extDirectory, "matlab.exe")
          if conf != nil
             _configs.clear
             _configs.push conf
            puts version + ' preferred'
            log(version + " preferred")
          end
          return _configs
        else
          conf = checkConfig(matlabhome, version, libDirectory, binDirectory, extDirectory, "matlab.exe")
          if conf != nil
            puts version + ' accepted'
            log(version + " accepted")
            _configs.push conf
          end
        end
    end

    return _configs

  end


  def findMatlabUnix(version_pref, versions_rejected, min_version, max_version)
    _configs = Array.new
    matlab_versions_text = ["matlab2010b", "matlab2010a", "matlab2009b", "matlab2009a", "matlab2008b", "matlab2008a", "matlab2007b", "matlab2007a", "matlab2006b", "matlab2006a"]
    matlab_versions_num = ["7.11", "7.10", "7.9", "7.8", "7.7", "7.6", "7.5", "7.4", "7.3", "7.2"]

    libDirectory = matlabLibDir()
    extDirectory = matlabExtDir()
    binDirectory = matlabBinDir()
    matlab_versions_text.each do |version_txt|
      matlabsym = `which #{version_txt} 2>/dev/null`
      if $?.to_i == 0
        #puts('|' + matlabsym.strip() +'|')
        matlabsym = matlabsym.strip()

        matlabbin = readlink!(matlabsym)
        matlabhome = File.dirname(File.dirname(matlabbin))

        ind = matlab_versions_text.index(version_txt)
        version = matlab_versions_num[ind]

        if (versions_rejected != nil && versions_rejected.index(version)) || (min_version != nil && inf(version, min_version)) || (max_version != nil && inf(max_version, version))
          puts version + ' rejected'
          log(version + " rejected")
        elsif version == version_pref
          conf = checkConfig(matlabhome, version, libDirectory, binDirectory, extDirectory, "matlab")
          if conf != nil
             _configs.clear
             _configs.push conf
            puts version + ' preferred'
            log(version + " preferred")
          end                             
          return _configs
        else
          conf = checkConfig(matlabhome, version, libDirectory, binDirectory, extDirectory, "matlab")
          if conf != nil
            puts version + ' accepted'
            log(version + " accepted")
            _configs.push conf
          end          
        end
      end
    end
    return _configs
  end

  def checkConfig(matlabhome, version, libDirectory, binDirectory, extDirectory, command)
    home = JavaIO::File.new(matlabhome);
    if (!home.exists || !home.canRead || !home.isDirectory)
      puts home.toString " cannot be found."
      return nil;
    end
    libdir = JavaIO::File.new(home, libDirectory)
    if (!libdir.exists || !libdir.canRead || !libdir.isDirectory)
      puts libdir.toString " cannot be found."
      return nil;
    end
    bindir = JavaIO::File.new(home, binDirectory)
    if (!bindir.exists || !bindir.canRead || !bindir.isDirectory)
      puts bindir.toString " cannot be found."
      return nil;
    end
    comm = JavaIO::File.new(bindir, command)
    if (!comm.exists || !comm.canExecute || !comm.isFile)
      puts comm.toString " cannot be found."
      return nil;
    end
    conf = MatlabEngineConfig.new(matlabhome, version, libDirectory, binDirectory, extDirectory, command)
    ptodir = ptolemyDir(conf)
    ptodirjava  = JavaIO::File.new(ptodir)
    if (!ptodirjava.exists || !ptodirjava.canRead || !ptodirjava.isDirectory)
      puts ptodirjava.toString " cannot be found."
      return nil;
    end
    conf.setPtolemyPath(ptodir)
    return conf;

  end


  def matlabLibDir
    case MatsciFinder.os
      when :windows
        if arch64?
          "bin\\win64"
        else
          "bin\\win32"
        end
      when :linux
        if arch64?
          "bin/glnxa64"
        else
          "bin/glnx86"
        end
      else
        raise "Unsupported os #{RbConfig::CONFIG['host_os']}"
    end
  end

  def matlabBinDir
    case MatsciFinder.os
      when :windows
        if arch64?
          "bin\\win64"
        else
          "bin\\win32"
        end
      when :linux
        if arch64?
          "bin"
        else
          "bin"
        end
      else
        raise "Unsupported os #{RbConfig::CONFIG['host_os']}"
    end
  end

  def matlabExtDir
    case MatsciFinder.os
      when :windows
        if arch64?
          nil
        else
          nil
        end
      when :linux
        if arch64?
          "sys/os/glnxa64"
        else
          "sys/os/glnx86"
        end
      else
        raise "Unsupported os #{RbConfig::CONFIG['host_os']}"
    end
  end

  def ptolemyDir(conf)
    cz = conf.getClass()
    res = cz.getResource('/ptolemy/matlab')
    if (res == nil)
      raise "Unable to find resource /ptolemy/matlab"
    end
    conn = res.openConnection
    jarFileURL = conn.getJarFileURL()
    jarFile = java.io.File.new(jarFileURL.toURI())
    libDirFile = jarFile.getParentFile()
    ptolemyLibDirURI = libDirFile.toURI()

    pattern = conf.getVersion() + "/" + conf.getMatlabLibDirName().gsub("\\", "/") + "/"

    ptolemyLibDirURI = ptolemyLibDirURI.resolve(pattern);
    answer = java.io.File.new(ptolemyLibDirURI);
    if (!answer.exists() || !answer.canRead())
      raise "Can't find ptolemy native library at " + answer.toString() +
              ". The native library is generated from scripts in SCHEDULER/extensions/matlab. Refer to README file."
    else
      libraryFile = java.io.File.new(ptolemyLibDirURI.resolve(java.lang.System.mapLibraryName("ptmatlab")))
      if !libraryFile.exists() || !libraryFile.canRead()
        raise "Can't find ptolemy native library at " + libraryFile.toString() +
                ". The native library is generated from scripts in SCHEDULER/extensions/matlab. Refer to README file."

      end
    end
    return answer.getAbsolutePath()
  end

  def MatsciFinder.os
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

  def readlink!(path)
    path = File.expand_path(path)
    return path unless File.symlink?(path)
    dirname = File.dirname(path)
    readlink = File.readlink(path)
    if not readlink =~ /^\// # it's a relative path
      readlink = dirname + '/'+ readlink # make it absolute
    end
    readlink = File.expand_path(readlink) # eliminate this/../../that
    if File.symlink?(readlink)
      return File.readlink!(readlink) # recursively follow symlinks
    else
      return readlink
    end
  end


  def bits
    @bits ||= (
    if 1.size == 8
      64
    else
      32
    end
    )
    #@bits ||= (
    #if %w[ x86_64 amd64 i686 ].include? RbConfig::CONFIG['host_cpu']
    #  64
    #else
    #  32
    #end
    #)
  end

  def MatsciFinder.windows?
    os == :windows
  end

  def arch64?
    bits == 64
  end


end


class Registry

  module Constants
    KEY_WOW64_64KEY = 0x0100
    KEY_WOW64_32KEY = 0x0200


    HKEY_CLASSES_ROOT = 0x80000000
    HKEY_CURRENT_USER = 0x80000001
    HKEY_LOCAL_MACHINE = 0x80000002
    HKEY_USERS = 0x80000003
    HKEY_PERFORMANCE_DATA = 0x80000004
    HKEY_PERFORMANCE_TEXT = 0x80000050
    HKEY_PERFORMANCE_NLSTEXT = 0x80000060
    HKEY_CURRENT_CONFIG = 0x80000005
    HKEY_DYN_DATA = 0x80000006

    REG_NONE = 0
    REG_SZ = 1
    REG_EXPAND_SZ = 2
    REG_BINARY = 3
    REG_DWORD = 4
    REG_DWORD_LITTLE_ENDIAN = 4
    REG_DWORD_BIG_ENDIAN = 5
    REG_LINK = 6
    REG_MULTI_SZ = 7
    REG_RESOURCE_LIST = 8
    REG_FULL_RESOURCE_DESCRIPTOR = 9
    REG_RESOURCE_REQUIREMENTS_LIST = 10
    REG_QWORD = 11
    REG_QWORD_LITTLE_ENDIAN = 11

    STANDARD_RIGHTS_READ = 0x00020000
    STANDARD_RIGHTS_WRITE = 0x00020000
    KEY_QUERY_VALUE = 0x0001
    KEY_SET_VALUE = 0x0002
    KEY_CREATE_SUB_KEY = 0x0004
    KEY_ENUMERATE_SUB_KEYS = 0x0008
    KEY_NOTIFY = 0x0010
    KEY_CREATE_LINK = 0x0020

    bits ||= (
    if 1.size == 8
      64
    else
      32
    end
    )

    if bits == 64
      KEY_READ = STANDARD_RIGHTS_READ |
              KEY_QUERY_VALUE | KEY_ENUMERATE_SUB_KEYS | KEY_NOTIFY | (KEY_WOW64_64KEY | KEY_WOW64_32KEY)
    else
      KEY_READ = STANDARD_RIGHTS_READ |
              KEY_QUERY_VALUE | KEY_ENUMERATE_SUB_KEYS | KEY_NOTIFY
    end

    KEY_WRITE = STANDARD_RIGHTS_WRITE |
            KEY_SET_VALUE | KEY_CREATE_SUB_KEY
    KEY_EXECUTE = KEY_READ
    KEY_ALL_ACCESS = KEY_READ | KEY_WRITE | KEY_CREATE_LINK

    REG_OPTION_RESERVED = 0x0000
    REG_OPTION_NON_VOLATILE = 0x0000
    REG_OPTION_VOLATILE = 0x0001
    REG_OPTION_CREATE_LINK = 0x0002
    REG_OPTION_BACKUP_RESTORE = 0x0004
    REG_OPTION_OPEN_LINK = 0x0008
    REG_LEGAL_OPTION = REG_OPTION_RESERVED |
            REG_OPTION_NON_VOLATILE | REG_OPTION_CREATE_LINK |
            REG_OPTION_BACKUP_RESTORE | REG_OPTION_OPEN_LINK

    REG_CREATED_NEW_KEY = 1
    REG_OPENED_EXISTING_KEY = 2

    REG_WHOLE_HIVE_VOLATILE = 0x0001
    REG_REFRESH_HIVE = 0x0002
    REG_NO_LAZY_FLUSH = 0x0004
    REG_FORCE_RESTORE = 0x0008

    MAX_KEY_LENGTH = 514
    MAX_VALUE_LENGTH = 32768
  end
  include Constants
  include Enumerable


  #
  # Error
  #
  class Error < ::StandardError
    if (MatsciFinder.windows?)
      Kernel32 = NativeLibrary.getInstance('kernel32')
      FormatMessageA = Kernel32.getFunction('FormatMessageA')
    end

    def initialize(code)
      @code = code
      #msg = "\0" * 1024
      msg = java.nio.ByteBuffer.allocate(1024)
      len = FormatMessageA.invokeInt([0x1200, 0, code, 0, msg, 1024, 0].to_java)
      msg = String.from_java_bytes(msg.array)
      super msg[0, len].tr("\r", '').chomp
    end

    attr_reader :code
  end

  #
  # Predefined Keys
  #
  class PredefinedKey < Registry
    def initialize(hkey, keyname)
      @hkey = hkey
      @parent = nil
      @keyname = keyname
      @disposition = REG_OPENED_EXISTING_KEY
    end

    # Predefined keys cannot be closed
    def close
      raise Error.new(5) ## ERROR_ACCESS_DENIED
    end

    # Fake class for Registry#open, Registry#create
    def class
      Registry
    end

    # Make all
    Constants.constants.grep(/^HKEY_/) do |c|
      Registry.const_set c, new(Constants.const_get(c), c)
    end
  end

  module API

    if (MatsciFinder.windows?)
      Advapi32 = NativeLibrary.getInstance('advapi32')

      RegOpenKeyExA = Advapi32.getFunction('RegOpenKeyExA')
      RegEnumKeyExA = Advapi32.getFunction('RegEnumKeyExA')
      RegEnumValueA = Advapi32.getFunction('RegEnumValueA')
      RegQueryValueExA = Advapi32.getFunction('RegQueryValueExA')
      RegCloseKey = Advapi32.getFunction('RegCloseKey')
    end

    module_function

    def packdw(dw)
      [dw].pack('V')
    end

    def unpackdw(dw)
      dw += [0].pack('V')
      dw.unpack('V')[0]
    end

    def packqw(qw)
      [qw & 0xFFFFFFFF, qw >> 32].pack('VV')
    end

    def unpackqw(qw)
      qw = qw.unpack('VV')
      (qw[1] << 32) | qw[0]
    end

    def check(result)
      raise Error, result, caller(2) if result != 0
    end

    def OpenKey(hkey, name, opt, desired)
      #result = packdw(0)
      result = java.nio.LongBuffer.allocate(1)
      check RegOpenKeyExA.invokeLong([hkey, name, opt, desired, result].to_java)
      #unpackdw(result)
      result = result.get()
    end

    def EnumValue(hkey, index)
      #name = ' ' * Constants::MAX_KEY_LENGTH
      name = java.nio.ByteBuffer.allocate(Constants::MAX_KEY_LENGTH)
      size = packdw(Constants::MAX_KEY_LENGTH)
      check RegEnumValueA.invokeLong([hkey, index, name, size, 0, 0, 0, 0].to_java)
      name = String.from_java_bytes(name.array)
      name[0, unpackdw(size)]
    end

    def EnumKey(hkey, index)
      #name = ' ' * Constants::MAX_KEY_LENGTH
      name = java.nio.ByteBuffer.allocate(Constants::MAX_KEY_LENGTH)
      size = packdw(Constants::MAX_KEY_LENGTH)
      wtime = ' ' * 8
      check RegEnumKeyExA.invokeLong([hkey, index, name, size, 0, 0, 0, wtime].to_java)
      name = String.from_java_bytes(name.array)
      [name[0, unpackdw(size)], unpackqw(wtime)]
    end


    def QueryValue(hkey, name)
      #type = packdw(0)
      type =  java.nio.LongBuffer.allocate(1)
      #size = packdw(0)
      size =  java.nio.LongBuffer.allocate(1)
      check RegQueryValueExA.invokeLong([hkey, name, 0, type, 0, size].to_java)
      #data = ' ' * unpackdw(size)
      data = java.nio.ByteBuffer.allocate(size.get(0)+1)
      check RegQueryValueExA.invokeLong([hkey, name, 0, type, data, size].to_java)
      #data.putChar(0)
      data = String.from_java_bytes(data.array)

      #[unpackdw(type), data[0, unpackdw(size)]]
      [type.get(), data[0, size.get()]]
    end

    def CloseKey(hkey)
      check RegCloseKey.invokeLong([hkey].to_java)
    end
  end

  #
  # utility functions
  #
  def self.expand_environ(str)
    str.gsub(/%([^%]+)%/) { ENV[$1] || ENV[$1.upcase] || $& }
  end

  @@type2name = {}
  %w[
      REG_NONE REG_SZ REG_EXPAND_SZ REG_BINARY REG_DWORD
      REG_DWORD_BIG_ENDIAN REG_LINK REG_MULTI_SZ
      REG_RESOURCE_LIST REG_FULL_RESOURCE_DESCRIPTOR
      REG_RESOURCE_REQUIREMENTS_LIST REG_QWORD
         ].each do |type|
    @@type2name[Constants.const_get(type)] = type
  end

  def self.type2name(type)
    @@type2name[type] || type.to_s
  end

  def self.wtime2time(wtime)
    Time.at((wtime - 116444736000000000) / 10000000)
  end

  def self.time2wtime(time)
    time.to_i * 10000000 + 116444736000000000
  end

  #
  # constructors
  #
  private_class_method :new

  def self.open(hkey, subkey, desired = KEY_READ, opt = REG_OPTION_RESERVED)
    subkey = subkey.chomp('\\')
    newkey = API.OpenKey(hkey.hkey, subkey, opt, desired)
    obj = new(newkey, hkey, subkey, REG_OPENED_EXISTING_KEY)
    if block_given?
      begin
        yield obj
      ensure
        obj.close
      end
    else
      obj
    end
  end

  #
  # finalizer
  #
  @@final = proc { |hkey| proc { API.CloseKey(hkey[0]) if hkey[0] } }

  #
  # initialize
  #
  def initialize(hkey, parent, keyname, disposition)
    @hkey = hkey
    @parent = parent
    @keyname = keyname
    @disposition = disposition
    @hkeyfinal = [hkey]
    ObjectSpace.define_finalizer self, @@final.call(@hkeyfinal)
  end

  attr_reader :hkey, :parent, :keyname, :disposition

  #
  # attributes
  #
  def created?
    @disposition == REG_CREATED_NEW_KEY
  end

  def open?
    !@hkey.nil?
  end

  def name
    parent = self
    name = @keyname
    while parent = parent.parent
      name = parent.keyname + '\\' + name
    end
    name
  end

  def inspect
    "\#<Win32::RegistryX key=#{name.inspect}>"
  end

#
  # open/close
  #
  def open(subkey, desired = KEY_READ, opt = REG_OPTION_RESERVED, & blk)
    self.class.open(self, subkey, desired, opt, & blk)
  end


  def close
    API.CloseKey(@hkey)
    @hkey = @parent = @keyname = nil
    @hkeyfinal[0] = nil
  end

  #
  # iterator
  #
  def each_value
    index = 0
    while true
      begin
        subkey = API.EnumValue(@hkey, index)
      rescue Error
        break
      end
      begin
        type, data = read(subkey)
      rescue Error
        next
      end
      yield subkey, type, data
      index += 1
    end
    index
  end

  alias each each_value

  def each_key
    index = 0
    while true
      begin
        subkey, wtime = API.EnumKey(@hkey, index)
      rescue Error
        break
      end
      yield subkey, wtime
      index += 1
    end
    index
  end

  def keys
    keys_ary = []
    each_key { |key,| keys_ary << key }
    keys_ary
  end

  #
  # reader
  #
  def read(name, * rtype)
    type, data = API.QueryValue(@hkey, name)
    unless rtype.empty? or rtype.include?(type)
      raise TypeError, "Type mismatch (expect #{rtype.inspect} but #{type} present)"
    end
    case type
      when REG_SZ, REG_EXPAND_SZ
        [type, data.chop]
      when REG_MULTI_SZ
        [type, data.split(/\0/)]
      when REG_BINARY
        [type, data]
      when REG_DWORD
        [type, API.unpackdw(data)]
      when REG_DWORD_BIG_ENDIAN
        [type, data.unpack('N')[0]]
      when REG_QWORD
        [type, API.unpackqw(data)]
      else
        raise TypeError, "Type #{type} is not supported."
    end
  end

  def [](name, * rtype)
    type, data = read(name, * rtype)
    case type
      when REG_SZ, REG_DWORD, REG_QWORD, REG_MULTI_SZ
        data
      when REG_EXPAND_SZ
        Registry.expand_environ(data)
      else
        raise TypeError, "Type #{type} is not supported."
    end
  end

  def read_s(name)
    read(name, REG_SZ)[1]
  end

  def read_s_expand(name)
    type, data = read(name, REG_SZ, REG_EXPAND_SZ)
    if type == REG_EXPAND_SZ
      Registry.expand_environ(data)
    else
      data
    end
  end

  def read_i(name)
    read(name, REG_DWORD, REG_DWORD_BIG_ENDIAN, REG_QWORD)[1]
  end

  def read_bin(name)
    read(name, REG_BINARY)[1]
  end


end

def main_matlab(mf, version_pref, versions_rejected, v_rej_string, min_version, max_version)

  if versions_rejected != nil
    versions_rejected = versions_rejected.to_a
  end
  begin
  cf = MatlabFinder.getInstance().findMatSci(version_pref, v_rej_string, min_version, max_version);
  rescue JavaIO::FileNotFoundException => fnfe
    puts fnfe.message
  rescue Exception => e
    puts e.message + "\n" + e.backtrace.join("\n")
  end
  if (cf == nil)
    cfs =  mf.findMatlab(version_pref, versions_rejected, min_version, max_version)
    if (cfs.size > 0)
      cf = cfs[0];
    end
  end
  #cfs.each do |cf|
  #  puts(cf.toString)
  #end

  MatlabEngineConfig.setCurrentConfiguration(cf);
  return cf

end



$selected = false
mf = MatsciFinder.new
begin

  if defined?($args)
    cpt = 0
    versionPref = nil
    versionMin = nil
    versionMax = nil
    versionRej = nil
    versionRejString = nil;
    while cpt < $args.size
      case $args[cpt]
        when "versionPref"
          versionPref  = $args[cpt+1]
        when "versionMin"
          versionMin  = $args[cpt+1]
        when "versionMax"
          versionMax  = $args[cpt+1]
        when "versionRej"

          if $args[cpt+1] != nil && $args[cpt+1].length() > 0
            versionRej = $args[cpt+1].split(/[; ,]+/)
            versionRej = versionRej - [""];
            versionRejString = $args[cpt+1];
          end
      end
      cpt += 2
    end

   cf = main_matlab(mf, versionPref, versionRej, versionRejString, versionMin, versionMax)
   
    if (cf != nil)
      puts cf
      mf.log(cf)
      $selected = true
    else
      $selected = false
    end
  end
rescue Exception => e
  puts  e.message + "\n" + e.backtrace.join("\n")
  raise java.lang.RuntimeException.new(e.message + "\n" + e.backtrace.join("\n"))
ensure
  mf.close  
end



