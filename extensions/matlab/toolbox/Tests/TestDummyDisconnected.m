function [ok, msg]=TestDummyDisconnected(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 400000;
    else
        timeout =200000;
    end
end

opt = PAoptions;

disp('................ Testing a simulated disconnected mode');

disp('........ First submitting two jobs and keep their info');
disp('..............................................submit 1');
resl1 = PAsolve(@factorial,{1},{2},{3},{4},{5});
disp('..............................................submit 2');
resl2 = PAsolve(@factorial,{1},{2},{3},{4},{5});

sched = PAScheduler;

jobs = sched.PATaskRepository('uncomplete');

n = length(jobs);

jobs = jobs(n-1:n);

jinfo1 = sched.PATaskRepository(jobs{1}, 'jobinfo');
alltasks1 = sched.PATaskRepository(jobs{1}, 'alltasks');
taskinfolist1 = {};
for j=1:length(alltasks1)
    taskinfo = sched.PATaskRepository(jobs{1}, alltasks1{j}, 'taskinfo');
    taskinfolist1 = [taskinfolist1 {taskinfo}];

end

jinfo2 = sched.PATaskRepository(jobs{2}, 'jobinfo');
alltasks2 = sched.PATaskRepository(jobs{2}, 'alltasks');
taskinfolist2 = {};
for j=1:length(alltasks1)
    taskinfo = sched.PATaskRepository(jobs{2}, alltasks1{j}, 'taskinfo');
    taskinfolist2 = [taskinfolist2 {taskinfo}];

end

disp('..................................then wait for results');
val1=PAwaitAll(resl1,timeout)
val2=PAwaitAll(resl2,timeout)

[ok,msg]=checkValuesFact(val1);
if ~ok disp(msg),return; end
[ok,msg]=checkValuesFact(val2);
if ~ok disp(msg),return; end

disp('........... retrieve the same results from the scheduler');

solver = sched.PAgetsolver();

resfutureList1 = solver.retrieve(jinfo1);

for i=1:resfutureList1.size()
    syncres1(i)=PAResult(resfutureList1.get(i-1), taskinfolist1{i});
    sched.PAaddDirToClean(jobs{1}, taskinfolist1{i}.cleanDirSet);
end

resfutureList2 = solver.retrieve(jinfo2);

for i=1:resfutureList2.size()
    syncres2(i)=PAResult(resfutureList2.get(i-1), taskinfolist2{i});
    sched.PAaddDirToClean(jobs{2}, taskinfolist1{i}.cleanDirSet);
end

val1=PAwaitAll(syncres1,timeout)
val2=PAwaitAll(syncres2,timeout)



function [ok,msg]=checkValuesFact(val)
[ok,msg]=checkValues(val,{1,2,6,24,120},'factorial');

function [ok,msg]=checkValues(val,right,name)
if length(right) ~= length(val)
    ok = false;
    msg = 'Wrong number of outputs';
else
    for i=1:length(right)
        if iscell(val)
            if val{i} ~= right{i}
                ok = false;
                msg = ['TestMultipleSubmit::Wrong value of ' name '(' num2str(i) '), received ' num2str(val{i}) ', expected ' num2str(right{i})];
            else
                ok = true;
                msg = [];
            end
        else
            if val(i) ~= right{i}
                ok = false;
                msg = ['TestMultipleSubmit::Wrong value of ' name '(' num2str(i) '), received ' num2str(val(i)) ', expected ' num2str(right{i})];
            else
                ok = true;
                msg = [];
            end
        end
    end

end