function [out]=myHello(in)
    nl = evalin('caller','NODE_LIST'); 
    nul = evalin('caller','NODE_URL_LIST'); 
    disp(['Number of nodes used : ' num2str(length(nl))]);
    for i=1:length(nl)
        disp(['Node n°' num2str(i) ': ' nl{i} ' ' nul{i} ]);
    end
    disp(['Hello ' in]);
    out=true;
end