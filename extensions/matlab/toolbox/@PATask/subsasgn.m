% /*
%   * ################################################################
%   *
%   * ProActive Parallel Suite(TM): The Java(TM) library for
%   *    Parallel, Distributed, Multi-Core Computing for
%   *    Enterprise Grids & Clouds
%   *
%   * Copyright (C) 1997-2011 INRIA/University of
%   *                 Nice-Sophia Antipolis/ActiveEon
%   * Contact: proactive@ow2.org or contact@activeeon.com
%   *
%   * This library is free software; you can redistribute it and/or
%   * modify it under the terms of the GNU Affero General Public License
%   * as published by the Free Software Foundation; version 3 of
%   * the License.
%   *
%   * This library is distributed in the hope that it will be useful,
%   * but WITHOUT ANY WARRANTY; without even the implied warranty of
%   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
%   * Affero General Public License for more details.
%   *
%   * You should have received a copy of the GNU Affero General Public License
%   * along with this library; if not, write to the Free Software
%   * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
%   * USA
%   *
%   * If needed, contact us to obtain a release under GPL Version 2 or 3
%   * or a different license than the AGPL.
%   *
%   *  Initial developer(s):               The ProActive Team
%   *                        http://proactive.inria.fr/team_members.htm
%   *  Contributor(s):
%   *
%   * ################################################################
%   * $$PROACTIVE_INITIAL_DEV$$
%   */
function out = subsasgn(this, S, A)
%S.type
%S.subs
if isempty(this)
    this = PATask;
end
if isempty(S) && strcmp(class(A),'PATask')
    this = PATask();
    this=assign(this,A);
end

if length(S) == 1
    switch S.type
        case '.'
            checkValidity(S.subs,A);
            out = builtin('subsasgn',this,S,A);
        case '()'
            if strcmp(class(A),'PATask')                
                if length(S.subs) == 1
                    il = S.subs{1};
                    if isscalar(A)
                        %this(il)=arrayfun(@(x)A,this(il));
                        for i=1:length(S.subs{1})
                            tsk = PATask();
                            tsk=assign(tsk,A);                          
                            this(il(i))=tsk;
                        end
                    else
                        %this(il)=arrayfun(@(x,B)A,this(il),A);
                        for i=1:length(S.subs{1})
                            tsk = PATask();
                            tsk=assign(tsk, A(i));                         
                            this(il(i))=tsk;
                        end
                    end
                elseif length(S.subs) == 2
                    il = S.subs{1};
                    jl = S.subs{2};
                    if size(A,1) == 1 && size(A,2) == 1
                        for i=1:length(il)
                            for j=1:length(jl)
                                tsk = PATask();
                                tsk=assign(tsk,A); 
                                this(il(i),jl(j))=tsk;
                            end
                        end
                    else                        
                        for i=1:length(il)
                            for j=1:length(jl)
                                tsk = PATask();
                                tsk=assign(tsk,A(i,j));  
                                this(il(i),jl(j))=tsk;
                            end
                        end
                    end
                else
                    error('PATask:subsasgn',...
                        'arrays with more than 2 dimensions not supported')
                end
                out=this;

            else
                error('PATask:subsasgn',...
                    'Not a supported subscripted assignment')
            end
        case '{}'
            error('PATask:subsasgn',...
                'Not a supported subscripted assignment')

    end
elseif length(S) == 2
    switch S(1).type
        case '.'
            error('PATask:subsasgn',...
                'Not a supported subscripted assignment')
        case '()'
            if strcmp(class(this),'PATask')
                newS=substruct('()',S(1).subs);
                subList = builtin('subsref',this,newS);
                %substr=num2str(S(1).subs{1});
                %for k=2:length(S(1).subs)
                % substr=[substr ','  num2str(S(1).subs{k})];
                %end

                %eval(['subList=this(' substr ');']);
            else
                error('PATask:subsasgn',...
                    'Not a supported subscripted assignment')
            end
        case '{}'
            error('PATask:subsasgn',...
                'Not a supported subscripted assignment')

    end

    switch S(2).type
        case '.'
            checkValidity(S(2).subs,A);
            [n,m] = size(subList);
            for i=1:n
                for j=1:m
                    if strcmp(S(2).subs, 'Params') && ~iscell(A)
                        subList(i,j) = builtin('subsasgn',subList(i,j),S(2),{A});
                    else
                        subList(i,j) = builtin('subsasgn',subList(i,j),S(2),A);
                    end
                end
            end
        case '()'

            error('PATask:subsasgn',...
                'Not a supported subscripted assignment')

        case '{}'
            error('PATask:subsasgn',...
                'Not a supported subscripted assignment')

    end
    builtin('subsasgn',this,newS,subList);
    %eval(['this(' substr ')=subList;']);
    out=this;


else
    error('PATask:subsasgn',...
        'Not a supported subscripted assignment')
end





