function this = PAScheduler(varargin)

this.PAgetsolver = @PAgetsolver;
this.PAgetlogin = @PAgetlogin;
this.findUsedToolboxes = @findUsedToolboxes;
this.findDependency = @findDependency;
this.PAaddDirToClean = @PAaddDirToClean;
this.PAaddFileToClean = @PAaddFileToClean;
this.PAgetInputDeployer = @PAgetInputDeployer;
this.PAgetOutputDeployer = @PAgetOutputDeployer;
this.PAprepare=@PAprepare;
this.serialize=@serialize;
this.PAJobInfo = @PAJobInfo;
this.PATaskRepository = @PATaskRepository;
this.logindlg=@logindlg;

this = class(this, 'PAScheduler');