@rem replace the dummy path below by the current rm_virtualization_main.py location
@rem to avoid python path issues, please supply an absolute file path
python "path\to\Scheduling\scripts\virtualization\rm_virtualization_main.py" rm-node-starter.log
@rem you can now specify this kind of batch file as a daemon on a Windows host using Sc & autoexnt.
@rem you just have to add the path of this file within your autoexnt.bat file wich is situated in C:\WINDOWS\system32\autoexnt.bat