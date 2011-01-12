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

if (isempty(this) && isempty(S)) && strcmp(class(A),'PAResult')
    this = A;
end

if length(S) == 1
    switch S.type
        case '()'
            if strcmp(class(A),'PAResult')
                if length(S.subs) == 1
                    il = S.subs{1};
                    if isscalar(A)
                        for i=1:length(il)
                            if isnumeric(this)
                                this=A;
                            else
                                this(il(i))=A;
                            end
                        end
                    else
                        for i=1:length(il)
                            if isnumeric(this)
                                this=A(i);
                            else
                                this(il(i))=A(i);
                            end
                        end
                    end
                elseif length(S.subs) == 2
                    il = S.subs{1};
                    jl = S.subs{2};
                    if size(A,1) == 1 && size(A,2) == 1

                        for i=1:length(il)
                            for j=1:length(jl)
                                this(il(i),jl(j))=A;
                            end
                        end
                    else
                        for i=1:size(A,1)
                            for j=1:size(A,2)
                                this(il(i),jl(j))=A(i,j);
                            end
                        end
                    end
                else
                    error('PAResult:subsasgn',...
                        'arrays with more than 2 dimensions not supported')
                end
                out=this;
                %out(S.subs{:})=PATask(A.Func,A.Params,A.Description,A.InputFiles,A.OutputFiles);
            else
                error('PAResult:subsasgn',...
                    'Not a supported subscripted assignment')
            end
        case '{}'
            error('PAResult:subsasgn',...
                'Not a supported subscripted assignment')

    end
elseif length(S) == 2
    error('PAResult:subsasgn',...
        'Not a supported subscripted assignment')
    

else
    error('PAResult:subsasgn',...
        'Not a supported subscripted assignment')
end
